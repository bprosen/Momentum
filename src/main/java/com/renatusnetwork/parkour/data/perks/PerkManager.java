package com.renatusnetwork.parkour.data.perks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public void addRequiredLevel(Perk perk, Level level)
    {
        perk.addRequiredLevel(level);
        PerksDB.insertRequiredLevel(perk.getName(), level.getName());
    }

    public void removeRequiredLevel(Perk perk, Level level)
    {
        perk.removeRequiredLevel(level);
        PerksDB.removeRequiredLevel(perk.getName(), level.getName());
    }

    public void addArmorPiece(Perk perk, PerksArmorType type, Material material)
    {
        perk.addArmorItem(type, new ItemStack(material));
        PerksDB.insertArmor(perk.getName(), type.name(), material.name());
    }

    public void updateArmorTitle(Perk perk, PerksArmorType type, String title)
    {
        ItemStack item = perk.getArmorPiece(type);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(title);
        item.setItemMeta(itemMeta);

        PerksDB.updateArmorTitle(perk.getName(), type.name(), title);
    }

    public boolean updateArmorGlow(Perk perk, PerksArmorType type)
    {
        ItemStack item = perk.getArmorPiece(type);
        ItemMeta itemMeta = item.getItemMeta();

        boolean glow = itemMeta.hasEnchant(Enchantment.DURABILITY);

        if (glow)
        {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        else
        {
            itemMeta.removeEnchant(Enchantment.DURABILITY);
            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);

        PerksDB.updateArmorGlow(perk.getName(), type.name());

        return glow; // return the value of the glow
    }

    public void updateArmorMaterial(Perk perk, PerksArmorType type, Material material)
    {
        ItemStack item = perk.getArmorPiece(type);
        ItemMeta itemMeta = item.getItemMeta();

        ItemStack newItem = new ItemStack(material);
        newItem.setItemMeta(itemMeta);

        perk.setArmorItem(type, newItem);
        PerksDB.updateArmorMaterial(perk.getName(), type.name(), material.name());
    }

    public void updateArmorMaterialType(Perk perk, PerksArmorType type, int typeNum)
    {
        ItemStack item = perk.getArmorPiece(type);
        ItemMeta itemMeta = item.getItemMeta();

        ItemStack newItem = new ItemStack(item.getType(), 1, (short) typeNum);
        newItem.setItemMeta(itemMeta);

        perk.setArmorItem(type, newItem);

        PerksDB.updateArmorMaterialType(perk.getName(), type.name(), typeNum);
    }

    public void updateRequiresMasteryLevels(Perk perk)
    {
        perk.toggleRequiresMasteryLevels();
        PerksDB.updateRequiresMasteryLevels(perk.getName());
    }

    public void removeArmorPiece(Perk perk, PerksArmorType type)
    {
        perk.removeArmorItem(type);
        PerksDB.removeArmor(perk.getName(), type.name());
    }

    public void bought(PlayerStats playerStats, Perk perk)
    {
        playerStats.addPerk(perk);
        StatsDB.addBoughtPerk(playerStats.getUUID(), perk.getName());
    }

    public void setPerk(Perk perk, PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        HashMap<PerksArmorType, ItemStack> items = perk.getArmorItems();

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