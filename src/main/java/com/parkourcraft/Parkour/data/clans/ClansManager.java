package com.parkourcraft.Parkour.data.clans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class ClansManager {

    private List<Clan> clans = new ArrayList<>();

    public ClansManager(Plugin plugin) {
        load();

        startScheduler(plugin);
    }

    private void load() {
        Clans_DB.loadClans(clans);
        Clans_DB.loadMembers(this);
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
