package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PerkManager {

    private HashMap<String, Perk> perks = new HashMap<>();
    private HashMap<String, Integer> IDCache;

    public PerkManager(Plugin plugin) {
        this.IDCache = PerksDB.getIDCache();
        load();
        startScheduler(plugin);
    }

    public void load() {
        for (String perkName : PerksYAML.getNames())
            load(perkName);

        Parkour.getPluginLogger().info("Perks loaded: " + perks.size());
    }

    private void startScheduler(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (PerksDB.syncPerkIDs()) {
                    Parkour.getPerkManager().setIDCache(PerksDB.getIDCache());
                    PerksDB.syncIDCache();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 10L);
    }

    public void setIDCache(HashMap<String, Integer> IDCache) {
        this.IDCache = IDCache;
    }

    public Map<String, Integer> getIDCache() {
        return IDCache;
    }

    public Perk get(String perkName) {
        return perks.get(perkName);
    }

    public HashMap<String, Perk> getPerks() {
        return perks;
    }

    public boolean exists(String perkName) {
        return get(perkName) != null;
    }

    public void remove(String perkName) {
        for (Iterator<Perk> iterator = perks.values().iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(perkName))
                iterator.remove();
    }

    public void create(String perkName) {
        if (!exists(perkName))
            PerksYAML.create(perkName);
    }

    public void load(String perkName) {
        boolean exists = exists(perkName);

        if (!PerksYAML.exists(perkName) && exists)
            remove(perkName);
        else {
            Perk perk = new Perk(perkName);
            PerksDB.syncIDCache(perk, IDCache);

            if (exists)
                remove(perkName);

            perks.put(perkName, perk);
        }
    }

    public void bought(PlayerStats playerStats, Perk perk) {
        if (perk.getID() > 0) {
            Long date = System.currentTimeMillis();

            playerStats.addPerk(perk.getName(), date);
            PerksDB.insertPerk(playerStats, perk, date);
        }
    }

    public void setPerk(Perk perk, PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        HashMap<String, ItemStack> items = perk.getItems();
        if (!items.isEmpty())
        {
            for (Map.Entry<String, ItemStack> entry : items.entrySet())
                switch (entry.getKey()) {
                    case "head":
                        player.getInventory().setHelmet(entry.getValue());
                        break;
                    case "chest":
                        player.getInventory().setChestplate(entry.getValue());
                        break;
                    case "leg":
                        player.getInventory().setLeggings(entry.getValue());
                        break;
                    case "feet":
                        player.getInventory().setBoots(entry.getValue());
                        break;
                }
        }
        else if (perk.isInfinitePKBlock())
        {
            // update in both stats and db
            playerStats.setInfiniteBlock(perk.getInfinitePKBlock());
            Parkour.getDatabaseManager().add("UPDATE players SET infinite_block='" + perk.getInfinitePKBlock().name() + "' WHERE uuid='" + playerStats.getUUID() + "'");
        }
    }
}