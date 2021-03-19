package com.parkourcraft.parkour.data.clans;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ClansManager {

    private List<Clan> clans = new ArrayList<>();

    public ClansManager(Plugin plugin) {
        load();

        startScheduler(plugin);
    }

    private void load() {
        clans = Clans_DB.getClans();

        Map<Integer, List<ClanMember>> members = Clans_DB.getMembers();

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
        clans.add(clan);
    }

    public void addMember(int clanID, ClanMember clanMember) {
        Clan clan = get(clanID);

        if (clan != null)
            clan.addMember(clanMember);
    }

    public void removeClan(int clanID) {
        Clan clan = get(clanID);

        if (clan != null)
            clans.remove(clan);
    }

    public Clan get(int clanID) {
        for (Clan clan : clans)
            if (clan.getID() == clanID)
                return clan;

        return null;
    }

    public Clan get(String clanTag) {
        for (Clan clan : clans)
            if (clan.getTag().equalsIgnoreCase(clanTag))
                return clan;

        return null;
    }

    public void doClanXPCalc(Clan clan, Player player, LevelObject levelObject) {
        int min = Parkour.getSettingsManager().clan_calc_percent_min;
        int max = Parkour.getSettingsManager().clan_calc_percent_max;

        // get random percent
        double percent = ThreadLocalRandom.current().nextInt(min, max) / 100.0;
        long clanXP = (long) (levelObject.getReward() * percent);
        long totalXP = clanXP + clan.getXP();

        // level them up
        if (totalXP > Clans_YAML.getLevelUpPrice(clan)) {

            // left over after level up
            long clanXPOverflow = totalXP - Clans_YAML.getLevelUpPrice(clan);
            int newLevel = clan.getLevel() + 1;

            // this is the section that will determine if they will skip any levels
            for (int i = clan.getLevel(); i <= Clans_YAML.getMaxLevel(); i++) {
                // this means they are still above the next level amount
                if (clanXPOverflow >= Clans_YAML.getLevelUpPrice(newLevel)) {

                    // remove from overflow and add +1 level
                    clanXPOverflow -= Clans_YAML.getLevelUpPrice(newLevel);
                    newLevel++;
                } else {
                    break;
                }
            }

            clan.setLevel(newLevel);

            // send announcement to all online clan members
            for (ClanMember clanMember : clan.getMembers()) {
                // make sure they are online
                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

                if (clanPlayer != null)
                    clanPlayer.sendMessage(Utils.translate("&eYour clan has leveled up to &6&lLevel " + newLevel));
            }
            // add rest of xp after leveling up
            Clans_DB.setClanLevel(newLevel, clan.getID());
            Clans_DB.setClanXP(clanXPOverflow, clan.getID());
            clan.setXP(clanXPOverflow);

        } else {

            // otherwise add xp to cache and database
            clan.addXP(clanXP);
            Clans_DB.setClanXP(totalXP, clan.getID());

            long clanXPNeeded = Clans_YAML.getLevelUpPrice(clan) - clan.getXP();

            for (ClanMember clanMember : clan.getMembers()) {
                // make sure they are online
                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));

                if (clanPlayer != null)
                    clanPlayer.sendMessage(Utils.translate("&6" + player.getName() + " &ehas gained &6&l" + clanXP
                            + " &eXP for your clan! &c(XP Needed to Level Up - &4" + clanXPNeeded + "&c)"));
            }
        }
    }

    public void deleteClan(int clanID) {
        Clan clan = get(clanID);

        if (clan != null) {
            // iterate through existing clan members to reset/remove their data
            for (ClanMember clanMember : clan.getMembers()) {

                // reset clan member in database
                Clans_DB.resetClanMember(clanMember.getPlayerName());

                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                if (clanPlayer != null) {
                    clanPlayer.sendMessage(Utils.translate("&6&l" + clan.getOwner().getPlayerName() +
                            " &ehas disbanded your &6&lClan &6" + clan.getTag()));

                    // reset data on the players
                    Parkour.getStatsManager().get(clanPlayer).resetClan();
                }
            }
            // remove from database and list
            Clans_DB.removeClan(clanID);
            clans.remove(clan);
        }
    }

    private void syncNewClans() {
        for (Clan clan : clans)
            if (clan.getID() == -1)
                Clans_DB.newClan(clan);
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                syncNewClans();
            }
        }, 0L, 5L);
    }
}
