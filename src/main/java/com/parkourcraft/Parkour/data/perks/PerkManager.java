package com.parkourcraft.Parkour.data.perks;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PerkManager {

    private List<Perk> perks = new ArrayList<>();
    private Map<String, Integer> IDCache = new HashMap<>();

    public PerkManager(Plugin plugin) {
        load();

        Perks_DB.loadIDCache();

        startScheduler(plugin);
    }

    public void load() {
        for (String perkName : Perks_YAML.getNames())
            load(perkName);

        syncPermissions();
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            public void run() {
                Perks_DB.syncPerkIDs();
            }
        }, 0L, 4L);
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
            Perks_YAML.create(perkName);
    }

    public void load(String perkName) {
        boolean exists = exists(perkName);

        if (!Perks_YAML.exists(perkName)
                && exists)
            remove(perkName);
        else {
            Perk perk = new Perk(perkName);
            Perks_DB.syncID(perk);

            if (exists)
                remove(perkName);

            perks.add(perk);
        }
    }

    public void syncPermissions(Player player) {
        PlayerStats playerStats = Parkour.stats.get(player);

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
            Perks_DB.insertPerk(playerStats, perk, date);
        }
    }

}
