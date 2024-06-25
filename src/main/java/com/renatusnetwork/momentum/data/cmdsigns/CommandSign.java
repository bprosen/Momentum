package com.renatusnetwork.momentum.data.cmdsigns;

public class CommandSign {
	private final String name;
	private String command;
	private final CmdSignLocation location;

	public CommandSign(String name, String command, CmdSignLocation location) {
		this.name = name;
		this.command = command;
		this.location = location;
	}

	public String getName() { return this.name; }

	public CmdSignLocation getLocation() { return this.location; }

	public String getCommand() { return this.command; }
	public void updateCommand(String newCommand) { this.command = newCommand; }
}
