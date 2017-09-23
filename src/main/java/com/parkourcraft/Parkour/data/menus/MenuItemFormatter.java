package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuItemFormatter {

    public static ItemStack format(PlayerStats playerStats, MenuItem menuItem) {
        if (menuItem.getType().equals("level"))
            return getLevel(playerStats, menuItem);

        // Add in some '%player%' and such formatters for lore

        return menuItem.getItem();
    }

    private static ItemStack getLevel(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = menuItem.getItem();
        String levelName = menuItem.getTypeValue();

        if (LevelManager.exists(levelName)) {
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

}
