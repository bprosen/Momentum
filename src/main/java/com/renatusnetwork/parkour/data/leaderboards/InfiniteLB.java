package com.renatusnetwork.parkour.data.leaderboards;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class InfiniteLB
{
    private InfiniteType type;
    private ArrayList<InfiniteLBPosition> leaderboard;

    public InfiniteLB(InfiniteType type)
    {
        this.type = type;

        int maxSize = 10;
        SettingsManager settingsManager = Parkour.getSettingsManager();

        switch (type)
        {
            case CLASSIC:
                maxSize = settingsManager.infinite_classic_lb_size;
                break;
            case SPEEDRUN:
                maxSize = settingsManager.infinite_speedrun_lb_size;
                break;
            case SPRINT:
                maxSize = settingsManager.infinite_sprint_lb_size;
                break;
            case TIMED:
                maxSize = settingsManager.infinite_timed_lb_size;
                break;
        }

        this.leaderboard = new ArrayList<>(maxSize);
        loadLeaderboard();
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
                                    " LIMIT " + Parkour.getSettingsManager().max_infinite_leaderboard_size);

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
        }.runTaskAsynchronously(Parkour.getPlugin());
    }
}
