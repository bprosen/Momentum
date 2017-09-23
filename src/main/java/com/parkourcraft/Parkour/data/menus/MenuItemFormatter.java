package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Time;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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
            if (itemLore == null)
                itemLore = new ArrayList<>();

            String formattedTitle = level.getFormattedTitle();
            itemMeta.setDisplayName(formattedTitle);

            itemLore.add(
                    ChatColor.GRAY + "Click to go to " +
                            formattedTitle.replace(ChatColor.BOLD + "", "")
            );

            itemLore.add(ChatColor.GRAY + "Reward " + ChatColor.GOLD + level.getReward() + " Coins");

            int levelCompletionsCount = playerStats.getLevelCompletionsCount(levelName);
            if (levelCompletionsCount > 0) {
                itemLore.add("");

                itemLore.add(
                        ChatColor.GRAY + "Times completed " +
                                ChatColor.GREEN + levelCompletionsCount
                );

                List<LevelCompletion> bestLevelCompletions = playerStats.getQuickestCompletions(levelName);
                if (bestLevelCompletions.size() > 0) {
                    itemLore.add(ChatColor.GRAY + "Quickest times");

                    for (int i = 0; i < bestLevelCompletions.size() - 1 && i < 3; i++ ) {
                        double completionTime = ((double) bestLevelCompletions.get(i).getCompletionTimeElapsed()) / 1000;
                        Parkour.getPluginLogger().info(bestLevelCompletions.get(i).getCompletionTimeElapsed() + "");
                        long timeSince = System.currentTimeMillis() - bestLevelCompletions.get(i).getTimeOfCompletion();

                        itemLore.add(
                                "  " + ChatColor.GREEN + Double.toString(completionTime) + "s" +
                                        ChatColor.GRAY + ", "+ Time.elapsedShortened(timeSince) + " ago"
                        );
                    }
                }
            }

            itemMeta.setLore((itemLore));
            item.setItemMeta(itemMeta);
        }

        return item;
    }

}
