package com.renatusnetwork.parkour.data.races;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.*;
import com.renatusnetwork.parkour.data.races.gamemode.ChoosingLevel;
import com.renatusnetwork.parkour.data.races.gamemode.Race;
import com.renatusnetwork.parkour.data.races.gamemode.RaceRequest;
import com.renatusnetwork.parkour.data.stats.*;
import com.renatusnetwork.parkour.data.leaderboards.RaceLBPosition;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RaceManager
{
    private Set<RaceRequest> raceRequests;
    private HashMap<Integer, RaceLBPosition> raceLeaderboard;
    private HashMap<String, ChoosingLevel> choosingLevel;

    public RaceManager()
    {
        this.raceRequests = new HashSet<>();
        this.raceLeaderboard = new HashMap<>(Parkour.getSettingsManager().max_race_leaderboard_size);
        this.choosingLevel = new HashMap<>();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 60 * 60, 20 * 60 * 180);
    }

    /*
        Race Requests Section
     */
    public void sendRequest(PlayerStats sender, PlayerStats requested, Level level, int bet)
    {
        RaceRequest raceRequest = new RaceRequest(sender, requested, level, bet);
        addRequest(raceRequest);

        raceRequest.send();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                RaceRequest request = getRequest(sender, requested);

                if (request != null)
                {
                    removeRequest(raceRequest);

                    if (sender != null)
                        sender.sendMessage(Utils.translate("&4" + requested.getDisplayName() + "&c did not accept your race request in time"));
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 30);
    }

    public void acceptRequest(PlayerStats sender, PlayerStats requested)
    {

        RaceRequest raceRequest = getRequest(sender, requested);

        // request exists
        if (raceRequest != null)
            raceRequest.accept();
        else
            sender.sendMessage(Utils.translate("&cYou do not have a request from &4" + requested.getName()));
    }

    public RaceRequest getRequest(PlayerStats sender, PlayerStats requested)
    {
        for (RaceRequest raceRequest : raceRequests)
        {
            if (raceRequest.equals(sender, requested))
                return raceRequest;
        }
        return null;
    }

    public void removeRequest(RaceRequest raceRequest)
    {
        raceRequests.remove(raceRequest);
    }

    public void addRequest(RaceRequest request)
    {
        raceRequests.add(request);
    }

    public void addChoosingRaceLevel(PlayerStats sender, PlayerStats requested, int bet)
    {
        choosingLevel.put(sender.getName(), new ChoosingLevel(sender, requested, bet));
    }

    public boolean containsChoosingRaceLevel(String name)
    {
        return choosingLevel.containsKey(name);
    }

    public void removeChoosingRaceLevel(String name)
    {
        choosingLevel.remove(name);
    }

    public ChoosingLevel getChoosingLevelData(PlayerStats playerStats)
    {
        return choosingLevel.get(playerStats.getName());
    }

    /*
        Leaderboard Section
     */
    public void loadLeaderboard()
    {
        try
        {
            raceLeaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    DatabaseManager.PLAYERS_TABLE,
                    "name, race_wins, race_losses",
                    "WHERE race_wins > 0 " +
                            "ORDER BY race_wins DESC " +
                            "LIMIT " + Parkour.getSettingsManager().max_race_leaderboard_size);

            int leaderboardPos = 1;

            for (Map<String, String> scoreResult : scoreResults)
            {
                int wins = Integer.parseInt(scoreResult.get("race_wins"));
                int losses = Integer.parseInt(scoreResult.get("race_losses"));

                // avoid divided by 0 error
                float winRate;
                if (losses > 0)
                    winRate = Float.parseFloat(Utils.formatDecimal((double) wins / losses));
                else
                    winRate = wins;

                raceLeaderboard.put(leaderboardPos,
                        new RaceLBPosition(
                                scoreResult.get("name"),
                                wins,
                                winRate
                        )
                );
                leaderboardPos++;
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public HashMap<Integer, RaceLBPosition> getLeaderboard() { return raceLeaderboard; }
}
