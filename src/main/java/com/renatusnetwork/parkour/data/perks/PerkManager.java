package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PerkManager
{
    private HashMap<String, Perk> perks;

    public PerkManager()
    {
        load();
    }

    public void load()
    {
        this.perks = PerksDB.getPerks();

        Parkour.getPluginLogger().info("Perks loaded: " + perks.size());
    }

    public Perk get(String perkName) {
        return perks.get(perkName);
    }

    public boolean exists(String perkName)
    {
        return perks.containsKey(perkName);
    }

    public void remove(Perk perk)
    {
        perks.remove(perk.getName());
        PerksDB.remove(perk.getName());
    }

    public void create(String perkName)
    {
        perks.put(perkName, new Perk(perkName));
        PerksDB.insert(perkName);
    }

    public void updateTitle(Perk perk, String title)
    {
        perk.setTitle(title);
        PerksDB.updateTitle(perk.getName(), title);
    }

    public void updatePrice(Perk perk, int price)
    {
        perk.setPrice(price);
        PerksDB.updatePrice(perk.getName(), price);
    }

    public void updateRequiredPermission(Perk perk, String requiredPermission)
    {
        perk.setRequiredPermission(requiredPermission);
        PerksDB.updateRequiredPermission(perk.getName(), requiredPermission);
    }

    public void updateInfiniteBlock(Perk perk, Material material)
    {
        perk.setInfiniteBlock(material);
        PerksDB.updateInfiniteBlock(perk.getName(), material.name());
    }

    public void bought(PlayerStats playerStats, Perk perk)
    {
        Long date = System.currentTimeMillis();

        playerStats.addPerk(perk.getName(), date);
        StatsDB.addOwnedPerk(playerStats, perk, date);
    }

    public void setPerk(Perk perk, PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        HashMap<PerksArmorType, ItemStack> items = perk.getItems();

        if (!items.isEmpty())
        {
            for (Map.Entry<PerksArmorType, ItemStack> entry : items.entrySet())
                switch (entry.getKey())
                {
                    case HELMET:
                        player.getInventory().setHelmet(entry.getValue());
                        break;
                    case CHESTPLATE:
                        player.getInventory().setChestplate(entry.getValue());
                        break;
                    case LEGGINGS:
                        player.getInventory().setLeggings(entry.getValue());
                        break;
                    case BOOTS:
                        player.getInventory().setBoots(entry.getValue());
                        break;
                }
        }
        else if (perk.isInfiniteBlock())
        {
            // update in both stats and db
            Parkour.getStatsManager().updateInfiniteBlock(playerStats, perk.getInfiniteBlock());
        }
    }

    public int numPerks()
    {
        return perks.size();
    }

    public Collection<Perk> getPerks()
    {
        return perks.values();
    }
}