package com.renatusnetwork.parkour.data.clans;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.ClanXPRewardEvent;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ClansManager
{
    private HashMap<String, Clan> clans;
    private ArrayList<Clan> clansLB;
    private HashMap<String, Clan> clanChat;
    private Set<String> chatSpy;

    public ClansManager(Plugin plugin)
    {
        this.clans = new HashMap<>();
        this.clansLB = new ArrayList<>(Parkour.getSettingsManager().max_clans_leaderboard_size);
        this.clanChat = new HashMap<>();
        this.chatSpy = new HashSet<>();

        load();
        startScheduler(plugin);
    }

    private void load()
    {
        clans = ClansDB.getClans();
    }

    public void create(String tag, PlayerStats owner)
    {
        Clan clan = new Clan(tag, owner.getUUID());

        // add to cache and db
        add(clan);
        ClansDB.insert(clan);

        // update owner stats
        clan.addMember(new ClanMember(owner.getUUID(), owner.getName()));
        owner.setClan(clan);
        StatsDB.updatePlayerClan(owner.getUUID(), clan.getTag());
    }

    public void add(Clan clan) {
        clans.put(clan.getTag(), clan);
    }

    public void updateClanTag(Clan clan, String newTag)
    {
        // update in data
        clans.remove(clan.getTag());
        clans.put(clan.getTag(), clan);
        clan.setTag(newTag);

        ClansDB.updateTag(clan.getTag(), newTag);
    }

    public void updateTotalXP(Clan clan, long totalXP)
    {
        clan.setTotalXP(totalXP);
        ClansDB.updateTotalXP(clan.getTag(), totalXP);
    }

    public void updateXP(Clan clan, int xp)
    {
        clan.setXP(xp);
        ClansDB.updateXP(clan.getTag(), xp);
    }

    public void updateLevel(Clan clan, int level)
    {
        clan.setLevel(level);
        ClansDB.updateLevel(clan.getTag(), level);
    }

    public void updateClanOwner(Clan clan, String uuid)
    {
        clan.setOwner(uuid);
        ClansDB.updateOwner(clan.getTag(), uuid);
    }

    public void addMember(Clan clan, PlayerStats playerStats)
    {
        clan.addMember(new ClanMember(playerStats.getUUID(), playerStats.getName()));
        playerStats.setClan(clan);
        StatsDB.updatePlayerClan(playerStats.getUUID(), clan.getTag());
        clan.removeInvite(playerStats.getName());
    }

    public void kickMember(Clan clan, String playerName)
    {
        clan.removeMember(playerName);
        StatsDB.resetPlayerClan(playerName);

        PlayerStats victimStats = Parkour.getStatsManager().getByName(playerName);

        if (victimStats != null)
            victimStats.resetClan();
    }

    public void remove(String clanTag) { clans.remove(clanTag); }

    public boolean existsIgnoreCase(String clanTag)
    {
        for (String tag : clans.keySet())
        {
            if (tag.equalsIgnoreCase(clanTag))
                return true;
        }
        return false;
    }

    public Clan getFromMember(String memberName)
    {
        for (Clan clan : clans.values())
            if (clan.isMember(memberName))
                return clan;

        return null;
    }

    public Clan get(String clanTag) {
        return clans.get(clanTag);
    }

    public Clan getIgnoreCase(String clanTag)
    {
        for (Clan clan : clans.values())
            if (clan.getTag().equalsIgnoreCase(clanTag))
                return clan;

        return null;
    }

    public void updatePlayerNameInClan(Clan clan, String oldName, String newName)
    {
        for (ClanMember clanMember : clan.getMembers())
            if (clanMember.getName().equalsIgnoreCase(oldName))
                clanMember.setName(newName);
    }

    public ArrayList<Clan> getLeaderboard() { return clansLB; }

    public void loadLeaderboard() {
        try {

            Clan highestXPClan = null;
            int lbSize = 0;
            clansLB.clear();

            while (Parkour.getSettingsManager().max_clans_leaderboard_size > lbSize) {
                // loop through and make sure they are not already added, and higher than previous
                for (Clan clan : clans.values())
                    if (!clansLB.contains(clan) &&
                        (highestXPClan == null || clan.getTotalXP() > highestXPClan.getTotalXP()))
                        highestXPClan = clan;

                if (highestXPClan != null)
                {
                    clansLB.add(highestXPClan);
                    highestXPClan = null;
                }
                lbSize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSplitClanReward(Clan clan, Player player, Level level, int reward) {

        double percentage = (double) clan.getLevel() / 100;
        double splitAmountPerMember = reward * percentage;

        for (ClanMember clanMember : clan.getMembers()) {
            // make sure it is not given to the completioner
            if (!clanMember.getName().equalsIgnoreCase(player.getName())) {
                // check if they are online
                if (Bukkit.getPlayer(UUID.fromString(clanMember.getUUID())) != null) {

                    Player onlineMember = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                    StatsManager statsManager = Parkour.getStatsManager();
                    PlayerStats memberStats = statsManager.get(onlineMember);

                    Parkour.getStatsManager().addCoins(memberStats, splitAmountPerMember);

                    onlineMember.sendMessage(Utils.translate("&6" + player.getDisplayName() + " &7completed &6" +
                            level.getTitle() + "&7 and you received &6" + Utils.formatNumber(splitAmountPerMember)
                            + " &eCoins &7from your clan"));
                }
                else
                {
                    double coins = StatsDB.getCoinsFromUUID(clanMember.getUUID());
                    StatsDB.updateCoins(clanMember.getUUID(), splitAmountPerMember + coins, true);
                }
            }
        }
    }

    public void doClanXPCalc(Clan clan, PlayerStats playerStats, int reward)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();
        int min = settingsManager.clan_calc_percent_min;
        int max = settingsManager.clan_calc_percent_max;

        // get random percent
        double percent = ThreadLocalRandom.current().nextInt(min, max) / 100.0;
        int clanXP = (int) (reward * percent);

        ClanXPRewardEvent event = new ClanXPRewardEvent(playerStats, clan, clanXP);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled())
        {
            clanXP = event.getXP(); // override from event

            if (playerStats.hasModifier(ModifierType.CLAN_XP_BOOSTER))
            {
                Booster booster = (Booster) playerStats.getModifier(ModifierType.CLAN_XP_BOOSTER);
                clanXP *= booster.getMultiplier();
            }
            int totalXP = clanXP + clan.getXP();

            // if max level, keep calculating xp
            if (clan.isMaxLevel())
            {
                clan.addXP(clanXP);
                ClansDB.updateXP(clan.getTag(), totalXP);

            // level them up
            }
            else if (totalXP > settingsManager.clan_level_xp_required.get(clan.getLevel()))
                processClanLevelUp(clan, totalXP);
            // add xp to clan
            else
            {
                // otherwise add xp to cache and database
                clan.addXP(clanXP);
                ClansDB.updateXP(clan.getTag(), totalXP);
            }
            long newTotal = clan.getTotalXP() + clanXP;

            // update total gained xp
            ClansDB.updateTotalXP(clan.getTag(), newTotal);
            clan.setTotalXP(newTotal);
        }
    }

    public void deleteClan(Clan clan, boolean messageMembers)
    {
        // iterate through existing clan members to reset/remove their data
        for (ClanMember clanMember : clan.getMembers())
        {
            // we do not need to delete from db for each player as the foreign key will set null on cascading
            PlayerStats clanPlayer = Parkour.getStatsManager().get(clanMember.getUUID());
            if (clanPlayer != null)
            {
                if (messageMembers)
                    clanPlayer.sendMessage(Utils.translate(
                            "&6" + clan.getOwner().getName() + " &ehas disbanded your &6&lClan &6" + clan.getTag()
                    ));

                // reset data on the players
                clanPlayer.resetClan();
            }
        }

        // remove from database and list
        ClansDB.remove(clan.getTag());
        clans.remove(clan.getTag());
    }

    public void leaveClan(Clan clan, PlayerStats playerStats)
    {
        // reset cache
        StatsDB.resetPlayerClan(playerStats.getName());
        clan.removeMember(playerStats.getName());

        playerStats.resetClan();
    }

    public void sendMessageToMembers(Clan clan, String msg, String dontSendTo)
    {
        for (ClanMember clanMember : clan.getMembers())
        {
            // make sure they are online
            Player clanPlayer = Bukkit.getPlayer(clanMember.getName());

            if (clanPlayer != null && (dontSendTo == null || !clanPlayer.getName().equalsIgnoreCase(dontSendTo)))
                clanPlayer.sendMessage(Utils.translate(msg));
        }
    }

    public void updateMaxLevel(Clan clan, int newMaxLevel)
    {
        int oldMaxLevel = clan.getMaxLevel();

        SettingsManager settingsManager = Parkour.getSettingsManager();

        if (newMaxLevel > settingsManager.clans_max_level)
            newMaxLevel = settingsManager.clans_max_level;

        if (newMaxLevel < 5) // default max level
            newMaxLevel = 5;

        if (oldMaxLevel != newMaxLevel)
        {
            // update in cache and config
            clan.setMaxLevel(newMaxLevel);
            ClansDB.updateMaxLevel(clan.getTag(), newMaxLevel);

            int currLevel = clan.getLevel();

            // process potential level up!
            if (currLevel == oldMaxLevel && currLevel < newMaxLevel) // means they were max but not anymore!
            {
                // means they can level up
                if (settingsManager.clan_level_xp_required.get(clan.getLevel()) <= clan.getXP())
                    processClanLevelUp(clan, clan.getXP());
            }
            else if (newMaxLevel < currLevel)
            {
                // means they went down in max level but were past the new max (force their level down)
                clan.setLevel(clan.getMaxLevel());
                ClansDB.updateLevel(clan.getTag(), clan.getLevel());
            }
        }
    }

    private void processClanLevelUp(Clan clan, int totalXP)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();

        // left over after level up
        int clanXPOverflow = totalXP - settingsManager.clan_level_xp_required.get(clan.getLevel());
        int newLevel = clan.getLevel() + 1;

        // this is the section that will determine if they will skip any levels
        for (int i = clan.getLevel(); i < clan.getMaxLevel(); i++)
        {
            // this means they are still above the next level amount
            if (clanXPOverflow >= settingsManager.clan_level_xp_required.get(newLevel))
            {
                // remove from overflow and add +1 level
                clanXPOverflow -= settingsManager.clan_level_xp_required.get(newLevel);
                newLevel++;
            }
            else
                break;
        }

        // if > or = max level, manually set jic
        if (newLevel >= clan.getMaxLevel())
            newLevel = clan.getMaxLevel();

        clan.setLevel(newLevel);
        sendMessageToMembers(clan, "&eYour clan has leveled up to &6Level " + newLevel, null);

        // play level up sound to online clan members
        for (ClanMember clanMember : clan.getMembers())
        {
            Player onlineMember = Bukkit.getPlayer(clanMember.getName());

            if (onlineMember != null)
                onlineMember.playSound(onlineMember.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.2f, 0f);
        }

        // add rest of xp after leveling up
        ClansDB.updateLevel(clan.getTag(), newLevel);
        ClansDB.updateXP(clan.getTag(), clanXPOverflow);
        clan.setXP(clanXPOverflow);
    }

    public void updateMaxMembers(Clan clan, int newMaxMembers)
    {
        if (newMaxMembers > Parkour.getSettingsManager().clans_max_members)
            newMaxMembers = Parkour.getSettingsManager().clans_max_members;

        if (newMaxMembers < 5) // default max members
            newMaxMembers = 5;

        // update in cache and config
        clan.setMaxMembers(newMaxMembers);
        ClansDB.updateMaxMembers(clan.getTag(), newMaxMembers);
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

    public Set<String> getChatSpyMap() { return chatSpy; }
}
