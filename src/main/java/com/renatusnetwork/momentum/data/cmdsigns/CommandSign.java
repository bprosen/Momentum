package com.renatusnetwork.momentum.data.cmdsigns;

import java.util.HashSet;
import java.util.Set;

public class CommandSign {
	private final String name;
	private String command;
	private final CmdSignLocation location;

	private Set<String> usages; // set of uuids of players that have used this command sign

	public CommandSign(String name, String command, CmdSignLocation location) {
		this.name = name;
		this.command = command;
		this.location = location;
		this.usages = new HashSet<>();
	}

	public void loadUsages(Set<String> usages) {
		this.usages = usages;
	}

	public String getName() {
		return this.name;
	}

	// true if player has not used this sign
	protected boolean addUsage(String uuid) {
		return this.usages.add(uuid);
	}
	protected void removeUsage(String uuid) {
		this.usages.remove(uuid);
	}
	public boolean hasUsed(String uuid) {
		return usages.contains(uuid);
	}

	public CmdSignLocation getLocation() {
		return this.location;
	}

	public void updateCommand(String newCommand) {
		this.command = newCommand;
	}
	public String getCommand() {
		return this.command;
	}
}
