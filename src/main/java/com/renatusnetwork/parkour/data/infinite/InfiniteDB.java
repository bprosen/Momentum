package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.List;
import java.util.Map;

public class InfiniteDB {

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
