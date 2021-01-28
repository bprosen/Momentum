package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.locations.Locations_YAML;
import com.parkourcraft.Parkour.utils.Utils;
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
                    sender.sendMessage(Utils.translate("&7Locations: &2" + String.join("&7, &2",
                                       Parkour.getLocationManager().getNames())));
                } else if (a[0].equalsIgnoreCase("go")) { //subcommand: go
                    if (sender instanceof Player) {
                        Player player = ((Player) sender).getPlayer();

                        if (a.length == 2) {
                            String locationName = a[1];

                            if (Parkour.getLocationManager().exists(locationName)) {
                                Parkour.getLocationManager().teleport(player, locationName);
                                sender.sendMessage(Utils.translate("&7Attempted to send you to &2" + locationName));
                            } else {
                                sender.sendMessage(Utils.translate("&7The location &2" + locationName +
                                        " &7does not exist"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                            sender.sendMessage(getHelp("go"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cThis command must be run in-game"));
                    }
                } else if (a[0].equalsIgnoreCase("set")) { //subcommand: set
                    if (sender instanceof Player) {
                        Player player = ((Player) sender).getPlayer();

                        if (a.length == 2) {
                            String locationName = a[1];

                            Location playerLocation = player.getLocation();

                            Parkour.getLocationManager().save(locationName, playerLocation);
                            sender.sendMessage(Utils.translate("&7Set location &2" + locationName));
                        } else {
                            sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                            sender.sendMessage(getHelp("set"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cThis command must be run in-game"));
                    }
                } else if (a[0].equalsIgnoreCase("del")) { //subcommand: del
                    if (sender instanceof Player) {

                        if (a.length == 2) {
                            String locationName = a[1];

                            if (Parkour.getLocationManager().exists(locationName)) {
                                Locations_YAML.remove(locationName);
                                sender.sendMessage(Utils.translate("&7Deleted location &2" + locationName));
                            } else {
                                sender.sendMessage(Utils.translate("&7The location &2" + locationName + " &7does not exist"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                            sender.sendMessage(getHelp("go"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cThis command must be run in-game"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&c'&4" + a[0] + "&c' is not a valid parameter"));
                    sendHelp(sender);
                }
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to run this command"));
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&cLocation names are case sensitive"));
        sender.sendMessage(getHelp("go"));
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("set"));
        sender.sendMessage(getHelp("del"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("go"))
            return Utils.translate("&2/loc go <location>  &7Teleports you to a location");
        else if (cmd.equalsIgnoreCase("list"))
            return Utils.translate("&2/loc list  &7Lists the configured location");
        else if (cmd.equalsIgnoreCase("set"))
            return Utils.translate("&2/loc set <location>  &7Sets location from your current position");
        else if (cmd.equalsIgnoreCase("del"))
            return Utils.translate("&2/loc del <location>  &7Deletes location");
        return "";
    }
}
