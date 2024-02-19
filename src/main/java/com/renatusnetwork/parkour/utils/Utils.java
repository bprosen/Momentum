package com.renatusnetwork.parkour.utils;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<String> formatLore(List<String> loreList)
    {
        List<String> loreFormatted = new ArrayList<>();

        for (String lore : loreList)
            loreFormatted.add(translate(lore));

        return loreFormatted;
    }

    public static String getCoinFormat(int oldCoins, int newCoins)
    {
        return oldCoins != newCoins ? "&c&m" + Utils.formatNumber(oldCoins) + "&6 " + Utils.formatNumber(newCoins) : "&6" + Utils.formatNumber(oldCoins);
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

    public static boolean isFloat(String input) {
        try {
            Float.parseFloat(input);
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

    public static ItemStack getSwordIfExists(Player player)
    {
        return getItemStackIfExists(player, player.getInventory(), Parkour.getSettingsManager().sword_title);
    }

    public static ItemStack getShieldIfExists(Player player)
    {
        return getItemStackIfExists(player, player.getInventory(), Parkour.getSettingsManager().shield_title);
    }

    public static ItemStack getItemStackIfExists(Player player, Inventory inventory, String title)
    {
        ItemStack foundItem = null;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isItemFromTitle(offHand, title))
            foundItem = offHand;

        if (foundItem == null)
        {
            // try to find the sword in their inventory
            for (ItemStack item : inventory.getContents())
            {
                if (isItemFromTitle(item, title))
                {
                    foundItem = item;
                    break;
                }
            }
        }
        return foundItem;
    }

    public static int getSlotFromInventory(Inventory inventory, String title)
    {
        int maxSize = inventory.getSize();

        for (int slot = 0; slot < maxSize; slot++)
            if (isItemFromTitle(inventory.getItem(slot), title))
                return slot;

        return -1;
    }

    public static void addItemToHotbar(ItemStack item, Inventory inventory, int defaultSlot)
    {
        ItemStack itemInSlot = inventory.getItem(defaultSlot);

        // means something is filling it already
        if (itemInSlot != null && itemInSlot.getType() != Material.AIR)
        {
            // find space in hotbar
            for (int i = 0; i < 9; i++)
            {
                ItemStack invSlot = inventory.getItem(i);

                if (invSlot == null || invSlot.getType() == Material.AIR)
                {
                    inventory.setItem(i, item);
                    break;
                }
            }
        }
        else
            inventory.setItem(defaultSlot, item);
    }

    public static boolean isItemFromTitle(ItemStack item, String title)
    {
        return
            item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().equalsIgnoreCase(title);
    }

    public static ItemStack getPracPlateIfExists(Inventory inventory)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();
        return getItemStackIfExists(inventory, settingsManager.prac_item);
    }

    public static ItemStack getSpawnItemIfExists(Inventory inventory)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();
        return getItemStackIfExists(inventory, settingsManager.leave_item);
    }

    public static void removeSpawnItemIfExists(Player player, Inventory inventory)
    {
        ItemStack itemStack = Utils.getSpawnItemIfExists(player.getInventory());

        // remove if not null
        if (itemStack != null)
            player.getInventory().remove(itemStack);
    }

    private static ItemStack getItemStackIfExists(Inventory inventory, ItemStack searchItem)
    {

        ItemStack foundItem = null;

        if (searchItem != null && searchItem.hasItemMeta() && searchItem.getItemMeta().hasDisplayName())
        {
            // try to find the sword in their inventory
            for (ItemStack item : inventory.getContents())
            {
                if (item != null && item.getType() == searchItem.getType() && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                        item.getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(searchItem.getItemMeta().getDisplayName())))
                {
                    foundItem = item;
                    break;
                }
            }
        }
        return foundItem;
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
        for (Map.Entry<Integer, ItemStack> entry : Parkour.getSettingsManager().custom_join_inventory.entrySet())
            player.getInventory().setItem(entry.getKey(), entry.getValue());
    }

    public static void teleportToSpawn(PlayerStats playerStats) {
        Location loc = Parkour.getLocationManager().getLobbyLocation();
        Player player = playerStats.getPlayer();

        if (loc != null) {
            if (playerStats.isLoaded()) {
                if (!playerStats.isInTutorial()) {
                    if (!playerStats.isEventParticipant()) {
                        if (!playerStats.inRace()) {
                            if (!playerStats.isInInfinite()) {
                                if (!playerStats.isSpectating()) {

                                    BlackMarketManager blackMarketManager = Parkour.getBlackMarketManager();
                                    if (playerStats.isInBlackMarket())
                                        blackMarketManager.playerLeft(playerStats, false); // remove from event

                                    // toggle off elytra armor
                                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                                    player.teleport(loc);

                                    playerStats.resetPreviewLevel();
                                    playerStats.resetCurrentCheckpoint();
                                    PracticeHandler.resetDataOnly(playerStats);
                                    playerStats.resetLevel();
                                    playerStats.clearPotionEffects();

                                    if (playerStats.isAttemptingRankup())
                                        Parkour.getStatsManager().leftRankup(playerStats);

                                    if (playerStats.isAttemptingMastery())
                                        Parkour.getStatsManager().leftMastery(playerStats);

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
                        Parkour.getEventManager().removeParticipant(player, false); // remove if in event
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial, use &a/tutorial skip &cif you wish to skip"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
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

    public static void playSound(Sound sound)
    {
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    public static void spawnFirework(Location location, Color color, Color fadeColor, boolean secondDelay)
    {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.clearEffects();

        // build the firework and then set the new one
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.BURST)
                .withColor(color)
                .withFade(fadeColor)
                .build();

        meta.addEffect(effect);
        firework.setFireworkMeta(meta);

        if (secondDelay)
            new BukkitRunnable() {
            public void run() {
                firework.detonate();
            }
        }.runTaskLater(Parkour.getPlugin(), 20);
        else
            new BukkitRunnable() {
                public void run() {
                    firework.detonate();
                }
            }.runTaskLater(Parkour.getPlugin(), 1);
    }

    public static void addGlow(ItemMeta itemMeta)
    {
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public static void setDisabledPlayersItem(Inventory inventory, int slot)
    {
        ItemStack newItem = new ItemStack(Material.LEVER);
        ItemMeta meta = newItem.getItemMeta();
        meta.setDisplayName(Utils.translate("&7Players » &cDisabled"));
        newItem.setItemMeta(meta);
        inventory.setItem(slot, newItem);
    }

    public static void addSword(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack swordItem;
        LinkedHashMap<Integer, ItemStack> swords = settingsManager.setup_swords;

        // create item and give
        if (swords.containsKey(playerStats.getPrestiges()))
            swordItem = swords.get(playerStats.getPrestiges());
        else
            swordItem = swords.get(swords.size() - 1); // its linked so safe to assume

        Utils.addItemToHotbar(swordItem, player.getInventory(), settingsManager.sword_hotbar_slot);
        player.sendMessage(Utils.translate("&7You have been given a " + settingsManager.sword_title));
    }

    public static void addShield(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack shieldItem = new ItemStack(Material.SHIELD);
        ItemMeta meta = shieldItem.getItemMeta();
        meta.setDisplayName(Utils.translate(settingsManager.shield_title));

        if (playerStats.hasPrestiges())
            Utils.addGlow(meta);

        shieldItem.setItemMeta(meta);

        Utils.addItemToHotbar(shieldItem, player.getInventory(), settingsManager.shield_hotbar_slot);
        player.sendMessage(Utils.translate("&7You have been given a " + settingsManager.shield_title));
    }
}
