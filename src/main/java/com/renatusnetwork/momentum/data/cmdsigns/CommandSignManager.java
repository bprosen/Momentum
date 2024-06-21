package com.renatusnetwork.momentum.data.cmdsigns;

import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class CommandSignManager {
	private Map<String, String> cmdSigns; // sign id mapped to command
	private Map<String, List<String>> obtainedCmdSigns; // sign id mapped to players uuids

	public CommandSignManager() {
		cmdSigns = CmdSignsDB.loadCommandSigns();
		obtainedCmdSigns = CmdSignsDB.loadObtainedCommandSigns();
	}

	public void obtainCommandSign(PlayerStats playerStats, String signID) {
		obtainedCmdSigns.computeIfAbsent(signID, id -> new ArrayList<>()).add(playerStats.getUUID());
		CmdSignsDB.insertObtainedCommandSign(playerStats.getUUID(), signID);
	}

	public void unobtainCommandSign(PlayerStats playerStats, String signID) {
		obtainedCmdSigns.get(signID).remove(playerStats.getUUID());
		CmdSignsDB.unobtainCommandSign(playerStats.getUUID(), signID);
	}

	public void addCommandSign(String signID, String command, World world, double x, double y, double z) {
		cmdSigns.put(signID, command);
		CmdSignsDB.insertCommandSign(signID, command, world.getName(), x, y, z);
	}

	public void deleteCommandSign(String signID) {
		cmdSigns.remove(signID);
		obtainedCmdSigns.remove(signID);
		CmdSignsDB.deleteCommandSign(signID);
	}

	public boolean commandSignExists(String signID) {
		return cmdSigns.containsKey(signID);
	}

	public String getSignCommand(String signID) {
		return cmdSigns.get(signID);
	}

	public String getSignIDFromLocation(Location location) {
		return CmdSignsDB.getSignID(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
	}

	public boolean hasCommandSign(PlayerStats playerStats, String signID) {
		List<String> uuids = obtainedCmdSigns.get(signID);
		return uuids != null && uuids.contains(playerStats.getUUID());
	}

	public Map<String, String> getCommandSigns() {
		Map<String, String> temp = new HashMap<>();
		for (String id : cmdSigns.keySet())
			temp.put(id, CmdSignsDB.getSignLocation(id));

		return temp;
	}
}
