package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
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
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (a.length == 2 && a[0].equalsIgnoreCase("create")) {
                    // Creates a clan at the set price
                    if (playerStats.getClan() == null) {
                        int playerBalance  = (int) Parkour.getEconomy().getBalance(player);

                        if (playerBalance > Parkour.getSettingsManager().clans_price_create) {
                            if (a.length > 1) {
                                String clanTag = a[1];

                                if (clanTagRequirements(clanTag, sender)) {
                                    Parkour.getEconomy().withdrawPlayer(player, Parkour.getSettingsManager().clans_price_create);
                                    Parkour.getClansManager().add(new Clan(-1, clanTag, playerStats.getPlayerID()));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&cNo clan tag specified"));
                                sender.sendMessage(getHelp("create"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&cYou need &6" +
                                    Parkour.getSettingsManager().clans_price_create + " Coins &cto create a clan"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cYou cannot create a clan while in one"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("changetag")) {
                    // Changes clan tag
                } else {
                    // Unknown argument
                    sendHelp(sender);
                }
            } else {
                sender.sendMessage(Utils.translate("&cMost clan commands can only be run in-game"));
            }
        } else {
                sendHelp(sender);
        }
        return true;
    }

    private static boolean clanTagRequirements(String clanTag, CommandSender sender) {
        // Clan Tag has improper length
        if (clanTag.length() < Parkour.getSettingsManager().clans_tag_length_min
            && clanTag.length() > Parkour.getSettingsManager().clans_tag_length_max) {

            sender.sendMessage(Utils.translate("&c'&4" + clanTag + "&c' does not fit Clan Tag requirements"));
            sender.sendMessage(Utils.translate("&cClan Tags must be &4" + Parkour.getSettingsManager().clans_tag_length_min + "-"
                                               + Parkour.getSettingsManager().clans_tag_length_max + " &ccharacters"));
            return false;
        } else if (Parkour.getClansManager().get(clanTag) != null) {
            sender.sendMessage(Utils.translate("&cThe Clan Tag '&4" + clanTag + " &c' is already taken"));
            return false;
        } else {
            return true;
        }
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("stats")); // console friendly
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("changetag"));
        sender.sendMessage(getHelp("setowner"));
        sender.sendMessage(getHelp("kick"));
        sender.sendMessage(getHelp("invite"));
        sender.sendMessage(getHelp("deinvite"));
        sender.sendMessage(getHelp("disband"));
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "stats":
                return Utils.translate("&3/clans stats <clan>  &7Display clan statistics");
            case "create":
                return Utils.translate("&3/clan create <tag>  &7Create a clan &6" + Parkour.getSettingsManager().clans_price_create + " Coins");
            case "changetag":
                return Utils.translate("&3/clan changetag <tag>  &7Change clan tag &6" + Parkour.getSettingsManager().clans_price_create + " Coins");
            case "setowner":
                return Utils.translate("&3/clan owner <player>  &7Give your clan ownership");
            case "kick":
                return Utils.translate("&3/clan kick <player>  &7Kick player from your clan");
            case "invite":
                return Utils.translate("&3/clan invite <player>  &7Invite player to your clan");
            case "deinvite":
                return Utils.translate("&3/clan deinvite <player>  &7Revoke invitation");
            case "disband":
                return Utils.translate("&3/clan disband  &7Disband your clan");
        }
        return "";
    }
}
