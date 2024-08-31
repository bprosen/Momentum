package com.renatusnetwork.momentum.data.cmdsigns;

import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.World;

import java.util.*;

public class CommandSignManager {

	private final Map<String, CommandSign> cmdSigns; // sign id mapped to command sign object
	private final Map<CmdSignLocation, CommandSign> locations; // sign location mapped to command sign object

	public CommandSignManager() {
		cmdSigns = CmdSignsDB.loadCommandSigns();
		locations = new HashMap<>();

		for (CommandSign csign : cmdSigns.values())
			locations.put(csign.getLocation(), csign);
	}

	public void useCommandSign(PlayerStats playerStats, String name) {
		playerStats.addCommandSign(name);
		CmdSignsDB.insertUsedCommandSign(playerStats.getUUID(), name);
	}

	public void addCommandSign(String name, String command, World world, int x, int y, int z) {
		CmdSignLocation loc = new CmdSignLocation(world, x, y, z);
		CommandSign csign =  new CommandSign(name, null, command, loc, false);

		cmdSigns.put(name, csign);
		locations.put(loc, csign);
		CmdSignsDB.insertCommandSign(name, command, world.getName(), x, y, z);
	}

	public void deleteCommandSign(String name) {
		locations.remove(cmdSigns.get(name).getLocation());
		cmdSigns.remove(name);
		CmdSignsDB.deleteCommandSign(name);
	}

	public void toggleBroadcast(String name) {
		CommandSign csign = cmdSigns.get(name);

		csign.toggleBroadcast();
		CmdSignsDB.toggleBroadcast(name);
	}

	public void updateTitle(String name, String title) {
		CommandSign csign = cmdSigns.get(name);

		csign.setTitle(title);
		CmdSignsDB.updateTitle(name, title);
	}
	public boolean commandSignExists(String name) {
		return cmdSigns.containsKey(name);
	}

	public boolean commandSignExists(CmdSignLocation location) {
		return locations.containsKey(location);
	}

	public void updateCommand(String name, String newCommand) {
		cmdSigns.get(name).updateCommand(newCommand);
		CmdSignsDB.updateCommand(name, newCommand);
	}

	public CommandSign getCommandSign(String name) {
		return cmdSigns.get(name);
	}

	public CommandSign getCommandSign(CmdSignLocation location) {
		return locations.get(location);
	}

	public Collection<CommandSign> getCommandSigns() {
		return cmdSigns.values();
	}
}
