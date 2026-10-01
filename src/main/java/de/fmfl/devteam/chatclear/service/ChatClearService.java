package de.fmfl.devteam.chatclear.service;

import de.fmfl.devteam.chatclear.config.Settings;
import de.fmfl.devteam.chatclear.message.Messages;
import java.util.Collection;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class ChatClearService {
    private final Supplier<? extends Collection<? extends Player>> players;

    public ChatClearService(Supplier<? extends Collection<? extends Player>> players) {
        this.players = players;
    }

    public void clearSelf(Player player, Settings settings) {
        if (!player.isOnline()) {
            return;
        }
        for (int i = 0; i < settings.clearLines(); i++) {
            player.sendMessage(Component.empty());
        }
    }

    public int clearGlobal(Settings settings, String actor, boolean silent) {
        int count = 0;
        for (Player player : players.get()) {
            if (!player.isOnline()
                    || (settings.bypassEnabled()
                            && player.hasPermission("fmfl.chatclear.bypass"))) {
                continue;
            }
            clearSelf(player, settings);
            if (!silent) {
                player.sendMessage(Messages.render(settings, "announcement", actor, 0));
            }
            count++;
        }
        return count;
    }
}
