package com.parkourcraft.parkour.data.perks;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PerkManager {

    private List<Perk> perks = new ArrayList<>();
    private Map<String, Integer> IDCache;

    public PerkManager(Plugin plugin) {
        this.IDCache = PerksDB.getIDCache();
        load();
        startScheduler(plugin);
    }

    public void load() {
        for (String perkName : PerksYAML.getNames())
            load(perkName);

        Parkour.getPluginLogger().info("Perks loaded: " + perks.size());

        syncPermissions();
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                if (PerksDB.syncPerkIDs()) {
                    Parkour.getPerkManager().setIDCache(PerksDB.getIDCache());
                    PerksDB.syncIDCache();
                }
            }
        }, 0L, 10L);
    }

    public void setIDCache(Map<String, Integer> IDCache) {
        this.IDCache = IDCache;
    }

    public Map<String, Integer> getIDCache() {
        return IDCache;
    }

    public Perk get(String perkName) {
        for (Perk perk : perks)
            if (perk.getName().equals(perkName))
                return perk;

        return null;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public boolean exists(String perkName) {
        return get(perkName) != null;
    }

    public void remove(String perkName) {
        for (Iterator<Perk> iterator = perks.iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(perkName))
                iterator.remove();
    }

    public void create(String perkName) {
        if (!exists(perkName))
            PerksYAML.create(perkName);
    }

    public void load(String perkName) {
        boolean exists = exists(perkName);

        if (!PerksYAML.exists(perkName)
                && exists)
            remove(perkName);
        else {
            Perk perk = new Perk(perkName);
            PerksDB.syncIDCache(perk, IDCache);

            if (exists)
                remove(perkName);

            perks.add(perk);
        }
    }

    public void syncPermissions(Player player) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        for (Perk perk : perks) {
            boolean hasRequirements = perk.hasRequirements(playerStats, player);

            for (String permission : perk.getPermissions())
                player.addAttachment(Parkour.getPlugin(), permission, hasRequirements);
        }
    }

    private void syncPermissions() {
        for (Player player : Bukkit.getOnlinePlayers())
            syncPermissions(player);
    }

    public void bought(PlayerStats playerStats, Perk perk) {
        if (perk.getID() > 0) {
            Long date = System.currentTimeMillis();

            playerStats.addPerk(perk.getName(), date);
            PerksDB.insertPerk(playerStats, perk, date);
        }
    }

}
