package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.data.locations.Locations_YAML;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Location_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            if (a.length == 0)
                sendHelp(sender);
            else {
                if (a[0].equalsIgnoreCase("list")) { //subcommand: list
                    sender.sendMessage(
                            ChatColor.GRAY + "Locations: "
                                    + ChatColor.GREEN + String.join(
                                    ChatColor.GRAY + ", " + ChatColor.GREEN,
                                    Parkour.locations.getNames()
                            )
                    );
                } else if (a[0].equalsIgnoreCase("go")) { //subcommand: go
                    if (sender instanceof Player) {
                        Player player = ((Player) sender).getPlayer();

                        if (a.length == 2) {
                            String locationName = a[1];

                            if (Parkour.locations.exists(locationName)) {
                                Parkour.locations.teleport(player, locationName);

                                sender.sendMessage(
                                        ChatColor.GRAY + "Attempted to send you to "
                                                + ChatColor.GREEN + locationName
                                );
                            } else
                                sender.sendMessage(
                                        ChatColor.GRAY + "The location "
                                                + ChatColor.GREEN + locationName
                                                + ChatColor.GRAY + " does not exist"
                                );
                        } else {
                            sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                            sender.sendMessage(getHelp("go"));
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "This command must be run in-game");
                } else if (a[0].equalsIgnoreCase("set")) { //subcommand: set
                    if (sender instanceof Player) {
                        Player player = ((Player) sender).getPlayer();

                        if (a.length == 2) {
                            String locationName = a[1];

                            Location playerLocation = player.getLocation();

                            Parkour.locations.save(locationName, playerLocation);

                            sender.sendMessage(
                                    ChatColor.GRAY + "Set location "
                                            + ChatColor.GREEN + locationName
                            );
                        } else {
                            sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                            sender.sendMessage(getHelp("set"));
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "This command must be run in-game");
                } else if (a[0].equalsIgnoreCase("del")) { //subcommand: del
                    if (sender instanceof Player) {
                        Player player = ((Player) sender).getPlayer();

                        if (a.length == 2) {
                            String locationName = a[1];

                            if (Parkour.locations.exists(locationName)) {
                                Locations_YAML.remove(locationName);

                                sender.sendMessage(
                                        ChatColor.GRAY + "Deleted location "
                                                + ChatColor.GREEN + locationName
                                );
                            } else
                                sender.sendMessage(
                                        ChatColor.GRAY + "The location "
                                                + ChatColor.GREEN + locationName
                                                + ChatColor.GRAY + " does not exist"
                                );
                        } else {
                            sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                            sender.sendMessage(getHelp("go"));
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "This command must be run in-game");
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "'" + ChatColor.DARK_RED + a[0] +
                                    ChatColor.RED + "' is not a valid parameter"
                    );
                    sendHelp(sender);
                }
            }
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Location names are case sensitive");
        sender.sendMessage(getHelp("go"));
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("set"));
        sender.sendMessage(getHelp("del"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("go"))
            return ChatColor.GREEN + "/loc go <location>" +
                    ChatColor.GRAY + " Teleports you to a location";
        else if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/loc list" +
                    ChatColor.GRAY + " Lists the configured locations";
        else if (cmd.equalsIgnoreCase("set"))
            return ChatColor.GREEN + "/loc set <location>" +
                    ChatColor.GRAY + " Sets location from your current positon";
        else if (cmd.equalsIgnoreCase("del"))
            return ChatColor.GREEN + "/loc del <location>" +
                    ChatColor.GRAY + " Deletes location";
        return "";
    }

}
