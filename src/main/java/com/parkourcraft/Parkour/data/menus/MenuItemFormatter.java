package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.PerkManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Time;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MenuItemFormatter {

    public static ItemStack format(Player player, PlayerStats playerStats, MenuItem menuItem) {
        if (menuItem.getType().equals("level"))
            return getLevel(playerStats, menuItem);
        if (menuItem.getType().equals("perk"))
            return getPerk(player, playerStats, menuItem);

        // Add in some '%player%' and such formatters for lore

        return menuItem.getItem();
    }

    private static ItemStack getPerk(Player player, PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = menuItem.getItem();
        String perkName = menuItem.getTypeValue();
        Perk perk = PerkManager.get(perkName);

        if (perk != null) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            String formattedTitle = perk.getFormattedTitle();
            itemMeta.setDisplayName(formattedTitle);

            List<String> requirements = perk.getRequirements();
            if (requirements.size() > 0) {
                itemLore.add("");
                itemLore.add(ChatColor.GRAY + "Level Requirements");

                for (String requirement : requirements) {
                    LevelObject level = LevelManager.get(requirement);

                    if (level!=null)
                        itemLore.add(ChatColor.GRAY + " - " + level.getFormattedTitle());
                }
            }

            if (perk.getPrice() > 0) {
                itemLore.add("");
                itemLore.add(
                        ChatColor.YELLOW + "Price "
                        + ChatColor.GOLD + perk.getPrice() + ChatColor.BOLD + " Coins"
                );
            }

            itemLore.add("");
            if (perk.hasRequirements(playerStats))
                itemLore.add(ChatColor.GREEN + "You own this perk");
            else {
                itemLore.add(ChatColor.RED + "You do not own this perk");

                if (perk.getPrice() > 0) {
                    int playerBalance = (int) Parkour.economy.getBalance(player);

                    if (playerBalance > perk.getPrice())
                        itemLore.add(ChatColor.GRAY + "  Click to buy ");
                    else {
                        int requiredCoins = perk.getPrice() - playerBalance;

                        itemLore.add(
                                ChatColor.GRAY + "  Requires "
                                + ChatColor.GOLD + "" + requiredCoins
                                + ChatColor.GRAY + " more "
                                + ChatColor.GOLD + "Coins"
                        );
                    }
                }
            }

            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    private static ItemStack getLevel(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = menuItem.getItem();
        String levelName = menuItem.getTypeValue();
        LevelObject level = LevelManager.get(levelName);

        if (level != null) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>();

            String formattedTitle = level.getFormattedTitle();
            itemMeta.setDisplayName(formattedTitle);

            itemLore.add(
                    ChatColor.GRAY + "Click to go to " +
                            formattedTitle
                                    .replace(ChatColor.BOLD + "", "")
                                    .replace(ChatColor.ITALIC + "", "")
            );

            itemLore.add("  " + ChatColor.GOLD + level.getReward() + " Coin " + ChatColor.GRAY + "Reward");

            int levelCompletionsCount = playerStats.getLevelCompletionsCount(levelName);
            if (levelCompletionsCount > 0) {
                itemLore.add("");

                String beatenMessage = ChatColor.GRAY + "Beaten " + ChatColor.GREEN + levelCompletionsCount
                        + ChatColor.GRAY + " Time";
                if (levelCompletionsCount > 1)
                    beatenMessage += "s";

                itemLore.add(beatenMessage);

                List<LevelCompletion> bestLevelCompletions = playerStats.getQuickestCompletions(levelName);
                if (bestLevelCompletions.size() > 0) {
                    itemLore.add(ChatColor.GRAY + " Top Personal Times");

                    for (int i = 0; i <= (bestLevelCompletions.size()) - 1 && i <= 2; i++ ) {
                        double completionTime = ((double) bestLevelCompletions.get(i).getCompletionTimeElapsed()) / 1000;
                        long timeSince = System.currentTimeMillis() - bestLevelCompletions.get(i).getTimeOfCompletion();

                        itemLore.add("  " + ChatColor.GREEN + Double.toString(completionTime) + "s");
                        itemLore.add("   " + ChatColor.GRAY + Time.elapsedShortened(timeSince) + "ago");
                    }
                }
            }

            itemMeta.setLore((itemLore));
            item.setItemMeta(itemMeta);
        }

        return item;
    }

}
