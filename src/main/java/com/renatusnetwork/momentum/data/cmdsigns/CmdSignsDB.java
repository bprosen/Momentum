package com.renatusnetwork.momentum.data.cmdsigns;

import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
			String id = result.get("name");
			String command = result.get("command");
			World world = Bukkit.getWorld(result.get("world"));
			double x = Double.parseDouble(result.get("x"));
			double y = Double.parseDouble(result.get("y"));
			double z = Double.parseDouble(result.get("z"));

			CommandSign csign = new CommandSign(id, command, new Location(world, x, y, z));
			csign.loadUsages(loadObtainedCommandSigns(id));
		}

		return temp;
	}

	private static Set<String> loadObtainedCommandSigns(String name) {
		List<Map<String, String>> results = DatabaseQueries.getResults(
				DatabaseManager.OBTAINED_COMMAND_SIGNS,
				"uuid",
				" WHERE name = ?",
				name
		);

		Set<String> temp = new HashSet<>();
		if (results.isEmpty())
			return temp;

		for (Map<String, String> result : results)
			temp.add(result.get("uuid"));

		return temp;
	}

	public static void insertCommandSign(String name, String command, String world, double x, double y, double z) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.COMMAND_SIGNS + " (name, command, world, x, y, z) VALUES (?, ?, ?, ?, ?)",
				name, command, world, x, y, z
		);
	}

	public static void insertObtainedCommandSign(String uuid, String name) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.OBTAINED_COMMAND_SIGNS + " (uuid, name) VALUES (?, ?)",
				uuid, name
		);
	}

	public static void deleteCommandSign(String name) {
		DatabaseQueries.runAsyncQuery(
				"DELETE FROM " + DatabaseManager.COMMAND_SIGNS + " WHERE name = ?",
				name
		);
	}

	public static void unobtainCommandSign(String uuid, String name) {
		DatabaseQueries.runAsyncQuery(
				"DELETE FROM " + DatabaseManager.OBTAINED_COMMAND_SIGNS + " WHERE uuid = ? AND name = ?",
				uuid, name
		);
	}

	public static void updateCommand(String name, String newCommand) {
		DatabaseQueries.runAsyncQuery(
				"UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET command = " + newCommand + " WHERE name = ?",
				name
		);
	}
}
