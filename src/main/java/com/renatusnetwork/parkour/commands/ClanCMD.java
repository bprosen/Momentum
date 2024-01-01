package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.clans.*;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.sk89q.worldedit.UnknownItemException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.UUID;

public class ClanCMD implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {

        ClansManager clansManager = Parkour.getClansManager();

        if (a.length > 0)
        {
            if (a.length == 3 && a[0].equalsIgnoreCase("setmaxlevel"))
            {
                if (sender.hasPermission("rn-parkour.admin"))
                {
                    String clanName = a[1];
                    if (Utils.isInteger(a[2]))
                    {
                        int newMaxLevel = Integer.parseInt(a[2]);

                        Clan targetClan = Parkour.getClansManager().get(clanName);
                        if (targetClan != null)
                        {
                            // make sure it is not negative level
                            clansManager.updateMaxLevel(targetClan, newMaxLevel);
                            sender.sendMessage(Utils.translate("&eYou set &6" + targetClan.getTag() + "&e's" +
                                    " Max Level to &6" + targetClan.getMaxLevel()));
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not a valid integer"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("setmaxmembers"))
            {
                if (sender.hasPermission("rn-parkour.admin"))
                {
                    String clanName = a[1];
                    if (Utils.isInteger(a[2]))
                    {
                        int newMaxMembers = Integer.parseInt(a[2]);

                        Clan targetClan = Parkour.getClansManager().get(clanName);
                        if (targetClan != null)
                        {
                            int memberCount = targetClan.getMembers().size();
                            if (memberCount <= newMaxMembers)
                            {
                                clansManager.updateMaxMembers(targetClan, newMaxMembers);
                                sender.sendMessage(Utils.translate("&eYou set &6" + targetClan.getTag() + "&e's" +
                                        " Max Members to &6" + targetClan.getMaxMembers()));
                            }
                            else
                            {
                                sender.sendMessage(Utils.translate("&cYou cannot set &4" + targetClan.getTag() + "&c's max members to less than what they have (" + memberCount + ")"));
                            }
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not a valid integer"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("addmaxlevel"))
            {
                if (sender.hasPermission("rn-parkour.admin"))
                {
                    String playerName = a[1];
                    Player target = Bukkit.getPlayer(playerName);

                    if (target != null)
                    {
                        Clan targetClan = Parkour.getStatsManager().get(target).getClan();

                        if (targetClan != null)
                        {
                            clansManager.updateMaxLevel(targetClan, targetClan.getMaxLevel() + 1);
                            sender.sendMessage(Utils.translate("&eYou set &6" + targetClan.getTag() + "&e's" +
                                    " Max Level to &6" + targetClan.getMaxLevel()));
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cis not in a clan"));
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("addmaxmember"))
            {
                if (sender.hasPermission("rn-parkour.admin"))
                {
                    String playerName = a[1];
                    Player target = Bukkit.getPlayer(playerName);

                    if (target != null)
                    {
                        Clan targetClan = Parkour.getStatsManager().get(target).getClan();

                        if (targetClan != null)
                        {
                            clansManager.updateMaxMembers(targetClan, targetClan.getMaxMembers() + 1);
                            sender.sendMessage(Utils.translate("&eYou set &6" + targetClan.getTag() + "&e's" +
                                    " Max Members to &6" + targetClan.getMaxMembers()));
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cis not in a clan"));
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
            }
            else if (a[0].equalsIgnoreCase("stats"))
            {

                Clan targetClan;
                // this is what allows "/clan stats" to do self check
                if (a.length == 1)
                {
                    // make sure it is a player, not console
                    if (sender instanceof Player)
                    {

                        Player player = (Player) sender;
                        Clan selfClan = Parkour.getStatsManager().get(player).getClan();

                        // check if they are in a clan
                        if (selfClan != null)
                            targetClan = selfClan;
                        else
                        {
                            player.sendMessage(Utils.translate("&cYou are not in a clan!"));
                            return true;
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&cConsole cannot execute this"));
                        return true;
                    }
                }
                else
                {
                    targetClan = Parkour.getClansManager().get(a[1]);
                }

                if (targetClan != null)
                {

                    // send stats
                    sender.sendMessage(Utils.translate("&6" + targetClan.getTag() + "&e's Stats"));
                    sender.sendMessage(Utils.translate("  &cClan Level &4" + targetClan.getLevel()));
                    sender.sendMessage(Utils.translate("  &cMax Level &4" + targetClan.getMaxLevel()));
                    sender.sendMessage(Utils.translate("  &cTotal Clan XP &4" + Utils.shortStyleNumber(targetClan.getTotalXP())));

                    // if max level, dont send needed to level up
                    if (!targetClan.isMaxLevel())
                    {
                        SettingsManager settingsManager = Parkour.getSettingsManager();

                        long clanXPNeeded = settingsManager.clan_level_xp_required.get(targetClan.getLevel()) - targetClan.getXP();

                        sender.sendMessage(Utils.translate("  &cClan XP for Level &4" + Utils.formatNumber(targetClan.getXP())));
                        sender.sendMessage(Utils.translate("  &cXP to Level Up"));
                        sender.sendMessage(Utils.translate("    &4" + Utils.formatNumber(clanXPNeeded)));
                    }

                    sender.sendMessage("");
                    sender.sendMessage(Utils.translate( "&6Members &e" + targetClan.getMembers().size() + "/" + targetClan.getMaxMembers()));

                    for (ClanMember clanMember : targetClan.getMembers())
                    {

                        Player player = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                        String onlineString = "  &c" + clanMember.getName() + " ";
                        // change string based on if they are online
                        if (player == null)
                            onlineString += " &4Offline";
                        else
                            onlineString += " &aOnline";

                        sender.sendMessage(Utils.translate(onlineString));
                    }
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not a clan!"));
            }
            else if (sender instanceof Player)
            {
                // Sub commands here cannot be ran by non-players
                Player player = (Player) sender;
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                Clan clan = playerStats.getClan();

                if (a.length == 3 && a[0].equalsIgnoreCase("setxp"))
                {
                    if (player.hasPermission("rn-parkour.admin"))
                    {
                        String clanName = a[1];
                        if (Utils.isInteger(a[2]))
                        {
                            int newXP = Integer.parseInt(a[2]);

                            Clan targetClan = Parkour.getClansManager().get(clanName);
                            if (targetClan != null)
                            {
                                // make sure it is not negative xp
                                if (newXP > 0)
                                {
                                    clansManager.updateXP(clan, newXP);
                                    player.sendMessage(Utils.translate("&eYou set &6&l" + targetClan.getTag() + "&e's XP to &6" + newXP));
                                }
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot set negative xp"));
                            }
                            else
                                player.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                        else
                            player.sendMessage(Utils.translate("&4" + a[2] + " is not an integer"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                // set total gained xp
                }
                else if (a.length == 3 && a[0].equalsIgnoreCase("settotalxp"))
                {
                    if (player.hasPermission("rn-parkour.admin"))
                    {
                        String clanName = a[1];

                        if (Utils.isLong(a[2]))
                        {
                            long newXP = Long.parseLong(a[2]);

                            Clan targetClan = Parkour.getClansManager().get(clanName);
                            if (targetClan != null)
                            {
                                // make sure it is not negative xp
                                if (newXP > 0) {
                                    clansManager.updateTotalXP(clan, newXP);
                                    player.sendMessage(Utils.translate("&eYou set &6&l" + targetClan.getTag() + "&e's total XP to &6" + newXP));
                                }
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot set negative xp"));
                            }
                            else
                                player.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                        else
                            player.sendMessage(Utils.translate("&4" + a[2] + " is not an integer"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
                else if (a.length == 3 && a[0].equalsIgnoreCase("setlevel"))
                {
                    if (player.hasPermission("rn-parkour.admin"))
                    {
                        String clanName = a[1];

                        if (Utils.isInteger(a[2]))
                        {
                            int newLevel = Integer.parseInt(a[2]);

                            Clan targetClan = Parkour.getClansManager().get(clanName);
                            if (targetClan != null)
                            {
                                // make sure it is actually a level, we cannot set beyond whats upgradable
                                if (Parkour.getSettingsManager().clan_level_xp_required.containsKey(newLevel))
                                {
                                    if (targetClan.getMaxLevel() >= newLevel)
                                    {
                                        clansManager.updateLevel(clan, newLevel);
                                        player.sendMessage(Utils.translate("&eYou set &6&l" + clan.getTag() + "&e's level to &6" + newLevel));
                                    }
                                    else
                                        player.sendMessage(Utils.translate("&cYou cannot set the level beyond the clan's max (" + targetClan.getMaxLevel() + ")"));
                                }
                                else
                                    player.sendMessage(Utils.translate("&cThat clan level does not exist!"));
                            }
                            else
                                player.sendMessage(Utils.translate("&4" + clanName + " &cis not a clan"));
                        }
                        else
                            player.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("chatspy"))
                {
                    if (player.hasPermission("rn-parkour.staff"))
                    {
                        clansManager.toggleChatSpy(player.getName(), false);

                        boolean chatSpy = clansManager.isInChatSpy(player.getName());
                        String isCSOn = "&aOn";
                        if (!chatSpy)
                            isCSOn = "&cOff";

                        player.sendMessage(Utils.translate("&7You have turned &6&lClan ChatSpy " + isCSOn));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("create"))
                {
                    // Creates a clan at the set price
                    if (clan == null)
                    {
                        int playerBalance = (int) playerStats.getCoins();
                        int createPrice = Parkour.getSettingsManager().clans_price_create;

                        if (playerBalance >= createPrice)
                        {
                            if (a.length > 1)
                            {
                                String clanTag = ChatColor.stripColor(a[1]);

                                if (clanTagRequirements(clanTag, sender))
                                {
                                    Parkour.getStatsManager().removeCoins(playerStats, Parkour.getSettingsManager().clans_price_create);
                                    clansManager.create(clanTag, playerStats);
                                    sender.sendMessage(Utils.translate("&7Successfully created your Clan &3" + clanTag));
                                }
                            }
                            else
                                sender.sendMessage(Utils.translate("&cNo clan tag specified"));
                        }
                        else
                            sender.sendMessage(Utils.translate(
                                "&cYou need &6" + createPrice +
                                     " Coins &a(" + (createPrice - playerBalance) + " more) &cto create a clan"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou cannot create a clan while in one"));
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("chat"))
                {
                    if (clan != null)
                    {

                        clansManager.toggleClanChat(player.getName(), clan);

                        boolean clanChat = clansManager.isInClanChat(player.getName());
                        String isCCOn = "&aOn";
                        if (!clanChat)
                            isCCOn = "&cOff";

                        player.sendMessage(Utils.translate("&7You have turned &6&lClan Chat " + isCCOn));
                    }
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("changetag"))
                {
                    // if they are in a clan
                    if (clan != null)
                    {
                        if (clan.isOwner(player.getName()))
                        {
                            if (a.length > 1)
                            {
                                int playerBalance = (int) playerStats.getCoins();
                                int price = Parkour.getSettingsManager().clans_price_tag;

                                if (playerBalance >= price)
                                {
                                    String newTag = ChatColor.stripColor(a[1]);

                                    if (clanTagRequirements(newTag, sender))
                                    {
                                        Parkour.getStatsManager().removeCoins(playerStats, Parkour.getSettingsManager().clans_price_tag);
                                        clansManager.updateClanTag(clan, newTag);
                                        sendClanMessage(clan,
                                                "&6&lClan Owner " + player.getName() + " &ehas changed your clan's tag to &c" + newTag,
                                                true, player);
                                    }
                                }
                                else
                                    sender.sendMessage(Utils.translate(
                                            "&cYou need &6" + price +
                                                    " Coins &a(" + (price - playerBalance) + " more) &cto change your clan's tag"));
                            }
                            else
                                sender.sendMessage(Utils.translate("&cNo clan tag specified"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou are not the owner of your clan"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("setowner"))
                {
                    Clan targetClan = Parkour.getClansManager().getFromMember(a[1]);

                    if (clan != null)
                    {
                        if (targetClan != null)
                        {
                            // if in same clan
                            if (targetClan.equals(clan))
                            {
                                if (clan.isOwner(player.getName()))
                                {
                                    clansManager.updateClanOwner(clan, targetClan.getMember(a[1]).getUUID());

                                    sendClanMessage(clan,
                                    "&6" + a[1] + " &ehas been promoted to &6&lClan Owner &eby &6" +
                                            player.getName(), true, player
                                    );
                                }
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot switch clan owners if you are not owner"));
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou are not in the same clan as &4" + a[1]));
                        }
                        else
                            player.sendMessage(Utils.translate("&4" + a[1] + " &cis not in a clan"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("invite"))
                {
                    // invite player
                    if (clan != null)
                    {
                        if (clan.isOwner(player.getName()))
                        {
                            PlayerStats targetStats = Parkour.getStatsManager().getByName(a[1]);

                            if (targetStats != null)
                            {
                                // if they are not in a clan
                                if (!targetStats.inClan())
                                {
                                    // if they already have an invite or not
                                    if (!clan.isInvited(targetStats.getName()))
                                    {
                                        int maxMembers = clan.getMaxMembers();

                                        if (clan.numMembers() < maxMembers)
                                        {
                                            // add an invite
                                            targetStats.sendMessage(Utils.translate(
                                                "&6&l" + player.getName() + " &ehas sent you an " +
                                                     "invitation to their &6&lClan &c" + clan.getTag()
                                            ));
                                            targetStats.sendMessage(Utils.translate(
                                                    "   &7Type &e/clan accept " + player.getName() + " " +
                                                         "&7within &c30 seconds &7to accept"
                                            ));

                                            player.sendMessage(Utils.translate(
                                                    "&eYou sent a &6&lClan Invite &eto &6" + targetStats.getName() + " " +
                                                         "&ethey have 30 seconds to accept"
                                            ));

                                            clan.addInvite(targetStats.getName());
                                            new BukkitRunnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    // ran out of time
                                                    if (clan.isInvited(targetStats.getName()))
                                                    {
                                                        player.sendMessage(Utils.translate(
                                                                "&6" + targetStats.getName() + " &edid not accept your &6&lClan Invite &ein time"
                                                        ));
                                                        targetStats.sendMessage(Utils.translate(
                                                                "&6You did not accept &6" + player.getName() + "&e's &6&lClan Invite &ein time"
                                                        ));

                                                        // remove old invite
                                                        clan.removeInvite(targetStats.getName());
                                                    }
                                                }
                                                // 20 seconds to accept invite
                                            }.runTaskLater(Parkour.getPlugin(), 20 * 30);
                                        }
                                        else
                                            player.sendMessage(Utils.translate("&cYou cannot invite anymore people to your clan! Max - " + maxMembers));
                                    }
                                    else
                                        player.sendMessage(Utils.translate("&cYou have already sent an invite to &4" + targetStats.getName()));
                                }
                                else
                                    player.sendMessage(Utils.translate("&4" + targetStats.getName() + " &cis already in a clan"));
                            }
                            else
                                player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cOnly the owner can invite others"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou have to be in a clan to invite someone!"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("accept"))
                {
                    // make sure they are not in a clan
                    if (clan == null)
                    {
                        PlayerStats targetStats = Parkour.getStatsManager().getByName(a[1]);

                        if (targetStats != null)
                        {
                            Clan targetClan = targetStats.getClan();

                            if (targetClan != null)
                            {
                                // if they are invited
                                if (targetClan.isInvited(player.getName()))
                                {
                                    if (targetClan.numMembers() < targetClan.getMaxMembers())
                                    {
                                        clansManager.addMember(targetClan, targetStats);
                                        sendClanMessage(targetClan, "&6" + player.getName() + " &ehas joined your clan!", false, player);
                                        player.sendMessage(Utils.translate("&eYou joined the &6&lClan &c" + targetClan.getTag()));
                                    }
                                    else
                                        player.sendMessage(Utils.translate("&cThat clan is full!"));
                                }
                                else
                                    player.sendMessage(Utils.translate("&eYou do not have an invite from &6Clan &c" + targetClan.getTag()));
                            }
                            else
                                player.sendMessage(Utils.translate("&6" + a[1] + " &eis not in a &6&lClan"));
                        }
                        else
                            player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot join a clan if you are in one"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("kick"))
                {
                    // kick if in clan

                    // make sure they are in a clan
                    if (clan != null)
                    {
                        // make sure they are owner of the clan
                        if (clan.isOwner(player.getName()))
                        {
                            String targetName = a[1];

                            // make sure they are not trying to kick themselves
                            if (!targetName.equalsIgnoreCase(player.getName()))
                            {
                                Clan targetClan = Parkour.getClansManager().getFromMember(targetName);

                                // if they do not have a clan stored in cache
                                if (targetClan != null)
                                {
                                    // make sure they are kicking from the same clan
                                    if (targetClan.equals(clan))
                                    {
                                        sendClanMessage(clan, "&6" + targetName + " &ehas been removed from the clan", true, player);
                                        clansManager.kickMember(clan, targetName);
                                    }
                                    else
                                        player.sendMessage(Utils.translate("&cYou are not in the same clan as &4" + targetName));
                                }
                                else
                                    player.sendMessage(Utils.translate("&4" + targetName + " &cis not in your clan"));
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou cannot kick yourself"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot kick a member from a clan you do not own!"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou are not in a clan!"));
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("disband"))
                {
                    // make sure they are in a clan
                    if (clan != null)
                    {
                        if (clan.getOwner().getName().equalsIgnoreCase(player.getName()))
                            // delete clan
                            clansManager.deleteClan(clan, true);
                        else
                            player.sendMessage(Utils.translate("&cYou cannot disband a clan you are not owner of"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot disband a clan if you are not in one"));
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("leave"))
                {
                    // leave if in a clan
                    if (clan != null)
                    {
                        // if they are only one in their clan
                        if (clan.numMembers() == 1)
                        {
                            clansManager.deleteClan(clan, false);
                            player.sendMessage(Utils.translate("&eYou have left your clan"));
                        }
                        else if (!clan.isOwner(player.getName()))
                        {
                            clansManager.leaveClan(clan, playerStats);
                            player.sendMessage(Utils.translate("&eYou have left your clan"));
                            sendClanMessage(clan, "&6" + player.getName() + " &ehas left your clan", false, player);
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot leave a clan you own"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou are not in a clan"));
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("delete"))
                {
                    if (player.hasPermission("rn-parkour.admin"))
                    {
                        Clan targetClan = clansManager.get(a[1]);

                        if (targetClan != null)
                        {
                            // remove from cache and db
                            clansManager.deleteClan(targetClan, false);
                            player.sendMessage(Utils.translate("&7You have deleted &c" + a[1] + " &7from the database"));
                        }
                        else
                            player.sendMessage(Utils.translate("&7Clan &c" + a[1] + " &7does not exist"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
                }
                else
                    sendHelp(sender);
            }
        }
        else
            sendHelp(sender);

        return true;
    }

    private static boolean clanTagRequirements(String clanTag, CommandSender sender)
    {
        // Clan Tag has improper length
        if (clanTag.length() < Parkour.getSettingsManager().clans_tag_length_min ||
            clanTag.length() > Parkour.getSettingsManager().clans_tag_length_max)
        {
            sender.sendMessage(Utils.translate("&c'&4" + clanTag + "&c' does not fit Clan Tag requirements"));
            sender.sendMessage(Utils.translate(
                    "&cClan Tags must be &4" + Parkour.getSettingsManager().clans_tag_length_min + "-" +
                         Parkour.getSettingsManager().clans_tag_length_max + " &ccharacters"
            ));
            return false;
        }
        else if (Parkour.getClansManager().get(clanTag) != null)
        {
            sender.sendMessage(Utils.translate("&cThe tag '&4" + clanTag + "&c' is already taken"));
            return false;
        }
        else if (Parkour.getSettingsManager().blocked_clan_names.contains(clanTag))
        {
            sender.sendMessage(Utils.translate("&cYou cannot use '&4" + clanTag + "&c' as a tag"));
            return false;
        }
        else
            return true;
    }

    private static void sendClanMessage(Clan targetClan, String message, boolean sendToSelf, Player self)
    {
        for (ClanMember clanMember : targetClan.getMembers())
        {
            // make sure they are online
            Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

            if (clanPlayer != null)
            {
                // if it will send to self, then do everyone
                if (sendToSelf)
                    clanPlayer.sendMessage(Utils.translate(message));
                // otherwise make sure it is not the same person
                else if (self != null && clanPlayer.getName() != self.getName())
                    clanPlayer.sendMessage(Utils.translate(message));
            }
        }
    }

    private static void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&3/clan stats <clan>  &7Display clan statistics"));
        sender.sendMessage(Utils.translate("&3/clan create <tag>  &7Create a clan &6" + Parkour.getSettingsManager().clans_price_create + " Coins"));
        sender.sendMessage(Utils.translate("&3/clan changetag <tag>  &7Change clan tag &6" + Parkour.getSettingsManager().clans_price_tag + " Coins"));
        sender.sendMessage(Utils.translate("&3/clan setowner <player>  &7Give your clan ownership"));
        sender.sendMessage(Utils.translate("&3/clan kick <player>  &7Kick player from your clan"));
        sender.sendMessage(Utils.translate("&3/clan invite <player>  &7Invite player to your clan"));
        sender.sendMessage(Utils.translate("&3/clan accept <player>  &7Accept invite from player"));
        sender.sendMessage(Utils.translate("&3/clan disband  &7Disband your clan"));
        sender.sendMessage(Utils.translate("&3/clan leave  &7Leave your clan"));
        sender.sendMessage(Utils.translate("&3/clan chat  &7Toggles clan chat"));

        if (sender.hasPermission("rn-parkour.admin"))
        {
            sender.sendMessage(Utils.translate("&3/clan setxp <clan> <xp>  &7Sets clan XP"));
            sender.sendMessage(Utils.translate("&3/clan setlevel <clan> <level>  &7Sets clan level"));
            sender.sendMessage(Utils.translate("&3/clan delete <clan>  &7Deletes the clan"));
            sender.sendMessage(Utils.translate("&3/clan settotalxp <clan>  &7Sets total XP of a clan"));
            sender.sendMessage(Utils.translate("&3/clan setmaxlevel <clan> <maxLevel>  &7Sets the new max level of a clan (min of 5, default)"));
            sender.sendMessage(Utils.translate("&3/clan setmaxmembers <clan> <maxMembers>  &7Sets the new max members of a clan (cannot set less than what they have)"));
            sender.sendMessage(Utils.translate("&3/clan addmaxlevel <playerName>  &7Adds 1 max level"));
            sender.sendMessage(Utils.translate("&3/clan addmaxmember <playerName>  &7Adds 1 max member"));
        }
        else if (sender.hasPermission("rn-parkour.staff"))
            sender.sendMessage(Utils.translate("&3/clan chatspy  &7Toggle clan chatspy"));
    }
}
