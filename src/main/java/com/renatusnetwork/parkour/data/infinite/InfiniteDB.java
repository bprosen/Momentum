package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.data.infinite.types.InfiniteType;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.List;
import java.util.Map;

public class InfiniteDB
{

    public static int getScoreFromName(InfiniteType type, String playerName)
    {
        String dbTypeName = "infinite_" + type.toString().toLowerCase() + "_score";
        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", dbTypeName,
                " WHERE player_name='" + playerName + "'");

        for (Map<String, String> scoreResult : scoreResults)
            return Integer.parseInt(scoreResult.get(dbTypeName));

        return 0;
    }

    public static boolean hasScore(InfiniteType type, String playerName)
    {
        String dbTypeName = "infinite_" + type.toString().toLowerCase() + "_score";

        List<Map<String, String>> scoreResults = DatabaseQueries.getResults("players", dbTypeName,
                " WHERE player_name='" + playerName + "'");

        return !scoreResults.isEmpty();
    }
}
