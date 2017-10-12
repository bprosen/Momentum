package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.clans.Clan;
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

                    if (playerStats.getClan() == null) {
                        int playerBalance  = (int) Parkour.economy.getBalance(player);

                        if (playerBalance > Parkour.settings.clans_price_create) {
                            if (a.length > 1) {
                                String clanTag = a[1];

                                if (clanTagRequirements(clanTag, sender)) {
                                    Parkour.economy.withdrawPlayer(player, Parkour.settings.clans_price_create);

                                    Parkour.clans.add(new Clan(-1, clanTag, playerStats.getPlayerID()));
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "No clan tag specified");
                                sender.sendMessage(getHelp("create"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.RED + "You need " +
                                            ChatColor.GOLD + Parkour.settings.clans_price_create + " Coins" +
                                            ChatColor.RED + " to create a clan, pleb!"
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

    private static boolean clanTagRequirements(String clanTag, CommandSender sender) {
        if (clanTag.length() < Parkour.settings.clans_tag_length_min
                && clanTag.length() > Parkour.settings.clans_tag_length_max) {
            // Clan Tag has improper length

            sender.sendMessage(
                    ChatColor.RED + "'" +
                            ChatColor.DARK_RED + clanTag +
                            ChatColor.RED + "' does not fit Clan Tag requirements"
            );
            sender.sendMessage(
                    ChatColor.RED + "Clan Tags must be " +
                            ChatColor.DARK_RED + Parkour.settings.clans_tag_length_min
                            + "-" + Parkour.settings.clans_tag_length_max +
                            ChatColor.RED + " characters"
            );

            return false;
        }

        if (Parkour.clans.get(clanTag) != null) {
            // Clan Tag already taken

            sender.sendMessage(
                    ChatColor.RED + "The Clan Tag '" +
                            ChatColor.DARK_RED + clanTag +
                            ChatColor.RED + "' is already taken"
            );

            return false;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("stats")); // console friendly
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("tag"));
        sender.sendMessage(getHelp("owner"));
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
                    ChatColor.GRAY + " Create a clan " +
                    ChatColor.GOLD + Parkour.settings.clans_price_create + " Coins";

        else if (cmd.equalsIgnoreCase("tag"))
            return ChatColor.DARK_AQUA + "/clan tag <tag>" +
                    ChatColor.GRAY + " Change clan tag " +
                    ChatColor.GOLD + Parkour.settings.clans_price_tag + " Coins";

        if (cmd.equalsIgnoreCase("owner"))
            return ChatColor.DARK_AQUA + "/clan owner [player]" +
                    ChatColor.GRAY + " Give your clan ownership";

        if (cmd.equalsIgnoreCase("kick"))
            return ChatColor.DARK_AQUA + "/clan kick [player]" +
                    ChatColor.GRAY + " Kick player from your clan";

        if (cmd.equalsIgnoreCase("invite"))
            return ChatColor.DARK_AQUA + "/clan invite [player]" +
                    ChatColor.GRAY + " Invite player to your clan";

        if (cmd.equalsIgnoreCase("deinvite"))
            return ChatColor.DARK_AQUA + "/clan deinvite [player]" +
                    ChatColor.GRAY + " Revoke invitation";

        if (cmd.equalsIgnoreCase("disband"))
            return ChatColor.DARK_AQUA + "/clan disband" +
                    ChatColor.GRAY + " Disband your clan";

        return "";
    }

}
