package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                // open menu if they meet requirements
                if (meetsRaceConditions(Parkour.getStatsManager().get(player), Parkour.getStatsManager().get(target), false, -1.0))
                    menuManager.openRaceLevelsGUI(player, target, -1.0);
            } else {
                player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
            }
        } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {
            PlayerStats targetStats = statsManager.getByName(a[1]);

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

                if (target != null) {
                    // open menu if meets conditions
                    if (meetsRaceConditions(Parkour.getStatsManager().get(player), Parkour.getStatsManager().get(target), true, betAmount))
                        menuManager.openRaceLevelsGUI(player, target, betAmount);
                } else {
                    player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
                }
            } else {
                player.sendMessage(Utils.translate("&cThat is not a valid amount to bet!"));
            }
        } else {
            sendHelp(player);
        }

        return false;
    }

    private boolean meetsRaceConditions(PlayerStats player1, PlayerStats player2, boolean bet, double betAmount) {

        if (player1.inRace()) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot send a request while in a race"));
            return false;
        }

        // if target is in race
        if (player2.inRace()) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot send a request while &4" + player2.getPlayerName() + " &cis in a race"));
            return false;
        }

        if (player1.getPlayerName().equalsIgnoreCase(player2.getPlayerName())) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race yourself..."));
            return false;
        }

        if (player1.isSpectating()) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
            return false;
        }

        if (player1.inPracticeMode()) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
            return false;
        }

        // make sure they have enough money for the bet
        double victimBalance = player2.getCoins();
        double senderBalance = player1.getCoins();

        if (bet && senderBalance < betAmount) {
            player1.getPlayer().sendMessage(Utils.translate("&7You do not have enough money for this bet!" +
                    " Your Balance &4$" + senderBalance));
            return false;
        }

        if (bet && victimBalance < betAmount) {
            player1.getPlayer().sendMessage(Utils.translate("&c" + player2.getPlayer().getName() + " &7does not have enough to do this bet" +
                    " - &cTheir Balance &4$" + victimBalance));
            return false;
        }

        double minBetAmount = Parkour.getSettingsManager().min_race_bet_amount;
        if (bet && betAmount < minBetAmount) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou cannot bet less than &4$" + minBetAmount));
            return false;
        }

        if (Parkour.getRaceManager().getRequest(player1.getPlayer(), player2.getPlayer()) != null) {
            player1.getPlayer().sendMessage(Utils.translate("&cYou have already sent a request to them!"));
            return false;
        }
        // if they pass all these checks, return true!
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&4&lRace Command Help"));
        player.sendMessage(Utils.translate("&c/race help &4- &7Displays this page"));
        player.sendMessage(Utils.translate("&c/race (IGN) &4- &7Send race request without a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN) (Bet) &4- &7Send race request with a bet"));
        player.sendMessage(Utils.translate("&c/race accept (IGN) &4- &7Accept pending race request"));
    }
}
