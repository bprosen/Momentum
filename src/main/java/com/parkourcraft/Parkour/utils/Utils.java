package com.parkourcraft.Parkour.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static ItemStack convertItem(String itemMaterial, int itemType) {
        ItemStack item = new ItemStack(Material.getMaterial(itemMaterial), 1, (byte) itemType);

        return item;
    }

    public static List<String> getLoreFormatted(List<String> loreList) {
        List<String> loreFormatted = new ArrayList<>();

        for (String lore : loreList)
            loreFormatted.add(ChatColor.translateAlternateColorCodes('&', lore));

        return loreFormatted;
    }

}
