package de.fmfl.devteam.chatclear;

import de.fmfl.devteam.chatclear.command.ChatClearCommand;
import de.fmfl.devteam.chatclear.config.ConfigurationService;
import de.fmfl.devteam.chatclear.service.ChatClearService;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatClearPlugin extends JavaPlugin {
    @Override public void onEnable() {
        saveDefaultConfig();
        YamlConfiguration defaults;
        try (var reader = new InputStreamReader(Objects.requireNonNull(getResource("config.yml")), StandardCharsets.UTF_8)) {
            defaults = YamlConfiguration.loadConfiguration(reader);
        } catch (java.io.IOException e) { throw new IllegalStateException("Cannot read bundled configuration", e); }
        var configuration = new ConfigurationService(new File(getDataFolder(), "config.yml"), defaults, getLogger());
        configuration.reload();
        var handler = new ChatClearCommand(configuration, new ChatClearService(() -> getServer().getOnlinePlayers()));
        var command = Objects.requireNonNull(getCommand("chatclear"), "Missing chatclear command in plugin.yml");
        command.setExecutor(handler);
        command.setTabCompleter(handler);
        getLogger().info("ChatClear enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatClear disabled.");
    }
}
