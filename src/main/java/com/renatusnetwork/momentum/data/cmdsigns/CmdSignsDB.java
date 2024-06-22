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
			String id = result.get("sign_id");
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

	private static Set<String> loadObtainedCommandSigns(String signID) {
		List<Map<String, String>> results = DatabaseQueries.getResults(
				DatabaseManager.OBTAINED_COMMAND_SIGNS,
				"uuid",
				" WHERE sign_id = ?",
				signID
		);

		Set<String> temp = new HashSet<>();
		if (results.isEmpty())
			return temp;

		for (Map<String, String> result : results)
			temp.add(result.get("uuid"));

		return temp;
	}

	public static void insertCommandSign(String signID, String command, String world, double x, double y, double z) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.COMMAND_SIGNS + " VALUES (?, ?, ?, ?, ?)",
				signID, command, world, x, y, z
		);
	}

	public static void insertObtainedCommandSign(String uuid, String signID) {
		DatabaseQueries.runAsyncQuery(
				"INSERT INTO " + DatabaseManager.OBTAINED_COMMAND_SIGNS + " VALUES (?, ?)",
				uuid, signID
		);
	}

	public static void deleteCommandSign(String signID) {
		DatabaseQueries.runAsyncQuery(
				"DELETE FROM " + DatabaseManager.COMMAND_SIGNS + " WHERE sign_id = ?",
				signID
		);
	}

	public static void unobtainCommandSign(String uuid, String signID) {
		DatabaseQueries.runAsyncQuery(
				"DELETE FROM " + DatabaseManager.OBTAINED_COMMAND_SIGNS + " WHERE uuid = ? AND sign_id = ?",
				uuid, signID
		);
	}

	public static void updateCommand(String signID, String newCommand) {
		DatabaseQueries.runAsyncQuery(
				"UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET command = " + newCommand + " WHERE sign_id = ?",
				signID
		);
	}
}
