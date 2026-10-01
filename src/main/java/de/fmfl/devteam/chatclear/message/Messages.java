package de.fmfl.devteam.chatclear.message;

import de.fmfl.devteam.chatclear.config.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

public final class Messages {
    private Messages() {}

    public static Component render(Settings settings, String key, String player, int count) {
        return MiniMessage.miniMessage()
                .deserialize(
                        settings.messages().getOrDefault(key, ""),
                        Placeholder.unparsed("player", player),
                        Placeholder.unparsed("count", Integer.toString(count)));
    }

    public static void send(CommandSender sender, Settings settings, String key, int count) {
        sender.sendMessage(render(settings, key, sender.getName(), count));
    }
}
