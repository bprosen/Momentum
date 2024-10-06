package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        StatsManager statsManager = Momentum.getStatsManager();
        PlayerStats playerStats = a.length >= 1 ? statsManager.getByName(a[0]) : statsManager.get(player);

        if (playerStats != null && playerStats.isLoaded()) {
            Momentum.getMenuManager().openInventory(playerStats, player, "profile", true);
        } else {
            sender.sendMessage(Utils.translate("&4Target is not online or stats are not loaded yet"));
        }
        return false;
    }
}
