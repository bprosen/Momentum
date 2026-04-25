package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerToggleCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (a.length == 1 && a[0].equalsIgnoreCase("toggle")) {
            StatsManager statsManager = Momentum.getStatsManager();

            if (statsManager.containsHiddenPlayer(player)) {
                statsManager.togglePlayerHiderOff(player, true);
            } else if (!Momentum.getStatsManager().get(player).isEventParticipant()) {
                statsManager.togglePlayerHiderOn(player, true);
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            }
        }
        return true;
    }
}
