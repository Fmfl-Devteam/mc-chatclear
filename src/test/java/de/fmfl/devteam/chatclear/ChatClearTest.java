package de.fmfl.devteam.chatclear;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.fmfl.devteam.chatclear.command.ChatClearCommand;
import de.fmfl.devteam.chatclear.config.ConfigurationService;
import de.fmfl.devteam.chatclear.config.Settings;
import de.fmfl.devteam.chatclear.message.Messages;
import de.fmfl.devteam.chatclear.service.ChatClearService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class ChatClearTest {
    private ConfigurationService configuration(Path file) throws Exception {
        var defaults = new YamlConfiguration();
        try (var reader =
                new java.io.InputStreamReader(
                        getClass().getResourceAsStream("/config.yml"),
                        java.nio.charset.StandardCharsets.UTF_8)) {
            defaults.load(reader);
        }
        return new ConfigurationService(file.toFile(), defaults, Logger.getAnonymousLogger());
    }

    @Test
    void validationAndMissingValuesFallback() throws Exception {
        var config = configuration(Path.of("build/test-config.yml"));
        var yaml = new YamlConfiguration();
        yaml.set("clear-lines", 1001);
        yaml.set("bypass-enabled", "false");
        yaml.set("messages.self-success", "<red>broken");
        var settings = config.parse(yaml);
        assertEquals(100, settings.clearLines());
        assertTrue(settings.bypassEnabled());
        assertEquals(config.current().messages(), settings.messages());
        for (Object value : List.of(-1, 0, "10", 2.5)) {
            yaml.set("clear-lines", value);
            assertEquals(100, config.parse(yaml).clearLines());
        }
        for (int value : List.of(1, 1000)) {
            yaml.set("clear-lines", value);
            assertEquals(value, config.parse(yaml).clearLines());
        }
    }

    @Test
    void malformedReloadPreservesSnapshot() throws Exception {
        Path path = Path.of("build/malformed-test.yml");
        var config = configuration(path);
        var before = config.current();
        Files.writeString(path, "messages: [unterminated");
        try {
            assertFalse(config.reload());
            assertSame(before, config.current());
            Files.writeString(path, "clear-lines: 2\nbypass-enabled: false\n");
            assertTrue(config.reload());
            assertEquals(2, config.current().clearLines());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void placeholdersAreLiteral() throws Exception {
        var settings = configuration(Path.of("build/test-config.yml")).current();
        String actor = "<red>Admin</red>";
        var text =
                PlainTextComponentSerializer.plainText()
                        .serialize(Messages.render(settings, "announcement", actor, 0));
        assertTrue(text.contains(actor));
    }

    @Test
    void modesBypassAndOfflinePlayers() throws Exception {
        var initial = configuration(Path.of("build/test-config.yml")).current();
        var settings = new Settings(2, true, initial.messages());
        Player normal = mock(Player.class),
                bypass = mock(Player.class),
                offline = mock(Player.class);
        when(normal.isOnline()).thenReturn(true);
        when(bypass.isOnline()).thenReturn(true);
        when(bypass.hasPermission("fmfl.chatclear.bypass")).thenReturn(true);
        var service = new ChatClearService(() -> List.of(normal, bypass, offline));
        assertEquals(1, service.clearGlobal(settings, "Admin", false));
        verify(normal, times(2)).sendMessage(Component.empty());
        verify(normal, times(3)).sendMessage(any(Component.class));
        verify(bypass, never()).sendMessage(any(Component.class));
        verify(offline, never()).sendMessage(any(Component.class));
        clearInvocations(normal, bypass);
        assertEquals(
                2, service.clearGlobal(new Settings(2, false, initial.messages()), "Admin", true));
        verify(normal, times(2)).sendMessage(any(Component.class));
        verify(bypass, times(2)).sendMessage(any(Component.class));
        service.clearSelf(bypass, settings);
        verify(bypass, times(4)).sendMessage(Component.empty());
        assertEquals(0, new ChatClearService(List::of).clearGlobal(settings, "Console", false));
    }

    @Test
    void commandPermissionsAndCompletion() throws Exception {
        var config = configuration(Path.of("build/test-config.yml"));
        var service = mock(ChatClearService.class);
        var command = new ChatClearCommand(config, service);
        CommandSender sender = mock(CommandSender.class);
        when(sender.getName()).thenReturn("Console");
        assertTrue(command.onCommand(sender, null, "cc", new String[0]));
        verifyNoInteractions(service);
        when(sender.hasPermission("fmfl.chatclear.silent")).thenReturn(true);
        command.onCommand(sender, null, "cc", new String[] {"silent"});
        verifyNoInteractions(service);
        when(sender.hasPermission("fmfl.chatclear.use")).thenReturn(true);
        command.onCommand(sender, null, "cc", new String[] {"silent"});
        verify(service).clearGlobal(config.current(), "Console", true);
        when(sender.hasPermission("fmfl.chatclear.self")).thenReturn(true);
        command.onCommand(sender, null, "cc", new String[] {"self"});
        verify(service, never()).clearSelf(any(), any());
        assertEquals(
                List.of("silent"), command.onTabComplete(sender, null, "cc", new String[] {""}));
        when(sender.hasPermission("fmfl.chatclear.reload")).thenReturn(true);
        assertEquals(
                List.of("reload"), command.onTabComplete(sender, null, "cc", new String[] {"RE"}));
        assertTrue(command.onTabComplete(sender, null, "cc", new String[] {"", ""}).isEmpty());
        command.onCommand(sender, null, "cc", new String[] {"bad"});
        verify(service, times(1)).clearGlobal(any(), anyString(), anyBoolean());
    }

    @Test
    void consoleGlobalAndReloadFeedback() throws Exception {
        Path path = Path.of("build/command-reload.yml");
        var config = configuration(path);
        var service = mock(ChatClearService.class);
        var handler = new ChatClearCommand(config, service);
        CommandSender console = mock(CommandSender.class);
        when(console.getName()).thenReturn("Console");
        when(console.hasPermission("fmfl.chatclear.use")).thenReturn(true);
        when(console.hasPermission("fmfl.chatclear.reload")).thenReturn(true);
        handler.onCommand(console, null, "chatclear", new String[0]);
        verify(service).clearGlobal(config.current(), "Console", false);
        Files.writeString(path, "messages: [broken");
        try {
            var before = config.current();
            handler.onCommand(console, null, "chatclear", new String[] {"reload"});
            assertSame(before, config.current());
            verify(console).sendMessage(Messages.render(before, "reload-failure", "Console", 0));
            Files.writeString(path, "clear-lines: 3\nbypass-enabled: false\n");
            handler.onCommand(console, null, "chatclear", new String[] {"reload"});
            assertEquals(3, config.current().clearLines());
            verify(console)
                    .sendMessage(Messages.render(config.current(), "reload-success", "Console", 0));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void selfPermissionIsIndependentAndOfflineRejected() throws Exception {
        var config = configuration(Path.of("build/test-config.yml"));
        var service = mock(ChatClearService.class);
        var command = new ChatClearCommand(config, service);
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("User");
        when(player.hasPermission("fmfl.chatclear.self")).thenReturn(true);
        when(player.isOnline()).thenReturn(true);
        command.onCommand(player, null, "cc", new String[] {"self"});
        verify(service).clearSelf(player, config.current());
        assertEquals(List.of("self"), command.onTabComplete(player, null, "cc", new String[] {""}));
        clearInvocations(service);
        when(player.isOnline()).thenReturn(false);
        command.onCommand(player, null, "cc", new String[] {"self"});
        verifyNoInteractions(service);
    }
}
