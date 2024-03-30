package com.renatusnetwork.momentum.data.levels;

import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
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
                "DELETE FROM " + DatabaseManager.LEVEL_RATINGS_TABLE + " WHERE uuid IN (SELECT uuid FROM " + DatabaseManager.PLAYERS_TABLE + " WHERE name=?) AND level_name=?",
                    playerName,
                    level.getName()
        );
    }
}
