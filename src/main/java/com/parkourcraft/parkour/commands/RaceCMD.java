package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsManager;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class RaceCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        RaceManager raceManager = Parkour.getRaceManager();
        StatsManager statsManager = Parkour.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);

        if (a.length == 0) {
            sendHelp(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendHelp(player);
        } else if (a.length == 1) {
            PlayerStats targetStats = statsManager.getByNameIgnoreCase(a[0]);

            if (targetStats != null) {
                // send race request
                raceManager.sendRequest(playerStats, targetStats, null, false, 0.0);
            } else {
                player.sendMessage(Utils.translate("&4" + targetStats.getPlayerName() + " &cis not online"));
            }
        } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {
            PlayerStats targetStats = statsManager.getByNameIgnoreCase(a[1]);

            if (targetStats != null) {
                // accept race request
                raceManager.acceptRequest(playerStats, targetStats);
            } else {
                player.sendMessage(Utils.translate("&4" + targetStats.getPlayerName() + " &cis not online"));
            }
        } else if (a.length == 2) {
            // send race request with bet
            if (Utils.isDouble(a[1])) {
                double betAmount = Double.parseDouble(a[1]);
                PlayerStats targetStats = statsManager.getByNameIgnoreCase(a[0]);

                if (targetStats != null) {
                    // send race request
                    raceManager.sendRequest(playerStats, targetStats, null, true, betAmount);
                } else {
                    player.sendMessage(Utils.translate("&4" + targetStats.getPlayerName() + " &cis not online"));
                }
            } else {
                player.sendMessage(Utils.translate("&cThat is not a valid amount to bet!"));
            }
        } else {
            sendHelp(player);
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&4&lRace Command Help"));
        player.sendMessage(Utils.translate("&c/race help &4- &7Displays this page"));
        player.sendMessage(Utils.translate("&c/race (IGN) &4- &7Send race request without a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN) (Bet) &4- &7Send race request with a bet"));
        player.sendMessage(Utils.translate("&c/race accept (IGN) &4- &7Accept pending race request"));
    }
}
