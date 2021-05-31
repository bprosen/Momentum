package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.infinite.InfinitePKDB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfiniteCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length >= 1 && a[0].equalsIgnoreCase("score")) {
            if (a.length == 2) {

                if (InfinitePKDB.hasScore(a[1])) {

                    int score = InfinitePKDB.getScoreFromName(a[1]);
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has a score of &6" + Utils.formatNumber(score)));
                } else {
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has not played &6Infinite Parkour &7before (score of 0)"));
                }

            } else if (a.length == 1) {

                if (InfinitePKDB.hasScore(player.getName())) {

                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    sender.sendMessage(Utils.translate("&7You have a score of &6" + Utils.formatNumber(playerStats.getInfinitePKScore())));
                } else {
                    sender.sendMessage(Utils.translate("&7You have yet to play &6Infinite Parkour &7(score of 0)"));
                }
            }
        }
        return false;
    }
}
