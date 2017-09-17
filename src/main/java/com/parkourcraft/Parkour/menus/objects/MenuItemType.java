package com.parkourcraft.Parkour.menus.objects;


import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.levels.LevelObject;
import com.parkourcraft.Parkour.stats.objects.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuItemType extends MenuItem {

    // itemType = level||teleport||open||display
    private String type;
    private String value;

    public MenuItemType(String itemType, String value) {
        this.type = itemType;
        this.value = value;
    }

    public ItemStack getFormattedItem(PlayerStats playerStats) {
        if (type.equals("level"))
            return getLevel(playerStats);
        else
            return getDisplay();
    }

    private ItemStack getLevel(PlayerStats playerStats) {
        ItemStack item = menuItem.getItem();

        if (LevelManager.levelConfigured(value)) {
            String levelName = value;
            LevelObject level = LevelManager.getLevel(levelName);

            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = itemMeta.getLore();

            itemMeta.setDisplayName(level.getTitleFormatted());

            itemLore.add(
                    ChatColor.GRAY + "Click to go to " +
                    level.getTitleFormatted().replace(ChatColor.BOLD + "", "")
            );

            itemLore.add(ChatColor.GRAY + "Level Reward " + ChatColor.GOLD + level.getReward() + " Coins");

            int levelCompletionsCount = playerStats.getLevelCompletionsCount(levelName);
            if (levelCompletionsCount > 0)
                itemLore.add(
                        ChatColor.GRAY + "You have beaten this level " +
                        ChatColor.GREEN + levelCompletionsCount +
                        ChatColor.GRAY + " times"
                );
        }

        return item;
    }

    private ItemStack getDisplay() {
        return menuItem.getItem();
    }

}
