package com.renatusnetwork.momentum.data.cmdsigns;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class CommandSignManager {
	private final Map<String, CommandSign> cmdSigns; // sign id mapped to command sign object
	private final Map<Location, CommandSign> locations; // sign location mapped to command sign object

	public CommandSignManager() {
		cmdSigns = CmdSignsDB.loadCommandSigns();
		locations = new HashMap<>();
		for (CommandSign csign : cmdSigns.values())
			locations.put(csign.getLocation(), csign);
	}

	public void obtainCommandSign(String uuid, String signID) {
		cmdSigns.get(signID).addUsage(uuid);
		CmdSignsDB.insertObtainedCommandSign(uuid, signID);
	}

	public void unobtainCommandSign(String uuid, String signID) {
		cmdSigns.get(signID).removeUsage(uuid);
		CmdSignsDB.unobtainCommandSign(uuid, signID);
	}

	public void addCommandSign(String signID, String command, World world, double x, double y, double z) {
		Location loc = new Location(world, x, y, z);
		CommandSign csign =  new CommandSign(signID, command, loc);
		cmdSigns.put(signID, csign);
		locations.put(loc, csign);
		CmdSignsDB.insertCommandSign(signID, command, world.getName(), x, y, z);
	}

	public void deleteCommandSign(String signID) {
		locations.remove(cmdSigns.get(signID).getLocation());
		cmdSigns.remove(signID);
		CmdSignsDB.deleteCommandSign(signID);
	}

	public boolean commandSignExists(String signID) {
		return cmdSigns.containsKey(signID);
	}

	public boolean commandSignExists(Location location) {
		return locations.containsKey(location);
	}

	public void updateCommand(String signID, String newCommand) {
		cmdSigns.get(signID).updateCommand(newCommand);
		CmdSignsDB.updateCommand(signID, newCommand);
	}

	public CommandSign getCommandSign(Location location) {
		return locations.get(location);
	}

	public Collection<CommandSign> getCommandSigns() {
		return cmdSigns.values();
	}
}
