package com.parkourcraft.Parkour.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static ItemStack convertItem(String itemMaterial, int itemType) {
        return new ItemStack(Material.getMaterial(itemMaterial), 1, (byte) itemType);
    }

    public static List<String> formatLore(List<String> loreList) {
        List<String> loreFormatted = new ArrayList<>();

        for (String lore : loreList)
            loreFormatted.add(ChatColor.translateAlternateColorCodes('&', lore));

        return loreFormatted;
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public static int getTrailingInt(String input) {
        input = ChatColor.stripColor(input);
        String finalChar = input.substring(input.length() - 1, input.length());

        if (isInteger(finalChar))
            return Integer.parseInt(finalChar);

        return 1;
    }

}
