package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
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

        if (LevelManager.exists(value)) {
            String levelName = value;
            LevelObject level = LevelManager.getLevel(levelName);

            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = itemMeta.getLore();

            itemMeta.setDisplayName(level.getFormattedTitle());

            itemLore.add(
                    ChatColor.GRAY + "Click to go to " +
                    level.getFormattedTitle().replace(ChatColor.BOLD + "", "")
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
