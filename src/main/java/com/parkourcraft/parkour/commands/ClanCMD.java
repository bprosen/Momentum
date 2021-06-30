package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.clans.ClanMember;
import com.parkourcraft.parkour.data.clans.ClansDB;
import com.parkourcraft.parkour.data.clans.ClansYAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ClanCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
            if (a[0].equalsIgnoreCase("stats")) {

                Clan targetClan;

                // this is what allows "/clan stats" to do self check
                if (a.length == 1) {
                    // make sure it is a player, not console
                    if (sender instanceof Player) {

                        Player player = (Player) sender;
                        Clan selfClan = Parkour.getStatsManager().get(player).getClan();
                        // check if they are in a clan
                        if (selfClan != null)
                            targetClan = selfClan;
                        else {
                            player.sendMessage(Utils.translate("&cYou are not in a clan!"));
                            return true;
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cConsole cannot execute this"));
                        return true;
                    }
                } else {
                    targetClan = Parkour.getClansManager().get(a[1]);
                }

                if (targetClan != null) {

                    // send stats
                    sender.sendMessage(Utils.translate("&6&l" + targetClan.getTag() + "&e's Stats"));
                    sender.sendMessage(Utils.translate("  &cClan Level &4" + targetClan.getLevel()));
                    sender.sendMessage(Utils.translate("  &cClan XP &4" + Utils.formatNumber(targetClan.getXP())));

                    // if max level, dont send needed to level up
                    if (!targetClan.isMaxLevel()) {
                        long clanXPNeeded = ClansYAML.getLevelUpPrice(targetClan) - targetClan.getXP();

                        sender.sendMessage(Utils.translate("  &cXP to Level Up"));
                        sender.sendMessage(Utils.translate("    &4" + Utils.formatNumber(clanXPNeeded)));
                        sender.sendMessage("");
                    }

                    sender.sendMessage(Utils.translate( "&6Members &e" + targetClan.getMembers().size()));

                    for (ClanMember clanMember : targetClan.getMembers()) {

                        Player player = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                        String onlineString = "  &c" + clanMember.getPlayerName() + " ";
                        // change string based on if they are online
                        if (player == null)
                            onlineString += " &4Offline";
                        else
                            onlineString += " &aOnline";

                        sender.sendMessage(Utils.translate(onlineString));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not a clan!"));
                }
            } else if (sender instanceof Player) {
                // Sub commands here cannot be ran by non-players
                Player player = (Player) sender;
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                Clan clan = playerStats.getClan();

                if (a.length == 3 && a[0].equalsIgnoreCase("setxp")) {

                    if (player.hasPermission("pc-parkour.admin")) {
                        String clanName = a[1];
                        if (Utils.isLong(a[2])) {
                            Long newXp = Long.parseLong(a[2]);

                            Clan targetClan = Parkour.getClansManager().get(clanName);
                            if (targetClan != null) {
                                // make sure it is not negative xp
                                if (newXp > 0) {
                                    targetClan.setXP(newXp);
                                    // update in db
                                    ClansDB.setClanXP(newXp, targetClan.getID());
                                    player.sendMessage(Utils.translate("&eYou set &6&l" + clan.getTag() + "&e's" +
                                            " XP to &6" + newXp));
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot set negative xp!"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                            }
                        } else {
                            player.sendMessage(Utils.translate(""));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("setlevel")) {

                    if (player.hasPermission("pc-parkour.admin")) {
                        String clanName = a[1];

                        if (Utils.isInteger(a[2])) {
                            int newLevel = Integer.parseInt(a[2]);

                            Clan targetClan = Parkour.getClansManager().get(clanName);
                            if (targetClan != null) {
                                // make sure it is actually a level
                                if (ClansYAML.isSection("clans." + newLevel)) {
                                    targetClan.setLevel(newLevel);
                                    // update in db
                                    ClansDB.setClanLevel(newLevel, targetClan.getID());
                                    player.sendMessage(Utils.translate("&eYou set &6&l" + clan.getTag() + "&e's" +
                                                                        " level to &6" + newLevel));
                                } else {
                                    player.sendMessage(Utils.translate("&cThat level does not exist!"));
                                }
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("create")) {
                    // Creates a clan at the set price
                    if (clan == null) {

                        int playerBalance = (int) Parkour.getEconomy().getBalance(player);

                        if (playerBalance >= Parkour.getSettingsManager().clans_price_create) {
                            if (a.length > 1) {
                                String clanTag = a[1];

                                if (clanTagRequirements(clanTag, sender)) {
                                    Parkour.getEconomy().withdrawPlayer(player, Parkour.getSettingsManager().clans_price_create);
                                    Parkour.getClansManager().add(new Clan(-1, clanTag, playerStats.getPlayerID(), 1, 0));
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

                    // if they are in a clan
                    if (clan != null) {
                        if (clan.getOwner().getPlayerName().equalsIgnoreCase(player.getName())) {

                            if (a.length > 1) {
                                String clanTag = a[1];

                                if (clanTagRequirements(clanTag, sender)) {
                                    // update in data
                                    clan.setTag(clanTag);
                                    ClansDB.updateClanTag(clan);
                                    sendClanMessage(clan, "&6&lClan Owner " +
                                            player.getName() + " &ehas changed your clan's tag to &c" + clanTag, true, player);
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&cNo clan tag specified"));
                                sender.sendMessage(getHelp("changetag"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou are not the owner of your clan"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("setowner")) {
                    // sets new owner

                    Player victim = Bukkit.getPlayer(a[1]);
                    if (victim == null) {
                        player.sendMessage(Utils.translate("&4" + victim.getName() + " &cis not online"));
                        return true;
                    }
                    Clan targetClan = Parkour.getStatsManager().get(player).getClan();

                    if (clan != null) {
                        if (targetClan != null) {
                            // if in same clan
                            if (targetClan.getID() == clan.getID()) {
                                if (clan.getOwner().getPlayerName().equalsIgnoreCase(player.getName())) {
                                    // update data
                                    clan.promoteOwner(victim.getUniqueId().toString());
                                    ClansDB.updateClanOwnerID(clan);
                                    sendClanMessage(clan, "&6" + victim.getName() + " &ehas been promoted to" +
                                            " &6&lClan Owner &eby &6" + player.getName(), true, player);
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot switch clan owners if you are not owner"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou are not in the same clan as &4" + victim.getName()));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + victim.getName() + " &cis not in a clan"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("invite")) {
                    // invite player
                    if (clan != null) {

                        Player victim = Bukkit.getPlayer(a[1]);

                        if (victim == null) {
                            player.sendMessage(Utils.translate("&4" + victim.getName() + " &cis not online"));
                            return true;
                        }

                        // if they are not in a clan
                        if (Parkour.getStatsManager().get(victim).getClan() == null) {

                            // if they already have an invite or not
                            if (!clan.isInvited(victim.getUniqueId().toString())) {

                                int maxMembers = Parkour.getSettingsManager().clans_max_members;
                                if (clan.getMembers().size() <= maxMembers) {
                                    // add an invite
                                    victim.sendMessage(Utils.translate("&6&l" + player.getName() + " &ehas sent you an" +
                                            " invitation to their &6&lClan &c" + clan.getTag()));
                                    victim.sendMessage(Utils.translate("   &7Type &e/clan accept " + player.getName() +
                                            " &7within &c20 seconds &7to accept"));
                                    player.sendMessage(Utils.translate("&eYou sent a &6&lClan Invite &eto &6" + victim.getName()
                                            + " &ethey have 20 seconds to accept"));

                                    clan.addInvite(victim.getUniqueId().toString());
                                    new BukkitRunnable() {
                                        public void run() {
                                            // ran out of time
                                            if (clan.isInvited(victim.getUniqueId().toString())) {

                                                player.sendMessage(Utils.translate("&6" + victim.getName() +
                                                        " &edid not accept your &6&lClan Invite &ein time"));
                                                victim.sendMessage(Utils.translate("&6You did not accept &6" +
                                                        player.getName() + "&e's &6&lClan Invite &ein time"));

                                                // remove old invite
                                                clan.removeInvite(victim.getUniqueId().toString());
                                            }
                                        }
                                        // 20 seconds to accept invite
                                    }.runTaskLater(Parkour.getPlugin(), 20 * 20);
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot invite anymore people to your" +
                                                                        " clan! Max - " + maxMembers));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou have already sent an invite to &4" + victim.getUniqueId().toString()));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + victim.getName() + " &cis already in a clan"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou have to be in a clan to invite someone!"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {
                    // accept invite if they have one

                    // make sure they are not in a clan
                    if (clan == null) {

                        Player victim = Bukkit.getPlayer(a[1]);
                        if (victim == null) {
                            player.sendMessage(Utils.translate("&4" + victim.getName() + " &cis not online"));
                            return true;
                        }

                        Clan targetClan = Parkour.getStatsManager().get(victim).getClan();

                        if (targetClan != null) {
                            // if they are invited
                            if (targetClan.isInvited(player.getUniqueId().toString())) {

                                if (targetClan.getMembers().size() <= Parkour.getSettingsManager().clans_max_members) {
                                    // add to clan and remove invite
                                    targetClan.addMember(new ClanMember(playerStats.getPlayerID(), playerStats.getUUID(), player.getName()));
                                    playerStats.setClan(targetClan);
                                    ClansDB.updatePlayerClanID(playerStats);
                                    targetClan.removeInvite(player.getUniqueId().toString());
                                    sendClanMessage(targetClan, "&6" + player.getName() + " &ehas joined your clan!", false, player);
                                    player.sendMessage(Utils.translate("&eYou joined the &6&lClan &c" + targetClan.getTag()));

                                } else {
                                    player.sendMessage(Utils.translate("&cThat clan is full!"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&eYou do not have an invite from &6Clan &c" + targetClan.getTag()));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&6" + victim.getName() + " &eis not in a &6&lClan"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot join a clan if you are in one"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("kick")) {
                    // kick if in clan

                    // make sure they are in a clan
                    if (clan != null) {
                        // make sure they are owner of the clan
                        if (clan.getOwner().getPlayerName().equalsIgnoreCase(player.getName())) {

                            String victimName = a[1];
                            if (Bukkit.getPlayer(victimName) == null) {
                                player.sendMessage(Utils.translate("&4" + victimName + " &cis not online"));
                                return true;
                            }

                            Player victim = Bukkit.getPlayer(victimName);
                            Clan targetClan = ClansDB.getClan(victimName);

                            // make sure they are not trying to kick themselves
                            if (!victim.getName().equalsIgnoreCase(player.getName())) {
                                // if they do not have a clan stored in database
                                if (targetClan != null) {

                                    // make sure they are kicking from the same clan
                                    if (targetClan.getID() == clan.getID()) {

                                        sendClanMessage(clan, "&6" + victimName + " &ehas been removed from your clan", true, victim);

                                        targetClan.removeMemberFromName(victimName);

                                        PlayerStats victimStats = Parkour.getStatsManager().get(victim);
                                        victimStats.resetClan();
                                        ClansDB.updatePlayerClanID(victimStats);
                                    }
                                } else {
                                    player.sendMessage(Utils.translate("&4" + victimName + " &cis not in your clan"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot kick yourself"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot kick a member from a clan you do not own!"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou are not in a clan!"));
                    }
                } else if (a.length == 1 && a[0].equalsIgnoreCase("disband")) {
                    // disband if in a clan

                    // make sure they are in a clan
                    if (clan != null) {
                        if (clan.getOwner().getPlayerName().equalsIgnoreCase(player.getName())) {
                            // delete clan
                            Parkour.getClansManager().deleteClan(clan.getID(), true);
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot disband a clan you are not owner of"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot disband a clan if you are not in one"));
                    }
                } else if (a.length == 1 && a[0].equalsIgnoreCase("leave")) {
                    // leave if in a clan

                    if (clan != null) {
                        // if they are only one in their clan
                        if (clan.getMembers().size() == 1) {

                            ClansDB.resetClanMember(player.getName());
                            ClansDB.removeClan(clan.getID());
                            Parkour.getClansManager().removeClan(clan.getID());
                            playerStats.resetClan();
                            player.sendMessage(Utils.translate("&eYou have left your clan"));

                            // if not only one, make sure they are not the owner
                        } else if (!clan.getOwner().getPlayerName().equalsIgnoreCase(player.getName())) {

                            ClansDB.resetClanMember(player.getName());
                            clan.removeMemberFromName(player.getName());
                            playerStats.resetClan();
                            player.sendMessage(Utils.translate("&eYou have left your clan"));
                            sendClanMessage(clan, "&6" + player.getName() + " &ehas left your clan", false, player);
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot leave a clan you own"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                    }
                } else if (player.hasPermission("pc-parkour.admin") && (a.length == 2 && a[0].equalsIgnoreCase("delete"))) {

                    Clan targetClan = Parkour.getClansManager().get(a[1]);

                    if (targetClan != null) {
                        // remove from cache and db
                        Parkour.getClansManager().deleteClan(targetClan.getID(), false);
                        player.sendMessage(Utils.translate("&7You have deleted &c" + a[1] + " &7from the database"));
                    } else {
                        player.sendMessage(Utils.translate("&7Clan &c" + a[1] + " &7does not exist"));
                    }
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
            sender.sendMessage(Utils.translate("&cThe Clan Tag '&4" + clanTag + "&c' is already taken"));
            return false;
        } else {
            return true;
        }
    }

    private static void sendClanMessage(Clan targetClan, String message, boolean sendToSelf, Player self) {
        for (ClanMember clanMember : targetClan.getMembers()) {
            // make sure they are online
            Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

            if (clanPlayer != null) {
                // if it will send to self, then do everyone
                if (sendToSelf)
                    clanPlayer.sendMessage(Utils.translate(message));
                // otherwise make sure it is not the same person
                else if (clanPlayer.getName() != self.getName())
                    clanPlayer.sendMessage(Utils.translate(message));
            }
        }
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("stats")); // console friendly
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("invite"));
        sender.sendMessage(getHelp("accept"));
        sender.sendMessage(getHelp("leave"));
        sender.sendMessage(getHelp("kick"));
        sender.sendMessage(getHelp("disband"));
        sender.sendMessage(getHelp("changetag"));
        sender.sendMessage(getHelp("setowner"));

        // send admin section
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("pc-parkour.admin")) {
                sender.sendMessage(getHelp("setlevel"));
                sender.sendMessage(getHelp("setxp"));
                sender.sendMessage(getHelp("delete"));
            }
        }
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "stats":
                return Utils.translate("&3/clan stats <clan>  &7Display clan statistics");
            case "create":
                return Utils.translate("&3/clan create <tag>  &7Create a clan &6" + Parkour.getSettingsManager().clans_price_create + " Coins");
            case "changetag":
                return Utils.translate("&3/clan changetag <tag>  &7Change clan tag &6" + Parkour.getSettingsManager().clans_price_create + " Coins");
            case "setowner":
                return Utils.translate("&3/clan setowner <player>  &7Give your clan ownership");
            case "kick":
                return Utils.translate("&3/clan kick <player>  &7Kick player from your clan");
            case "invite":
                return Utils.translate("&3/clan invite <player>  &7Invite player to your clan");
            case "accept":
                return Utils.translate("&3/clan accept <player>  &7Accept invite from player");
            case "disband":
                return Utils.translate("&3/clan disband  &7Disband your clan");
            case "leave":
                return Utils.translate("&3/clan leave  &7Leave your clan");
            case "setxp":
                return Utils.translate("&3/clan setxp <clan> <xp>  &7Sets clan XP");
            case "setlevel":
                return Utils.translate("&3/clan setlevel <clan> <level>  &7Sets clan level");
            case "delete":
                return Utils.translate("&3/clan delete <clan>  &7Deletes the clan");
        }
        return "";
    }
}
