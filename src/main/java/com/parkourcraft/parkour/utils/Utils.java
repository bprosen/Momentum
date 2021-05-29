package com.parkourcraft.parkour.utils;

import org.bukkit.*;
import java.math.BigDecimal;
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

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
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

    public static String formatNumber(double amount) {

        double newAmount = Double.valueOf(new BigDecimal(amount).toPlainString());
        // cannot cast java.lang.Double, need to cast primitive type
        int intAmount = (int) newAmount;
        // this makes it seperate digits by commands and .2 means round decimal by 2 places
        return String.format("%,d", intAmount);
    }

    public static String formatDecimal(double amount) {
        double newAmount = Double.valueOf(new BigDecimal(amount).toPlainString());
        // this makes it seperate digits by commands and .2 means round decimal by 2 places
        return String.format("%,.2f", newAmount);
    }

    public static String shortStyleNumber(double amount) {
        return amount < 1000 ? String.valueOf((int) amount) : formatDecimal(amount / 1000) + "k";
    }

    public static String translate(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
