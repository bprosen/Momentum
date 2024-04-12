package com.renatusnetwork.momentum.data.races;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.*;
import com.renatusnetwork.momentum.data.races.gamemode.ChoosingLevel;
import com.renatusnetwork.momentum.data.races.gamemode.RaceRequest;
import com.renatusnetwork.momentum.data.stats.*;
import com.renatusnetwork.momentum.data.leaderboards.RaceLBPosition;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaceManager
{
    private Set<RaceRequest> raceRequests;
    private HashMap<String, ChoosingLevel> choosingLevel;

    private ArrayList<RaceLBPosition> raceLB;

    public RaceManager()
    {
        this.raceRequests = new HashSet<>();
        this.raceLB = new ArrayList<>(Momentum.getSettingsManager().max_race_leaderboard_size);
        this.choosingLevel = new HashMap<>();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 10, 20 * 180);
    }

    /*
        Race Requests Section
     */
    public void sendRequest(PlayerStats sender, PlayerStats requested, Level level, int bet)
    {
        RaceRequest alreadyExistsRequest = getRequest(sender, requested);

        // request exists
        if (alreadyExistsRequest == null)
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
            }.runTaskLater(Momentum.getPlugin(), 20 * 30);
        }
        else
            sender.sendMessage(Utils.translate("&cYou have already sent a request to " + requested.getDisplayName()));
    }

    public void acceptRequest(PlayerStats sender, PlayerStats requested)
    {
        RaceRequest raceRequest = getRequest(sender, requested);

        // request exists
        if (raceRequest != null)
            raceRequest.accept();
        else
            sender.sendMessage(Utils.translate("&cYou do not have a race request from &4" + requested.getName()));
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

    public ChoosingLevel getChoosingLevelData(String name)
    {
        return choosingLevel.get(name);
    }

    /*
        Leaderboard Section
     */
    public void loadLeaderboard()
    {
        try
        {
            raceLB.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    DatabaseManager.PLAYERS_TABLE,
                    "name, race_wins, race_losses",
                    "WHERE race_wins > 0 " +
                            "ORDER BY race_wins DESC " +
                            "LIMIT " + Momentum.getSettingsManager().max_race_leaderboard_size);

            for (Map<String, String> scoreResult : scoreResults)
            {
                int wins = Integer.parseInt(scoreResult.get("race_wins"));
                int losses = Integer.parseInt(scoreResult.get("race_losses"));

                // avoid divided by 0 error
                float winRate = losses > 0 ? Float.parseFloat(Utils.formatDecimal((double) wins / losses, true, 1, 2)) : wins;

                raceLB.add(
                        new RaceLBPosition(
                                scoreResult.get("name"),
                                wins,
                                winRate
                        ));
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public ArrayList<RaceLBPosition> getLeaderboard() { return raceLB; }
}
