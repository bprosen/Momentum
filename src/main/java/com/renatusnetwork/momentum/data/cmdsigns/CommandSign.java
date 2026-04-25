package com.renatusnetwork.momentum.data.cmdsigns;

import java.util.List;

public class CommandSign {

    private final String name;
    private String title;
    private final List<String> commands;
    private final CmdSignLocation location;
    private boolean broadcast;

    public CommandSign(String name, String title, List<String> commands, CmdSignLocation location, boolean broadcast) {
        this.name = name;
        this.title = title;
        this.commands = commands;
        this.location = location;
        this.broadcast = broadcast;
    }

    public String getName() {
        return this.name;
    }

    public CmdSignLocation getLocation() {
        return this.location;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void updateCommand(int index, String newCommand) {
        this.commands.set(index, newCommand);
    }

    public void addCommand(String newCommand) {
        this.commands.add(newCommand);
    }

    // returns the command that was removed
    public String removeCommand(int index) {
        return this.commands.remove(index);
    }

    public void clearCommands() {
        this.commands.clear();
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }

    public void toggleBroadcast() {
        this.broadcast = !this.broadcast;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
