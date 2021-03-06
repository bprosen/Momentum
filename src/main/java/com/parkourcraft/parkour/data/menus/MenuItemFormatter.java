package com.parkourcraft.parkour.data.menus;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.perks.Perk;
import com.parkourcraft.parkour.data.rank.Rank;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Time;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
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
        if (menuItem.getType().equals("type") && menuItem.getTypeValue().equals("rankup"))
            return getRankUp(playerStats, menuItem);

        // Add in some '%player%' and such formatters for lore

        return menuItem.getItem();
    }

    private static ItemStack getPerk(Player player, PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        String perkName = menuItem.getTypeValue();
        Perk perk = Parkour.getPerkManager().get(perkName);

        if (perk != null) {
            ItemMeta itemMeta = item.getItemMeta();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            // Item Name Seciton (If not Perk Name, include Required Perk)
            if (menuItem.getFormattedTitle().equals(""))
                itemMeta.setDisplayName(perk.getFormattedTitle());
            else {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Perk Required"));
                itemLore.add("  " + perk.getFormattedTitle());
            }

            // Ownership Status Section
            itemLore.add("");
            if (perk.hasRequirements(playerStats, player))
                itemLore.add(Utils.translate("&2You own this perk"));
            else {
                itemLore.add(Utils.translate("&cYou do not own this perk"));

                // Click to Buy Section
                if (perk.getPrice() > 0) {
                    int playerBalance = (int) Parkour.getEconomy().getBalance(player);

                    if (playerBalance > perk.getPrice())
                        itemLore.add(Utils.translate("&7  Click to buy "));
                    else {
                        int requiredCoins = perk.getPrice() - playerBalance;

                        itemLore.add(Utils.translate("&7  Requires &6" + requiredCoins + " &7more &6Coins"));
                    }
                }
            }

            // Level Requirements Section
            List<String> requirements = perk.getRequirements();
            if (requirements.size() > 0
                    || perk.getPrice() > 0) {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Requirements"));

                for (String requirement : requirements) {
                    LevelObject level = Parkour.getLevelManager().get(requirement);

                    if (level != null)
                        itemLore.add(Utils.translate("&7 - " + level.getFormattedTitle()));
                }

                if (perk.getPrice() > 0)
                    itemLore.add(Utils.translate("&7 - Pay &6" + perk.getPrice() + " Coins"));
            }

            // Sections Over
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    private static ItemStack getRankUp(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        Rank rank = playerStats.getRank();

        if (rank != null) {
            // Existing Lore Section
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            itemMeta.setDisplayName(Utils.translate("&2Click to Rankup"));
            itemLore.add(Utils.translate("  &c&l" + rank.getRankTitle() + " &7-> &c&l" +
                                        Parkour.getRanksManager().get(rank.getRankId() + 1).getRankTitle()));
            itemLore.add(Utils.translate("  &7Cost of Rankup &6$" + (int) rank.getRankUpPrice()));
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    private static ItemStack getLevel(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        String levelName = menuItem.getTypeValue();
        LevelObject level = Parkour.getLevelManager().get(levelName);

        if (level != null) {
            ItemMeta itemMeta = item.getItemMeta();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            // Item Title Section
            String formattedTitle = level.getFormattedTitle();
            itemMeta.setDisplayName(formattedTitle);

            // Click To Go and Reward Section
            itemLore.add(Utils.translate("&7Click to go to " + formattedTitle
                         .replace("&l", "").replace("&o", "")));
            itemLore.add(Utils.translate("  &6" + Utils.formatNumber(level.getReward()) + " Coin &7Reward"));

            // Required Levels Section
            if (level.getRequiredLevels().size() > 0) {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Required Levels"));

                for (String requiredLevelName : level.getRequiredLevels()) {
                    LevelObject requiredLevel = Parkour.getLevelManager().get(requiredLevelName);

                    if (requiredLevel != null)
                        itemLore.add(Utils.translate("&7 - " + requiredLevel.getFormattedTitle()));
                }
            }

            // Personal Level Stats Section
            int levelCompletionsCount = playerStats.getLevelCompletionsCount(levelName);
            if (levelCompletionsCount > 0) {
                itemLore.add("");

                String beatenMessage = Utils.translate("&7Beaten &2" + levelCompletionsCount + " &7Time");
                if (levelCompletionsCount > 1)
                    beatenMessage += "s";

                itemLore.add(beatenMessage);

                List<LevelCompletion> bestLevelCompletions = playerStats.getQuickestCompletions(levelName);
                if (bestLevelCompletions.size() > 0) {
                    itemLore.add(Utils.translate("&7 Best Personal Time"));

                    double completionTime = ((double) bestLevelCompletions.get(0).getCompletionTimeElapsed()) / 1000;
                    long timeSince = System.currentTimeMillis() - bestLevelCompletions.get(0).getTimeOfCompletion();

                    itemLore.add(Utils.translate("  &2" + completionTime + "s"));

                    // this makes it so it will not have " ago" if they just completed it
                    String timeSinceString;
                    if (Time.elapsedShortened(timeSince).equalsIgnoreCase(""))
                        timeSinceString = Utils.translate("   &7Just now");
                    else
                        timeSinceString = Utils.translate("   &7" + Time.elapsedShortened(timeSince) + "ago");

                    itemLore.add(timeSinceString);
                }
            }
            // Sections over
            itemMeta.setLore((itemLore));
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}