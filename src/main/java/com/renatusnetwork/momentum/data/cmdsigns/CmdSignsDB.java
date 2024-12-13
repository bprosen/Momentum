package com.renatusnetwork.momentum.data.cmdsigns;

import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.*;

public class CmdSignsDB {

    public static Map<String, CommandSign> loadCommandSigns() {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.COMMAND_SIGNS,
                "*",
                ""
        );

        Map<String, CommandSign> temp = new HashMap<>();

        for (Map<String, String> result : results) {
            String name = result.get("name");
            String command = result.get("command");
            String title = result.get("title");
            World world = Bukkit.getWorld(result.get("world"));
            int x = Integer.parseInt(result.get("x"));
            int y = Integer.parseInt(result.get("y"));
            int z = Integer.parseInt(result.get("z"));
            boolean broadcast = Integer.parseInt(result.get("broadcast")) == 1;

            temp.put(name, new CommandSign(name, title, command, new CmdSignLocation(world, x, y, z), broadcast));
        }

        return temp;
    }

    public static void loadUsedCommandSigns(PlayerStats playerStats) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.USED_COMMAND_SIGNS,
                "*",
                "WHERE uuid=?",
                playerStats.getUUID()
        );

        for (Map<String, String> result : results) {
            playerStats.addCommandSign(result.get("name"));
        }
    }

    public static void insertCommandSign(String name, String command, String world, int x, int y, int z) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.COMMAND_SIGNS + " (name, command, world, x, y, z) VALUES (?,?,?,?,?,?)",
                name, command, world, x, y, z
        );
    }

    public static void insertUsedCommandSign(String uuid, String name) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.USED_COMMAND_SIGNS + " (uuid, name) VALUES (?,?)",
                uuid, name
        );
    }

    public static void deleteCommandSign(String name) {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.COMMAND_SIGNS + " WHERE name=?",
                name
        );
    }

    public static void unuseCommandSign(String uuid, String name) {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.USED_COMMAND_SIGNS + " WHERE uuid=? AND name=?",
                uuid, name
        );
    }

    public static void updateCommand(String name, String newCommand) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET command=? WHERE name=?",
                newCommand, name
        );
    }

    public static void toggleBroadcast(String name) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET broadcast=NOT broadcast WHERE name=?", name
        );
    }

    public static void updateTitle(String name, String title) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET title=? WHERE name=?", title, name
        );
    }
}
