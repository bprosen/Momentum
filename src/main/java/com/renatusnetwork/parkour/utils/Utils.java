package com.renatusnetwork.parkour.utils;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        String finalChar = input.substring(input.length() - 1);

        // make exception for Rate menu as it can have numbers at the end due to level name in title
        if (!input.contains("Rate") && isInteger(finalChar))
            return Integer.parseInt(finalChar);

        return 1;
    }

    public static String formatNumber(double amount) {

        double newAmount = Double.valueOf(new BigDecimal(amount).toPlainString());
        // cannot cast java.lang.Double, need to cast primitive type
        int intAmount = (int) newAmount;
        // this makes it seperate digits by commas
        return String.format("%,d", intAmount);
    }

    public static String formatDecimal(double amount) {
        double newAmount = Double.valueOf(new BigDecimal(amount).toPlainString());
        // this makes it seperate digits by commands and .2 means round decimal by 2 places
        return String.format("%,.2f", newAmount);
    }

    public static String shortStyleNumber(double amount) {

        String result = String.valueOf((int) amount);

        if (amount >= 1000000.0)
            result = ((int) amount / 1000000) + "M";
        else if (amount >= 1000.0)
            result = ((int) amount / 1000) + "k";

        return result;
    }

    public static ItemStack getSwordIfExists(Inventory inventory) {
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack swordItem = null;

        // try to find the sword in their inventory
        for (ItemStack item : inventory.getContents()) {

            if (item != null &&
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(settingsManager.sword_title))) {

                swordItem = item;
                break;
            }
        }
        return swordItem;
    }

    public static ItemStack getPracPlateIfExists(Inventory inventory) {
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack pracItem = null;

        // try to find the sword in their inventory
        for (ItemStack item : inventory.getContents()) {

            if (item != null && item.getType() == settingsManager.prac_type &&
                    item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                    item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(settingsManager.prac_title))) {

                pracItem = item;
                break;
            }
        }
        return pracItem;
    }

    public static String translate(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static Color getColorFromString(String colorName) {
        if (colorName != null) {
            switch (colorName.toLowerCase()) {
                case "black":
                    return Color.BLACK;
                case "white":
                    return Color.WHITE;
                case "yellow":
                    return Color.YELLOW;
                case "navy":
                    return Color.NAVY;
                case "blue":
                    return Color.BLUE;
                case "fuchsia":
                    return Color.FUCHSIA;
                case "aqua":
                    return Color.AQUA;
                case "olive":
                    return Color.OLIVE;
                case "maroon":
                    return Color.MAROON;
                case "green":
                    return Color.GREEN;
                case "lime":
                    return Color.LIME;
                case "gray":
                    return Color.GRAY;
                case "orange":
                    return Color.ORANGE;
                case "red":
                    return Color.RED;
                case "silver":
                    return Color.SILVER;
                case "teal":
                    return Color.TEAL;
                case "purple":
                    return Color.PURPLE;
            }
        }
        return null;
    }

    public static void setHotbar(Player player)
    {
        // loop through and set
        for (Map.Entry<Integer, ItemStack> entry : Parkour.getSettingsManager().customJoinInventory.entrySet())
            player.getInventory().setItem(entry.getKey(), entry.getValue());
    }

    public static void teleportToSpawn(PlayerStats playerStats) {
        Location loc = Parkour.getLocationManager().getLobbyLocation();
        Player player = playerStats.getPlayer();

        if (loc != null) {

            if (!playerStats.isEventParticipant()) {
                if (!playerStats.inRace()) {
                    if (!playerStats.isInInfinitePK()) {
                        if (!playerStats.isSpectating()) {
                            // toggle off elytra armor
                            Parkour.getStatsManager().toggleOffElytra(playerStats);

                            player.teleport(loc);

                            playerStats.resetCurrentCheckpoint();
                            playerStats.resetPracticeMode();
                            playerStats.resetLevel();

                            playerStats.clearPotionEffects();

                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while spectating someone"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in infinite parkour"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            }
        } else {
            Parkour.getPluginLogger().info("Unable to teleport " + player.getName() + " to spawn, null location?");
        }
    }

    public static void broadcastClickableHoverableCMD(String message, String hoverMessage, String commandClick)
    {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(Utils.translate(message)));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate(hoverMessage))));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandClick));

        Bukkit.spigot().broadcast(component); // send clickable
    }
}
