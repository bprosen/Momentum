package com.renatusnetwork.parkour.data.infinite.leaderboard;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfiniteLB
{
    private InfiniteType type;
    private HashMap<Integer, InfiniteLBPosition> leaderboard;

    public InfiniteLB(InfiniteType type)
    {
        this.type = type;
        this.leaderboard = new HashMap<>();
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

    public Collection<InfiniteLBPosition> getLeaderboardPositions()
    {
        return leaderboard.values();
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
                            "players",
                            "uuid, player_name, " + typeDBName,
                            " WHERE " + typeDBName + " > 0" +
                                    " ORDER BY " + typeDBName + " DESC" +
                                    " LIMIT " + Parkour.getSettingsManager().max_infinite_leaderboard_size);

                    int lbPos = 1;
                    for (Map<String, String> scoreResult : scoreResults)
                    {
                        leaderboard.put(lbPos,
                                new InfiniteLBPosition(
                                        scoreResult.get("uuid"),
                                        scoreResult.get("player_name"),
                                        Integer.parseInt(scoreResult.get(typeDBName)))
                        );
                        lbPos++;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }
}
