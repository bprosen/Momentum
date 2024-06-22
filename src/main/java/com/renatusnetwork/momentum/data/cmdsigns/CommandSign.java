package com.renatusnetwork.momentum.data.cmdsigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class CommandSign {
	private final String id;
	private String command;
	private final Location location;

	private Set<String> usages; // set of uuids of players that have used this command sign

	public CommandSign(String id, String command, Location location) {
		this.id = id;
		this.command = command;
		this.location = location;
		this.usages = new HashSet<>();
	}

	public void loadUsages(Set<String> usages) {
		this.usages = usages;
	}

	public String getID() {
		return this.id;
	}

	// true if player has not used this sign
	public boolean addUsage(String uuid) {
		return this.usages.add(uuid);
	}
	public void removeUsage(String uuid) {
		this.usages.remove(uuid);
	}
	public boolean hasUsed(String uuid) {
		return usages.contains(uuid);
	}

	public Location getLocation() {
		return this.location;
	}

	public void updateCommand(String newCommand) {
		this.command = newCommand;
	}

	public void executeCommand() {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.command);
	}
}
