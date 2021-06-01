package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfinitePKDB {

    public static void loadLeaderboard() {

        new BukkitRunnable() {
            @Override
            public void run() {

                Set<InfinitePKLBPosition> leaderboard = Parkour.getInfinitePKManager().getLeaderboard();
                List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                       "players",
                        "uuid, player_name, infinitepk_score",
                       " WHERE infinitepk_score > 0" +
                                " ORDER BY infinitepk_score DESC" +
                                " LIMIT " + Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

                for (Map<String, String> scoreResult : scoreResults) {
                    leaderboard.add(
                            new InfinitePKLBPosition(
                                    scoreResult.get("uuid"),
                                    scoreResult.get("player_name"),
                                    Integer.parseInt(scoreResult.get("infinitepk_score")),
                                    leaderboard.size() + 1)
                    );
                }
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

        if (scoreResults.isEmpty())
            return false;
        return true;
    }
}
