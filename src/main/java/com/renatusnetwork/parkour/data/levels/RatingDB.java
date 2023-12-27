package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingDB
{

    public static boolean hasRatedLevel(String raterUUID, String levelName)
    {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE,
                "level_name",
                "WHERE uuid=? AND level_name=?", raterUUID, levelName
        );

        return !ratingResults.isEmpty();
    }

    public static HashMap<String, Integer> getAllLevelRaters(String levelName)
    {
        HashMap<String, Integer> ratings = new HashMap<>();

        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE + " lr",
                "name, rating",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid WHERE level_name=?",
                levelName
        );

        for (Map<String, String> ratingResult : ratingResults)
            ratings.put(ratingResult.get("name"), Integer.parseInt(ratingResult.get("rating")));

        return ratings;
    }

    public static List<String> getSpecificLevelRaters(String levelName, int rating) {

        List<String> ratings = new ArrayList<>();

        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE + " lr",
                "name",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid WHERE level_name=? rating=?",
                levelName, rating
        );

        for (Map<String, String> ratingResult : ratingResults)
            ratings.add(ratingResult.get("name"));

        return ratings;
    }

    public static boolean hasRatedLevelFromName(String raterName, int levelID) {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE,
                "level_id",
                " WHERE player_name=?", raterName
        );

        boolean hasRated = false;

        for (Map<String, String> ratingResult : ratingResults) {
            if (Integer.parseInt(ratingResult.get("level_id")) == levelID) {
                hasRated = true;
                break;
            }
        }
        return hasRated;
    }

    public static int getRatingFromName(String playerName, String levelName)
    {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                DatabaseManager.LEVEL_RATINGS_TABLE + " lr",
                "rating",
                "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid WHERE name=? AND level_name=?",
                playerName, levelName
        );

        for (Map<String, String> ratingResult : ratingResults)
            return Integer.parseInt(ratingResult.get("rating"));

        return -1;
    }

    public static void addRating(Player player, Level level, int rating) {

        String query = "INSERT INTO " + DatabaseManager.LEVEL_RATINGS_TABLE + " " +
                       "(uuid, level_name, rating)" +
                       " VALUES('" +
                       player.getUniqueId().toString() + "','" +
                       level.getName() + "'," +
                       rating +
                       ")";

        DatabaseQueries.runAsyncQuery(query);
    }
}
