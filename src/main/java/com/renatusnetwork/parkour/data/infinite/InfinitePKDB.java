package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.List;
import java.util.Map;

public class InfinitePKDB {

    public static int getScoreFromName(String playerName) {
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "infinitepk_score",
                " WHERE player_name='?'", playerName);

        for (Map<String, String> scoreResult : scoreResults)
            return Integer.parseInt(scoreResult.get("infinitepk_score"));

        return 0;
    }

    public static boolean hasScore(String playerName) {
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", "infinitepk_score",
                " WHERE player_name='?'", playerName);

        return !scoreResults.isEmpty();
    }
}
