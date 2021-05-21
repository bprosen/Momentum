package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class RatingDB {

    public static float getAverageRating(int levelID) {

        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                "ratings",
                "rating",
                " WHERE level_id=" + levelID
        );

        if (!ratingResults.isEmpty()) {
            float totalRating = 0.0f;
            int totalRatings = 0;

            for (Map<String, String> ratingResult : ratingResults) {
                totalRatings++;
                totalRating += Double.parseDouble(ratingResult.get("rating"));
            }

            double averageRating = totalRating / totalRatings;
            double newAmount = Double.valueOf(new BigDecimal(averageRating).toPlainString());
            // this makes it seperate digits by commands and .2 means round decimal by 2 places
            return Float.parseFloat(String.format("%,.2f", newAmount));
        }
        return 0.00f;
    }

    public static int getTotalRatings(int levelID) {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                "ratings",
                "*",
                " WHERE level_id=" + levelID
        );

        if (ratingResults.isEmpty())
            return 0;
        else
            return ratingResults.size();
    }

    public static boolean hasRatedLevel(String raterUUID, int levelID) {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                "ratings",
                "level_id",
                " WHERE uuid='" + raterUUID + "'"
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

    public static boolean hasRatedLevelFromName(String raterName, int levelID) {
        List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                "ratings",
                "level_id",
                " WHERE player_name='" + raterName + "'"
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

    public static int getPlayerTotalRatings(String UUID, int levelID) {
        int total = 0;

        if (hasRatedLevel(UUID, levelID)) {
            List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                    "ratings",
                    "level_id",
                    " WHERE uuid='" + UUID + "' AND level_id=" + levelID
            );

            total = ratingResults.size();
        }
        return total;
    }

    public static int getRating(String UUID, int levelID) {
        if (hasRatedLevel(UUID, levelID)) {
            List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                    "ratings",
                    "rating",
                    " WHERE uuid='" + UUID + "' AND level_id=" + levelID
            );

            for (Map<String, String> ratingResult : ratingResults)
                return Integer.parseInt(ratingResult.get("rating"));
        }
        return 0;
    }

    public static int getRatingFromName(String playerName, int levelID) {
        if (hasRatedLevelFromName(playerName, levelID)) {
            List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                    "ratings",
                    "rating",
                    " WHERE player_name='" + playerName + "' AND level_id=" + levelID
            );

            for (Map<String, String> ratingResult : ratingResults)
                return Integer.parseInt(ratingResult.get("rating"));
        }
        return 0;
    }

    public static void removeRating(String UUID, int levelID, float rating, boolean async) {
        if (hasRatedLevel(UUID, levelID)) {
            String query = "DELETE FROM ratings WHERE uuid='" + UUID + "' AND level_id=" + levelID + " AND rating=" + rating;

            if (async)
                Parkour.getDatabaseManager().add(query);
            else
                Parkour.getDatabaseManager().run(query);
        }
    }

    public static void addRating(Player player, Level level, int rating) {

        String query = "INSERT INTO ratings " +
                       "(uuid, player_name, level_id, rating)" +
                       " VALUES('" +
                       player.getUniqueId().toString() + "','" +
                       player.getName() + "'," +
                       level.getID() + "," +
                       rating +
                       ")";

        Parkour.getDatabaseManager().add(query);
    }
}
