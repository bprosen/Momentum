package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BlackMarketYAML
{
    private static FileConfiguration blackmarketConfig = Parkour.getConfigManager().get("blackmarket");

    private static void commit() {
        Parkour.getConfigManager().save("blackmarket");
    }

    public static boolean isSection(String pathName)
    {
        return blackmarketConfig.isConfigurationSection(pathName);
    }

    public static boolean isSet(String pathName, String valuePath)
    {
        return blackmarketConfig.isSet(pathName + "." + valuePath);
    }

    public static String chooseBankItem()
    {
        List<String> keys = new ArrayList<>(blackmarketConfig.getConfigurationSection("items").getKeys(false));

        return keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
    }

    public static String getTitle(String name)
    {
        return blackmarketConfig.getString("items." + name + ".title");
    }

    public static String getDescription(String name)
    {
        return blackmarketConfig.getString("items." + name + ".description");
    }

    public static ItemStack getItem(String name)
    {
        ItemStack item = new ItemStack(Material.matchMaterial(blackmarketConfig.getString("items." + name + ".item.material")));
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(Utils.translate(blackmarketConfig.getString("items." + name + ".item.title")));

        List<String> tempLore = blackmarketConfig.getStringList("items." + name + ".item.title");
        List<String> lore = new ArrayList<>();

        for (String loreString : tempLore)
            lore.add(Utils.translate(loreString));

        itemMeta.setLore(lore);

        if (blackmarketConfig.isSet("items." + name + ".item.glow"))
        {
            boolean glow = blackmarketConfig.getBoolean("items." + name + ".item.glow");

            if (glow)
            {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }

        item.setItemMeta(itemMeta);

        return item;
    }

    public static int getStartingBid(String name)
    {
        return blackmarketConfig.getInt("items." + name + ".starting_bid");
    }

    public static float getNextBidMultiplier(String name)
    {
        return (float) blackmarketConfig.getDouble("items." + name + ".next_bid_multiplier");
    }
}
