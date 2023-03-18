package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCooldown;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.ranks.RanksYAML;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Time;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuItemFormatter {

    public static ItemStack format(Player player, PlayerStats playerStats, MenuItem menuItem) {
        if (menuItem.getType().equals("level"))
            return getLevel(playerStats, menuItem);
        if (menuItem.getType().equals("perk"))
            return getPerk(player, playerStats, menuItem);
        if (menuItem.getType().equals("open"))
            return enchantMenuItem(playerStats, menuItem, Parkour.getMenuManager().getMenu(menuItem.getTypeValue()));
        if (menuItem.getType().equals("type")) {
            // make coin rankup menu for /rankup if in stage 1
            if (menuItem.getTypeValue().equals("coin-rankup"))
                return getRankUp(playerStats, menuItem);
            else if (menuItem.getTypeValue().equals("featured-level"))
                return getFeaturedLevel(playerStats, menuItem);
            // make levels for /rankup if in stage 2
            else if (menuItem.getTypeValue().equals("rankup-level-1")
                    || menuItem.getTypeValue().equals("rankup-level-2")
                    || menuItem.getTypeValue().equals("rankup-level")) {

                ItemStack levelItem = getRankUpLevel(playerStats, menuItem);
                if (levelItem != null)
                    return levelItem;
            }
        }
        // Add in some '%player%' and such formatters for lore
        return menuItem.getItem();
    }

    //
    // Perk Section
    //
    private static ItemStack getPerk(Player player, PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        String perkName = menuItem.getTypeValue();
        Perk perk = Parkour.getPerkManager().get(perkName);

        if (perk != null) {
            ItemMeta itemMeta = item.getItemMeta();

            // if glowing, add glow effect
            if (menuItem.isGlowing()) {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());
            itemMeta.setDisplayName(perk.getFormattedTitle());

            // Ownership Status Section
            itemLore.add("");
            if (perk.hasRequiredPermissions(player) || perk.hasRequirements(playerStats, player))
                itemLore.add(Utils.translate("&2You own this perk"));
            else {
                itemLore.add(Utils.translate("&cYou do not own this perk"));

                // Click to Buy Section
                if (perk.getPrice() > 0) {
                    int playerBalance = (int) playerStats.getCoins();

                    if (playerBalance > perk.getPrice())
                        itemLore.add(Utils.translate("&7  Click to buy "));
                    else {
                        int requiredCoins = perk.getPrice() - playerBalance;

                        itemLore.add(Utils.translate("&7  Requires &6" + Utils.formatNumber(requiredCoins) + " &7more &6Coins"));
                    }
                }
            }

            // if it has shortened custom lore, add it, otherwise do normal lore
            if (perk.hasSetRequirementsLore()) {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Requirements"));

                // loop through if it has shortened lore and add it
                for (String shortLore : perk.getSetRequirementsLore())
                    itemLore.add(Utils.translate(shortLore));
            } else {
                // Level Requirements Section
                List<String> requirements = perk.getRequirements();
                if (requirements.size() > 0 || perk.getPrice() > 0) {
                    itemLore.add("");
                    itemLore.add(Utils.translate("&7Requirements"));

                    for (String requirement : requirements) {
                        Level level = Parkour.getLevelManager().get(requirement);

                        if (level != null)
                            itemLore.add(Utils.translate("&7 - " + level.getFormattedTitle()));
                    }

                    if (perk.getPrice() > 0)
                        itemLore.add(Utils.translate("&7 - Pay &6" + Utils.formatNumber(perk.getPrice()) + " Coins"));
                }
            }

            // Sections Over
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    //
    // Level + Rankup Section
    //
    private static ItemStack getRankUp(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        Rank rank = playerStats.getRank();

        if (rank != null) {
            // Existing Lore Section
            ItemMeta itemMeta = item.getItemMeta();
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            itemMeta.setDisplayName(Utils.translate("&2Click to enter Stage 2 of Rankup"));
            itemLore.add(Utils.translate("  &c&l" + rank.getRankTitle() + " &7-> &c&l" +
                    Parkour.getRanksManager().get(rank.getRankId() + 1).getRankTitle()));
            itemLore.add(Utils.translate("  &7Cost of Rankup to Stage 2 &6$" + Utils.formatNumber(rank.getRankUpPrice())));
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    private static ItemStack getLevel(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        String levelName = menuItem.getTypeValue();
        Level level = Parkour.getLevelManager().get(levelName);

        if (level != null) {
            // make it the featured in normal gui section too for consistency
            if (Parkour.getLevelManager().getFeaturedLevel().getName().equalsIgnoreCase(level.getName()))
                return getFeaturedLevel(playerStats, menuItem);
            else
                return createLevelItem(playerStats, level, menuItem, item, false);
        }
        return item;
    }

    // create a slightly different level item for featured level in gui
    private static ItemStack getFeaturedLevel(PlayerStats playerStats, MenuItem menuItem) {
        Level featuredLevel = Parkour.getLevelManager().getFeaturedLevel();
        ItemStack levelItem = menuItem.getItem();

        if (featuredLevel != null) {
            ItemMeta itemMeta = levelItem.getItemMeta();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>();

            // Item Title Section
            String formattedTitle = Utils.translate("&cFeatured &7- " + featuredLevel.getFormattedTitle());

            // add new if new level!
            if (featuredLevel.isNewLevel())
                formattedTitle = Utils.translate("&d&lNEW " + formattedTitle);

            if (featuredLevel.getPlayersInLevel() > 0)
                formattedTitle += Utils.translate(" &7(" + featuredLevel.getPlayersInLevel() + " Playing)");

            itemMeta.setDisplayName(formattedTitle);

            // Click To Go and Reward Section
            itemLore.add(Utils.translate("&7Click to go to " + featuredLevel.getFormattedTitle()
                    .replace("&l", "").replace("&o", "")));

            itemLore.add(Utils.translate("  &c&m" + Utils.formatNumber(featuredLevel.getReward()) +
                                        "&r &6" + Utils.formatNumber(featuredLevel.getReward() *
                                        Parkour.getSettingsManager().featured_level_reward_multiplier) +
                                        " &6Coin &7Reward"));

            if (featuredLevel.getTotalCompletionsCount() > 0)
                itemLore.add(Utils.translate("  &6" + Utils.shortStyleNumber(featuredLevel.getTotalCompletionsCount()) + " &7Completions"));

            // only show rating if above 5
            if (featuredLevel.getRatingsCount() >= 5) {
                itemLore.add(Utils.translate("  &6" + featuredLevel.getRating() + " &7Rating"));
                itemLore.add(Utils.translate("    &7Out of &e" + Utils.formatNumber(featuredLevel.getRatingsCount()) + " &7ratings"));
            }

            // Personal Level Stats Section
            int levelCompletionsCount = playerStats.getLevelCompletionsCount(featuredLevel.getName());
            if (levelCompletionsCount > 0) {
                // add glow effect to all levels they have completed
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                itemLore.add("");

                String beatenMessage = Utils.translate("&7Beaten &2" + Utils.formatNumber(levelCompletionsCount) + " &7Time");
                if (levelCompletionsCount > 1)
                    beatenMessage += "s";

                itemLore.add(beatenMessage);

                LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(featuredLevel.getName());
                if (fastestCompletion != null) {
                    itemLore.add(Utils.translate("&7 Best Personal Time"));

                    double completionTime = ((double) fastestCompletion.getCompletionTimeElapsed()) / 1000;
                    long timeSince = System.currentTimeMillis() - fastestCompletion.getTimeOfCompletion();

                    itemLore.add(Utils.translate("  &2" + completionTime + "s"));

                    // this makes it so it will not have " ago" if they just completed it
                    String timeSinceString;
                    if (Time.elapsedShortened(timeSince, false).equalsIgnoreCase(""))
                        timeSinceString = Utils.translate("   &7Just now");
                    else
                        timeSinceString = Utils.translate("   &7" + Time.elapsedShortened(timeSince, false) + "ago");

                    itemLore.add(timeSinceString);
                }
            }

            // Sections over
            itemMeta.setLore(itemLore);
            levelItem.setItemMeta(itemMeta);
        }
        return levelItem;
    }

    private static ItemStack enchantMenuItem(PlayerStats playerStats, MenuItem menuItem, Menu menu) {
        // get item and levels, clone so it can change properly
        ItemStack item = menuItem.getItem().clone();
        Set<Level> levelsInMenu = Parkour.getLevelManager().getLevelsFromMenu(menu);

        if (levelsInMenu != null && !levelsInMenu.isEmpty())
        {
            int count = 0;

            // more optimized: start as true and if a level is not completed, toggle to false and break
            boolean enchant = true;
            for (Level level : levelsInMenu)
            {
                if (playerStats.getLevelCompletionsCount(level.getName()) < 1)
                    enchant = false;
                else
                    count++;
            }


            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(Utils.translate(
                    itemMeta.getDisplayName() + " &7(&a" + (int) (((double) count / levelsInMenu.size()) * 100) + "%&7)"));

            // if enchanting, add durability and hide it for glow effect
            if (enchant)
            {
                itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    private static ItemStack getRankUpLevel(PlayerStats playerStats, MenuItem menuItem) {

        ItemStack item = new ItemStack(menuItem.getItem());
        String levelName = RanksYAML.getRankUpLevel(playerStats.getRank().getRankName(), menuItem.getTypeValue());

        if (levelName != null) {
            Level level = Parkour.getLevelManager().get(levelName);

            return createLevelItem(playerStats, level, menuItem, item, true);
        }
        return null;
    }

    private static ItemStack createLevelItem(PlayerStats playerStats, Level level, MenuItem menuItem, ItemStack item, boolean rankUpLevel) {

        if (level != null) {

            ItemMeta itemMeta = item.getItemMeta();
            String formattedTitle = level.getFormattedTitle();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            // do not show all the below info if not a rankup level
            if (!rankUpLevel) {

                // add new if new level!
                if (level.isNewLevel())
                    formattedTitle = Utils.translate("&d&lNEW " + formattedTitle);

                if (level.needsRank())
                {
                    if (!Parkour.getRanksManager().isPastRank(playerStats, level.getRequiredRank()))
                    {
                        // show whats required
                        Rank nextRank = Parkour.getRanksManager().getNextRank(level.getRequiredRank());
                        itemLore.add(Utils.translate("&cRequires " + nextRank.getRankTitle()));
                    }
                }
                // show they need to buy it
                if (level.getPrice() > 0 && !playerStats.hasBoughtLevel(level.getName()) && playerStats.getLevelCompletionsCount(level.getName()) <= 0)
                {
                    itemLore.add(Utils.translate("&7Click to buy " + level.getFormattedTitle() + " &7for &6" + Utils.formatNumber(level.getPrice()) + " &eCoins"));
                    itemLore.add(Utils.translate("&7You have &6" + Utils.formatNumber(playerStats.getCoins()) + " &eCoins"));
                }
                else
                    itemLore.add(Utils.translate("&7Click to go to " + level.getFormattedTitle()));

                // Item Title Section
                if (level.getPlayersInLevel() > 0)
                    formattedTitle += Utils.translate(" &7(" + level.getPlayersInLevel() + " Playing)");

                if (level.getDifficulty() > -1) // has difficulty
                {
                    String difficultyStr = "  ";
                    int difficulty = level.getDifficulty();

                    // determine what color we need to utilize
                    switch (difficulty)
                    {
                        case 10:
                        case 9:
                            difficultyStr += "&4";
                            break;
                        case 8:
                        case 7:
                            difficultyStr += "&c";
                            break;
                        case 6:
                        case 5:
                            difficultyStr += "&6";
                            break;
                        case 4:
                        case 3:
                            difficultyStr += "&e";
                            break;
                        case 2:
                        case 1:
                            difficultyStr += "&a";
                            break;
                    }

                    boolean pastDifficulty = false;

                    // difficulty goes up to 10
                    for (int i = 1; i <= 10; i++)
                    {
                        // if not past, and we go past difficulty, change to gray
                        if (!pastDifficulty && difficulty < i) {
                            pastDifficulty = true;
                            difficultyStr += "&f";
                        }
                        difficultyStr += "|";
                    }

                    itemLore.add(Utils.translate(difficultyStr + " &7Difficulty"));
                }

                int oldReward = level.getReward();
                int newReward = level.getReward();
                boolean modified = false;
                LevelCooldown cooldown = null;

                if (playerStats.getPrestiges() > 0 && level.getReward() > 0)
                {
                    newReward *= playerStats.getPrestigeMultiplier();
                    modified = true;
                }

                // set cooldown modifier last!
                if (level.hasCooldown() && Parkour.getLevelManager().inCooldownMap(playerStats.getPlayerName()))
                {
                    cooldown = Parkour.getLevelManager().getLevelCooldown(playerStats.getPlayerName());
                    newReward *= cooldown.getModifier();
                    modified = true;
                }

                // set modified, extra check for times of when max prestige = +25% and cooldown = -25%
                if (modified && oldReward != newReward)
                {
                    itemLore.add(Utils.translate("  &c&m" + Utils.formatNumber(oldReward) + "&6 " + Utils.formatNumber(newReward) + " Coin &7Reward"));

                    // on cooldown!
                    if (cooldown != null && cooldown.getModifier() != 1.00f)
                    {
                        itemLore.add(Utils.translate("  &7On cooldown &6(-" + ((int) ((1.00f - cooldown.getModifier()) * 100)) + "%)"));
                        itemLore.add(Utils.translate("    &7For " +
                                Time.elapsedShortened((Parkour.getSettingsManager().cooldownCalendar.getTimeInMillis() - System.currentTimeMillis()), false)) // get date - current and format
                        );
                    }
                }
                else
                    itemLore.add(Utils.translate("  &6" + Utils.formatNumber(oldReward) + " Coin &7Reward"));


                if (level.getTotalCompletionsCount() > 0)
                    itemLore.add(Utils.translate("  &6" + Utils.formatNumber(level.getTotalCompletionsCount()) + " &7Completions"));

                // only show rating if above 5
                if (level.getRatingsCount() >= 5) {
                    itemLore.add(Utils.translate("  &6" + level.getRating() + " &7Rating"));
                    itemLore.add(Utils.translate("    &7Out of &e" + Utils.formatNumber(level.getRatingsCount()) + " &7ratings"));
                }

                // Required Levels Section
                if (level.getRequiredLevels().size() > 0) {
                    itemLore.add("");
                    itemLore.add(Utils.translate("&7Required Levels"));

                    for (String requiredLevelName : level.getRequiredLevels()) {
                        Level requiredLevel = Parkour.getLevelManager().get(requiredLevelName);

                        if (requiredLevel != null)
                            itemLore.add(Utils.translate("&7 - " + requiredLevel.getFormattedTitle()));
                    }
                }

                // Personal Level Stats Section
                int levelCompletionsCount = playerStats.getLevelCompletionsCount(level.getName());
                if (levelCompletionsCount > 0) {
                    // add glow effect to all levels they have completed
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    itemLore.add("");

                    String beatenMessage = Utils.translate("&7Beaten &6" + Utils.formatNumber(levelCompletionsCount) + " &7Time");
                    if (levelCompletionsCount > 1)
                        beatenMessage += "s";

                    itemLore.add(beatenMessage);

                    LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(level.getName());
                    if (fastestCompletion != null) {
                        double completionTime = ((double) fastestCompletion.getCompletionTimeElapsed()) / 1000;
                        long timeSince = System.currentTimeMillis() - fastestCompletion.getTimeOfCompletion();

                        itemLore.add(Utils.translate("&7  Best Time &6" + completionTime + "s"));

                        // this makes it so it will not have " ago" if they just completed it
                        String timeSinceString;
                        if (Time.elapsedShortened(timeSince, false).equalsIgnoreCase(""))
                            timeSinceString = Utils.translate("  &7Just now");
                        else
                            timeSinceString = Utils.translate("  &7" + Time.elapsedShortened(timeSince, false) + "ago");

                        itemLore.add(timeSinceString);
                    }
                }
            }

            // Sections over
            itemMeta.setDisplayName(formattedTitle);
            itemMeta.setLore((itemLore));
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}