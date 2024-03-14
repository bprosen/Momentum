package com.renatusnetwork.momentum.data.leaderboards;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class InfiniteLB
{
    private InfiniteType type;
    private ArrayList<InfiniteLBPosition> leaderboard;

    public InfiniteLB(InfiniteType type)
    {
        this.type = type;
        this.leaderboard = new ArrayList<>(parseMaxSize());
        loadLeaderboard();
    }

    private int parseMaxSize()
    {
        SettingsManager settingsManager = Momentum.getSettingsManager();

        switch (type)
        {
            case CLASSIC:
                return settingsManager.infinite_classic_lb_size;
            case SPEEDRUN:
                return settingsManager.infinite_speedrun_lb_size;
            case SPRINT:
                return settingsManager.infinite_sprint_lb_size;
            case TIMED:
                return settingsManager.infinite_timed_lb_size;
        }
        return 10;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public int size()
    {
        return leaderboard.size();
    }

    public InfiniteLBPosition getLeaderboardPosition(int position)
    {
        return leaderboard.get(position);
    }

    public ArrayList<InfiniteLBPosition> getLeaderboardPositions()
    {
        return leaderboard;
    }

    public void loadLeaderboard()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    leaderboard.clear();

                    String typeDBName = "infinite_" + type.toString().toLowerCase() + "_score";

                    List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                            DatabaseManager.PLAYERS_TABLE,
                            "uuid, name, " + typeDBName,
                            "WHERE " + typeDBName + " > 0" +
                                    " ORDER BY " + typeDBName + " DESC" +
                                    " LIMIT " + parseMaxSize());

                    for (Map<String, String> scoreResult : scoreResults)
                        leaderboard.add(
                                new InfiniteLBPosition(
                                        scoreResult.get("uuid"),
                                        scoreResult.get("name"),
                                        Integer.parseInt(scoreResult.get(typeDBName)))
                        );
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }
}
