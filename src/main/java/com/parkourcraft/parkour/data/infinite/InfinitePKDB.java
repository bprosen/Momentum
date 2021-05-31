package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InfinitePKDB {

    public static void loadLeaderboard() {

        new BukkitRunnable() {
            @Override
            public void run() {

                LinkedHashMap<String, Integer> leaderboard = Parkour.getInfinitePKManager().getLeaderboard();
                List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "uuid, infinitepk_score",
                        " ORDER BY infinitepk_score DESC" +
                                " LIMIT " + leaderboard.size());

                for (Map<String, String> scoreResult : scoreResults)
                    leaderboard.put(scoreResult.get("uuid"), Integer.parseInt(scoreResult.get("infinitepk_score")));
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public static int getScore(String playerUUID) {
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "infinitepk_score",
                                        " WHERE uuid='" + playerUUID + "'");

        for (Map<String, String> scoreResult : scoreResults)
            return Integer.parseInt(scoreResult.get("infinitepk_score"));

        return 0;
    }

    public static int getScoreFromName(String playerName) {
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "infinitepk_score",
                " WHERE player_name='" + playerName + "'");

        for (Map<String, String> scoreResult : scoreResults)
            return Integer.parseInt(scoreResult.get("infinitepk_score"));

        return 0;
    }

    public static boolean hasScore(String playerName) {
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "infinitepk_score",
                " WHERE player_name='" + playerName + "'");

        if (!scoreResults.isEmpty() || Integer.parseInt(scoreResults.get(0).get("infinitepk_score")) > 0)
            return true;
        return false;
    }
}
