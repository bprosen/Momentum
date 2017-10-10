package com.parkourcraft.Parkour.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Clan_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {

            if (a[0].equalsIgnoreCase("stats")) {
                // Displays stats of clan requested, or personal if available


            } else if (sender instanceof Player) {
                // Sub commands here cannot be ran by non-players
                Player player = ((Player) sender).getPlayer();


                if (a[0].equalsIgnoreCase("create")) {
                    // Creates a clan at the set price


                } else if (a[0].equalsIgnoreCase("tag")) {
                    // Changes clan tag


                } else {
                    // Unknown argument

                    sendHelp(sender);
                    sender.sendMessage(
                            ChatColor.RED + "Unknown argument '" +
                                    ChatColor.DARK_RED + a[0]
                                    + ChatColor.RED + "'"
                    );
                }
            } else
                sender.sendMessage(ChatColor.RED + "Most clan commands can only be run in-game");
        } else
            sendHelp(sender);

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("stats")); // console friendly
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("tag"));
        sender.sendMessage(getHelp("kick"));
        sender.sendMessage(getHelp("invite"));
        sender.sendMessage(getHelp("deinvite"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("stats"))
            return ChatColor.DARK_AQUA + "/clan stats [clan]" +
                    ChatColor.GRAY + " Display clan statistics";

        else if (cmd.equalsIgnoreCase("create"))
            return ChatColor.DARK_AQUA + "/clan create <tag>" +
                    ChatColor.GRAY + " Create a clan";

        else if (cmd.equalsIgnoreCase("tag"))
            return ChatColor.DARK_AQUA + "/clan tag <tag>" +
                    ChatColor.GRAY + " Change clan tag";

        return "";
    }

}
