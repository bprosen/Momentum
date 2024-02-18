package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.races.RaceManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RaceCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        MenuManager menuManager = Parkour.getMenuManager();
        StatsManager statsManager = Parkour.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);
        RaceManager raceManager = Parkour.getRaceManager();

        if (a.length == 0)
            sendHelp(player);
        else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
            sendHelp(player);
        else if ((a.length == 1 || a.length == 2) && a[0].equalsIgnoreCase("random"))
        {
            int bet = 0;
            if (a.length == 2)
            {
                if (Utils.isInteger(a[1]))
                    bet = Integer.parseInt(a[1]);
                else
                {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not a valid integer"));
                    return false;
                }
            }

            Collection<PlayerStats> collection = Parkour.getStatsManager().getOnlinePlayers();
            PlayerStats opponentStats;

            synchronized (collection)
            {
                List<PlayerStats> list = new ArrayList<>();

                for (PlayerStats onlineStats : collection)
                    if (!onlineStats.equals(playerStats))
                        list.add(onlineStats);

                opponentStats = list.get(ThreadLocalRandom.current().nextInt(0, list.size()));
            }

            if (opponentStats != null)
            {
                // open menu if meets conditions
                raceManager.addChoosingRaceLevel(playerStats, opponentStats, bet);
                menuManager.openInventory(playerStats, "race_levels", true);
            }
            else
                sender.sendMessage(Utils.translate("&cCould not find an opponent"));

        }
        else if (a.length == 1)
        {
            PlayerStats targetStats = statsManager.getByName(a[0]);

            if (targetStats != null)
            {
                if (!targetStats.equals(playerStats))
                {
                    // open menu if meets conditions
                    raceManager.addChoosingRaceLevel(playerStats, targetStats, 0);
                    menuManager.openInventory(playerStats, "race_levels", true);
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot race yourself"));
            }
            else
                player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
        }
        else if (a.length == 2 && a[0].equalsIgnoreCase("accept"))
        {
            PlayerStats targetStats = statsManager.getByName(a[1]);

            if (targetStats != null)
                // accept race request
                Parkour.getRaceManager().acceptRequest(playerStats, targetStats);
            else
                player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
        }
        else if (a.length == 2)
        {
            // send race request with bet
            if (Utils.isInteger(a[1]))
            {
                int bet = Integer.parseInt(a[1]);
                int minBet = Parkour.getSettingsManager().min_race_bet_amount;

                if (bet >= minBet)
                {
                    PlayerStats targetStats = statsManager.getByName(a[0]);

                    if (targetStats != null)
                    {
                        if (!targetStats.equals(playerStats))
                        {
                            // open menu if meets conditions
                            raceManager.addChoosingRaceLevel(playerStats, targetStats, bet);
                            menuManager.openInventory(playerStats, "race_levels", true);
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot race yourself"));
                    }
                    else
                        player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot bet less than &6" + Utils.formatNumber(minBet) + " &eCoins"));
            }
            else
                player.sendMessage(Utils.translate("&cThat is not a valid amount to bet!"));
        }
        else
            sendHelp(player);

        return false;
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&4&lRace Command Help"));
        player.sendMessage(Utils.translate("&c/race help  &7Displays this page"));
        player.sendMessage(Utils.translate("&c/race random  &7Races someone random"));
        player.sendMessage(Utils.translate("&c/race random (Bet)  &7Races someone random with a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN)  &7Send race request without a bet"));
        player.sendMessage(Utils.translate("&c/race (IGN) (Bet)  &7Send race request with a bet"));
        player.sendMessage(Utils.translate("&c/race accept (IGN)  &7Accept pending race request"));
    }
}
