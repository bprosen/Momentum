package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsManager;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
        MenuManager menuManager = Parkour.getMenuManager();
        StatsManager statsManager = Parkour.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);

        if (a.length == 0) {
            sendHelp(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendHelp(player);
        } else if (a.length == 1) {
            Player target = Bukkit.getPlayer(a[0]);

            if (target != null) {
                // if they are in race
                if (Parkour.getStatsManager().get(player).inRace()) {
                    player.sendMessage(Utils.translate("&cYou cannot send a request while in a race"));
                    return true;
                }

                // if target is in race
                if (Parkour.getStatsManager().get(target).inRace()) {
                    player.sendMessage(Utils.translate("&cYou cannot send a request while &4" + target.getName() + " &cis in a race"));
                    return true;
                }
                // send race request
                menuManager.openRaceLevelsGUI(player, target, -1.0);
            } else {
                player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
            }
        } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {
            PlayerStats targetStats = statsManager.getByNameIgnoreCase(a[1]);

            if (targetStats != null) {
                // accept race request
                Parkour.getRaceManager().acceptRequest(playerStats, targetStats);
            } else {
                player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
        } else if (a.length == 2) {
            // send race request with bet
            if (Utils.isDouble(a[1])) {
                double betAmount = Double.parseDouble(a[1]);
                Player target = Bukkit.getPlayer(a[0]);

                double minBetAmount = Parkour.getSettingsManager().min_race_bet_amount;
                if (betAmount >= minBetAmount) {
                    if (target != null) {
                        // if they are in race
                        if (Parkour.getStatsManager().get(player).inRace()) {
                            player.sendMessage(Utils.translate("&cYou cannot send a request while in a race"));
                            return true;
                        }

                        // if target is in race
                        if (Parkour.getStatsManager().get(target).inRace()) {
                            player.sendMessage(Utils.translate("&cYou cannot send a request while &4" + target.getName() + " &cis in a race"));
                            return true;
                        }
                        // send race request
                        menuManager.openRaceLevelsGUI(player, target, betAmount);
                    } else {
                        player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot bet less than &4$" + minBetAmount));
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
