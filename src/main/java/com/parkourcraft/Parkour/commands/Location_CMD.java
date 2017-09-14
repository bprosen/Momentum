package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.utils.storage.LocationManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Location_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length == 0)
            sendHelp(sender);
        else {
            if (a[0].equalsIgnoreCase("list")) { //subcommand: list
                sender.sendMessage(
                        ChatColor.GREEN + "Locations "
                        + ChatColor.GRAY + String.join(
                            ChatColor.DARK_GRAY + ", " + ChatColor.GRAY,
                            LocationManager.getPositionNames()
                        )
                );
            } else if (a[0].equalsIgnoreCase("go")) { //subcommand: go
                if (sender instanceof Player) {
                    Player player = ((Player) sender).getPlayer();

                    if (a.length == 2) {
                        String locationName = a[1];

                        if (LocationManager.exists(locationName)) {
                            LocationManager.teleport(player, locationName);

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
            } else if (a[0].equalsIgnoreCase("create")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender).getPlayer();

                    if (a.length == 2) {
                        String locationName = a[1];

                        if (!LocationManager.exists(locationName)) {
                            Location playerLocation = player.getLocation();

                            LocationManager.savePosition(locationName, playerLocation);

                            sender.sendMessage(
                                    ChatColor.GRAY + "Created location "
                                            + ChatColor.GREEN + locationName
                            );
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "The location "
                                            + ChatColor.GREEN + locationName
                                            + ChatColor.GRAY + " already exists"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("create"));
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

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Location names are case sensitive");
        sender.sendMessage(getHelp("go"));
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("delete"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("go"))
            return ChatColor.GREEN + "/loc go <location>" +
                    ChatColor.GRAY + " Teleports you to a location";
        else if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/loc list" +
                    ChatColor.GRAY + " Lists the configured locations";
        else if (cmd.equalsIgnoreCase("create"))
            return ChatColor.GREEN + "/loc create <location>" +
                    ChatColor.GRAY + " Creates location from your current positon";
        else if (cmd.equalsIgnoreCase("delete"))
            return ChatColor.GREEN + "/loc delete <location>" +
                    ChatColor.GRAY + " Deletes location";
        return "";
    }

}
