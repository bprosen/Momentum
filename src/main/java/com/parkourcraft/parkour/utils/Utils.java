package com.parkourcraft.parkour.utils;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Utils {

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

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
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

    public static String translate(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
