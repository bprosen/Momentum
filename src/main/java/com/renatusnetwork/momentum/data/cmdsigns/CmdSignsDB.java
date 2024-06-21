package com.renatusnetwork.momentum.data.cmdsigns;

import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;

import java.util.*;

public class CmdSignsDB {
	public static Map<String, String> loadCommandSigns() {
		List<Map<String, String>> results = DatabaseQueries.getResults(
				DatabaseManager.COMMAND_SIGNS,
				"sign_id, command",
				""
		);

		Map<String, String> temp = new HashMap<>();

		for (Map<String, String> result : results) {
			String id = result.get("sign_id");
			String command = result.get("command");
			temp.put(id, command);
		}

		return temp;
	}

	public static Map<String, List<String>> loadObtainedCommandSigns() {
		List<Map<String, String>> results = DatabaseQueries.getResults(
				DatabaseManager.OBTAINED_COMMAND_SIGNS,
				"*",
				""
		);

		Map<String, List<String>> temp = new HashMap<>();

		for (Map<String, String> result : results) {
			String sign_id = result.get("sign_id");
			String uuid = result.get("uuid");

			temp.computeIfAbsent(sign_id, id -> new ArrayList<>()).add(uuid);
		}

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

	public static String getSignLocation(String signID) {
		Map<String, String> result = DatabaseQueries.getResult(
				DatabaseManager.COMMAND_SIGNS,
				"world, x, y, z",
				" WHERE sign_id = ?",
				signID
		);

		String world = result.get("world");
		String x = result.get("x");
		String y = result.get("y");
		String z = result.get("z");

		return world + "(" + x + ", " + y + ", " + z + ")";
	}

	public static String getSignID(String world, double x, double y, double z) {
		Map<String, String> result = DatabaseQueries.getResult(
				DatabaseManager.COMMAND_SIGNS,
				"sign_id",
				" WHERE world = ? AND x = ? AND y = ? AND z = ?",
				world, x, y, z
		);

		return result.getOrDefault("sign_id", null);
	}

	public static void updateCommand(String signID, String newCommand) {
		DatabaseQueries.runAsyncQuery(
				"UPDATE " + DatabaseManager.COMMAND_SIGNS + " SET command = " + newCommand + " WHERE sign_id = ?",
				signID
		);
	}
}
