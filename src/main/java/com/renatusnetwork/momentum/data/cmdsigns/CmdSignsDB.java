package com.renatusnetwork.momentum.data.cmdsigns;

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
			World world = Bukkit.getWorld(result.get("world"));
			int x = Integer.parseInt(result.get("x"));
			int y = Integer.parseInt(result.get("y"));
			int z = Integer.parseInt(result.get("z"));

			CommandSign csign = new CommandSign(name, command, new CmdSignLocation(world, x, y, z));
			csign.loadUsages(loadObtainedCommandSigns(name));

			temp.put(csign.getName(), csign);
		}

		return temp;
	}

	private static Set<String> loadObtainedCommandSigns(String name) {
		List<Map<String, String>> results = DatabaseQueries.getResults(
				DatabaseManager.USED_COMMAND_SIGNS,
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

	public static void insertCommandSign(String name, String command, String world, int x, int y, int z) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.COMMAND_SIGNS + " (name, command, world, x, y, z) VALUES (?, ?, ?, ?, ?)",
				name, command, world, x, y, z
		);
	}

	public static void insertObtainedCommandSign(String uuid, String name) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.USED_COMMAND_SIGNS + " (uuid, name) VALUES (?, ?)",
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
				"DELETE FROM " + DatabaseManager.USED_COMMAND_SIGNS + " WHERE uuid = ? AND name = ?",
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
