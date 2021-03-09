package com.parkourcraft.parkour.data.clans;

import com.parkourcraft.parkour.Parkour;
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

    public void deleteClan(int clanID) {
        Clan clan = get(clanID);

        if (clan != null) {
            // iterate through existing clan members to reset/remove their data
            for (ClanMember clanMember : clan.getMembers()) {

                // reset clan member in database
                Clans_DB.resetClanMember(clanMember.getPlayerName());

                Player clanPlayer = Bukkit.getPlayer(UUID.fromString(clanMember.getUUID()));
                if (clanPlayer != null) {
                    clanPlayer.sendMessage(Utils.translate("&4" + clan.getOwner().getPlayerName() +
                            " &chas disbanded your &6&lClan &c" + clan.getTag()));

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
