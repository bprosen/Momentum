package com.renatusnetwork.momentum.data.cmdsigns;

public class CommandSign {

	private String name;
	private String title;
	private String command;
	private CmdSignLocation location;
	private boolean broadcast;

	public CommandSign(String name, String title, String command, CmdSignLocation location, boolean broadcast) {
		this.name = name;
		this.command = command;
		this.location = location;
		this.broadcast = broadcast;
	}

	public String getName() { return this.name; }

	public CmdSignLocation getLocation() { return this.location; }

	public String getCommand() { return this.command; }

	public void updateCommand(String newCommand) { this.command = newCommand; }

	public boolean isBroadcast() { return this.broadcast; }

	public void toggleBroadcast() { this.broadcast = !this.broadcast; }

	public void setTitle(String title) { this.title = title; }

	public String getTitle() { return this.title; }
}
