package com.renatusnetwork.parkour.data.clans;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.ClanXPRewardEvent;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ClansManager {

    private HashMap<String, Clan> clans = new HashMap<>();
    private HashMap<Integer, Clan> clansLeaderboard = new HashMap<>(Parkour.getSettingsManager().max_clans_leaderboard_size);
    private HashMap<String, Clan> clanChat = new HashMap<>();
    private Set<String> chatSpy = new HashSet<>();

    public ClansManager(Plugin plugin) {
        load();

        startScheduler(plugin);
    }

    private void load() {
        clans = ClansDB.getClans();

        HashMap<Integer, List<ClanMember>> members = ClansDB.getMembers();

        syncMembers(members);
    }

    private void syncMembers(Map<Integer, List<ClanMember>> members) {
        for (Map.Entry<Integer, List<ClanMember>> entry : members.entrySet()) {
            Clan clan = get(entry.getKey());

            if (clan != null)
                for (ClanMember member : entry.getValue())
                    clan.addMember(member);
        }
    }

    public void add(Clan clan) {
        clans.put(clan.getTag(), clan);
    }

    public void addMember(int clanID, ClanMember clanMember) {
        Clan clan = get(clanID);

        if (clan != null)
            clan.addMember(clanMember);
    }

    public void remove(String clanTag) { clans.remove(clanTag); }

    public Clan get(int clanID) {
        for (Clan clan : clans.values())
            if (clan.getID() == clanID)
                return clan;

        return null;
    }

    public Clan getFromMember(String clanMember) {
        for (Clan clan : clans.values())
            for (ClanMember member : clan.getMembers())
                if (member.getPlayerName().equalsIgnoreCase(clanMember))
                    return clan;

        return null;
    }

    public Clan get(String clanTag) {
        return clans.get(clanTag);
    }

    public Clan getIgnoreCase(String clanTag) {
        for (Clan clan : clans.values())
            if (clan.getTag().equalsIgnoreCase(clanTag))
                return clan;

        return null;
    }

    public void updatePlayerNameInClan(Clan clan, String oldName, String newName) {
        for (ClanMember clanMember : clan.getMembers())
            if (clanMember.getPlayerName().equalsIgnoreCase(oldName))
                clanMember.setPlayerName(newName);
    }

    public HashMap<Integer, Clan> getLeaderboard() { return clansLeaderboard; }

    public void loadLeaderboard() {
        try {

            Clan highestXPClan = null;
            Set<Clan> alreadyAddedClans = new HashSet<>();
            int lbSize = 0;
            clansLeaderboard.clear();
            int lbPos = 1;

            while (Parkour.getSettingsManager().max_clans_leaderboard_size > lbSize) {
                // loop through and make sure they are not already added, and higher than previous
                for (Clan clan : clans.values())
                    if (!alreadyAddedClans.contains(clan) &&
                        (highestXPClan == null || clan.getTotalGainedXP() > highestXPClan.getTotalGainedXP()))
                        highestXPClan = clan;

                if (highestXPClan != null) {
                    alreadyAddedClans.add(highestXPClan);
                    clansLeaderboard.put(lbPos, highestXPClan);
                    highestXPClan = null;
                    lbPos++;
                }
                lbSize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSplitClanReward(Clan clan, Player player, Level level) {

        double percentage = (double) clan.getLevel() / 100;
        double splitAmountPerMember = level.getReward() * percentage;

        for (ClanMember clanMember : clan.getMembers()) {
            // make sure it is not given to the completioner
            if (!clanMember.getPlayerName().equalsIgnoreCase(player.getName())) {
                // check if they are online
                if (Bukkit.getPlayer(UUID.fromString(clanMember.getUUID())) != null) {

                    Player onlineMember = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                    StatsManager statsManager = Parkour.getStatsManager();
                    PlayerStats memberStats = statsManager.get(onlineMember);

                    Parkour.getStatsManager().addCoins(memberStats, splitAmountPerMember);

                    onlineMember.sendMessage(Utils.translate("&6" + player.getName() + " &ehas completed &6" +
                            level.getFormattedTitle() + " &eand you received &6" + (percentage * 100) + "%" +
                            " &eof the reward! &6" + Utils.formatNumber(splitAmountPerMember) + " &eCoins"));
                }
                else
                {
                    double coins = StatsDB.getCoinsFromUUID(clanMember.getUUID());
                    StatsDB.updateCoinsUUID(clanMember.getUUID(), splitAmountPerMember + coins);
                }
            }
        }
    }

    public void doClanXPCalc(Clan clan, Player player, Level level)
    {

        int min = Parkour.getSettingsManager().clan_calc_percent_min;
        int max = Parkour.getSettingsManager().clan_calc_percent_max;

        // get random percent
        double percent = ThreadLocalRandom.current().nextInt(min, max) / 100.0;
        int clanXP = (int) (level.getReward() * percent);

        ClanXPRewardEvent event = new ClanXPRewardEvent(player, clan, clanXP);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled())
        {
            clanXP = event.getXP(); // override from event

            int totalXP = clanXP + clan.getXP();

            // if max level, keep calculating xp
            if (clan.isMaxLevel()) {
                clan.addXP(clanXP);
                ClansDB.setClanXP(totalXP, clan.getID());
                sendMessageToMembers(clan, "&6" + player.getName() + " &ehas gained &6&l" +
                        Utils.formatNumber(clanXP) + " &eXP for your clan!" +
                        " Total XP &6&l" + Utils.shortStyleNumber(clan.getTotalGainedXP()), null);

                // level them up
            } else if (totalXP > ClansYAML.getLevelUpPrice(clan)) {

                // left over after level up
                int clanXPOverflow = totalXP - ClansYAML.getLevelUpPrice(clan);
                int newLevel = clan.getLevel() + 1;

                // this is the section that will determine if they will skip any levels
                for (int i = clan.getLevel(); i < ClansYAML.getMaxLevel(); i++) {
                    // this means they are still above the next level amount
                    if (clanXPOverflow >= ClansYAML.getLevelUpPrice(newLevel)) {

                        // remove from overflow and add +1 level
                        clanXPOverflow -= ClansYAML.getLevelUpPrice(newLevel);
                        newLevel++;
                    } else {
                        break;
                    }
                }

                // if > or = max level, manually set jic
                if (newLevel >= ClansYAML.getMaxLevel())
                    newLevel = ClansYAML.getMaxLevel();

                clan.setLevel(newLevel);
                sendMessageToMembers(clan, "&eYour clan has leveled up to &6&lLevel " + newLevel, null);

                // play level up sound to online clan members
                for (ClanMember clanMember : clan.getMembers()) {
                    Player onlineMember = Bukkit.getPlayer(clanMember.getPlayerName());

                    if (onlineMember != null)
                        onlineMember.playSound(onlineMember.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.2f, 0f);
                }

                // add rest of xp after leveling up
                ClansDB.setClanLevel(newLevel, clan.getID());
                ClansDB.setClanXP(clanXPOverflow, clan.getID());
                clan.setXP(clanXPOverflow);

                // add xp to clan
            } else {
                // otherwise add xp to cache and database
                clan.addXP(clanXP);
                ClansDB.setClanXP(totalXP, clan.getID());

                long clanXPNeeded = ClansYAML.getLevelUpPrice(clan) - clan.getXP();

                sendMessageToMembers(clan, "&6" + player.getName() + " &ehas gained &6&l" +
                        Utils.formatNumber(clanXP) + " &eXP for your clan! &c(XP Needed to Level Up - &4" +
                        Utils.formatNumber(clanXPNeeded) + "&c)", null);
            }
            // update total gained xp
            ClansDB.setTotalGainedClanXP(clan.getTotalGainedXP() + clanXP, clan.getID());
            clan.setTotalGainedXP(clan.getTotalGainedXP() + clanXP);
        }
    }

    public void deleteClan(int clanID, boolean messageMembers) {
        Clan clan = get(clanID);

        if (clan != null) {
            // iterate through existing clan members to reset/remove their data
            for (ClanMember clanMember : clan.getMembers()) {

                // reset clan member in database
                ClansDB.resetClanMember(clanMember.getPlayerName());

                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                if (clanPlayer != null) {

                    if (messageMembers) {
                        clanPlayer.sendMessage(Utils.translate("&6&l" + clan.getOwner().getPlayerName() +
                                " &ehas disbanded your &6&lClan &6" + clan.getTag()));
                    }

                    // reset data on the players
                    Parkour.getStatsManager().get(clanPlayer).resetClan();
                }
            }
            // remove from database and list
            ClansDB.removeClan(clanID);
            clans.remove(clan.getTag());
        }
    }

    public void sendMessageToMembers(Clan clan, String msg, String dontSendTo) {
        for (ClanMember clanMember : clan.getMembers()) {
            // make sure they are online
            Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

            if (clanPlayer != null)
                if (dontSendTo != null && clanPlayer.getName().equalsIgnoreCase(dontSendTo))
                    continue;
                else
                    clanPlayer.sendMessage(Utils.translate(msg));
        }
    }

    private void startScheduler(Plugin plugin) {
        // load clans leaderboard in async every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 10, 20 * 180);
    }

    public boolean isInClanChat(String playerName) {
        return clanChat.containsKey(playerName);
    }

    public void toggleClanChat(String playerName, Clan clan) {
        if (isInClanChat(playerName) || clan == null)
            clanChat.remove(playerName);
        else
            clanChat.put(playerName, clan);
    }

    public boolean isInChatSpy(String playerName) {
        return chatSpy.contains(playerName);
    }

    public void toggleChatSpy(String playerName, boolean disconnected) {
        if (isInChatSpy(playerName) || disconnected)
            chatSpy.remove(playerName);
        else
            chatSpy.add(playerName);
    }

    public HashMap<String, Clan> getClans() { return clans; }

    public HashMap<String, Clan> getClanChatMap() { return clanChat; }

    public Set<String> getChatSpyMap() { return chatSpy; }
}
