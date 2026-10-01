package de.fmfl.devteam.chatclear.command;

import de.fmfl.devteam.chatclear.config.ConfigurationService;
import de.fmfl.devteam.chatclear.config.Settings;
import de.fmfl.devteam.chatclear.message.Messages;
import de.fmfl.devteam.chatclear.service.ChatClearService;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ChatClearCommand implements CommandExecutor, TabCompleter {
    private final ConfigurationService config;
    private final ChatClearService service;
    public ChatClearCommand(ConfigurationService config, ChatClearService service) {
        this.config = config;
        this.service = service;
    }
    private boolean allowed(CommandSender sender, String mode) {
        return switch (mode) {
            case "self" -> sender.hasPermission("fmfl.chatclear.self");
            case "silent" -> sender.hasPermission("fmfl.chatclear.use") && sender.hasPermission("fmfl.chatclear.silent");
            case "reload" -> sender.hasPermission("fmfl.chatclear.reload");
            default -> sender.hasPermission("fmfl.chatclear.use");
        };
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Settings settings = config.current();
        String mode = args.length == 0 ? "global" : args[0].toLowerCase(Locale.ROOT);
        if (args.length > 1 || (!mode.equals("global") && !List.of("self", "silent", "reload").contains(mode))
                || (args.length > 0 && mode.equals("global"))) {
            Messages.send(sender, settings, "usage", 0);
            return true;
        }
        if (!allowed(sender, mode)) { Messages.send(sender, settings, "no-permission", 0); return true; }
        switch (mode) {
            case "reload" -> {
                boolean success = config.reload();
                Messages.send(sender, config.current(), success ? "reload-success" : "reload-failure", 0);
            }
            case "self" -> {
                if (!(sender instanceof Player player) || !player.isOnline()) {
                    Messages.send(sender, settings, "player-only", 0);
                    return true;
                }
                service.clearSelf(player, settings);
                Messages.send(sender, settings, "self-success", 0);
            }
            default -> {
                boolean silent = mode.equals("silent");
                int count = service.clearGlobal(settings, sender.getName(), silent);
                Messages.send(sender, settings, silent ? "silent-success" : "global-success", count);
            }
        }
        return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return List.of("self", "silent", "reload").stream()
                .filter(mode -> mode.startsWith(prefix) && allowed(sender, mode))
                .filter(mode -> !mode.equals("self") || sender instanceof Player player && player.isOnline()).toList();
    }
}
