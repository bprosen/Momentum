package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.entity.Player;

public class RatingDB
{
    public static void addRating(Player player, Level level, int rating)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.LEVEL_RATINGS_TABLE + " " +
                    "(uuid, level_name, rating) " +
                    "VALUES (?,?,?)",
                    player.getUniqueId().toString(),
                    level.getName(),
                    rating
        );
    }

    public static void updateRating(Player player, Level level, int rating)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.LEVEL_RATINGS_TABLE + " SET rating=? WHERE uuid=? AND level_name=?", rating, player.getUniqueId().toString(), level.getName()
        );
    }

    public static void removeRating(String playerName, Level level)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_RATINGS_TABLE + " lr " +
                    "JOIN " + DatabaseManager.PLAYERS_TABLE + " p ON p.uuid=lr.uuid " +
                    "WHERE p.name=? AND lr.level_name=?",
                    playerName,
                    level.getName()
        );
    }
}
