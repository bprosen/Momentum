package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.data.stats.StatsManager;
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
                PlayerStats playerStats = Parkour.stats.get(player);


                if (a[0].equalsIgnoreCase("create")) {
                    // Creates a clan at the set price

                    if (playerStats.getClanID() == -1) {
                        int playerBalance  = (int) Parkour.economy.getBalance(player);

                        if (playerBalance > Parkour.settings.clans_price_create) {
                            if (a.length > 1) {

                            } else {
                                sender.sendMessage(ChatColor.RED + "No clan tag specified");
                                sender.sendMessage(getHelp("create"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.RED + "You cannot afford to create a clan. It requires " +
                                            ChatColor.GOLD + Parkour.settings.clans_price_create + " Coins"
                            );
                    } else
                        sender.sendMessage(ChatColor.RED + "You cannot create a clan while in one");

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
        sender.sendMessage(getHelp("disband"));
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
