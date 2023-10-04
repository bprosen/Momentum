package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.BankItem;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCooldown;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.modifiers.discounts.Discount;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.ranks.RanksYAML;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Time;
import com.renatusnetwork.parkour.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuItemFormatter {

    public static ItemStack format(Player player, PlayerStats playerStats, MenuItem menuItem) {
        if (menuItem.getType().equals("level"))
        {
            if (menuItem.getTypeValue().equals("featured"))
                return getFeaturedLevel(playerStats, menuItem, menuItem.getItem());

            if (menuItem.getTypeValue().equals("rankup"))
                return getRankUpLevel(playerStats, menuItem);

            return getLevel(playerStats, menuItem);
        }

        if (menuItem.getType().equals("perk"))
            return getPerk(player, playerStats, menuItem);
        if (menuItem.getType().equals("bank"))
            return getBankItem(menuItem);
        if (menuItem.getType().equals("open"))
            return enchantMenuItem(playerStats, menuItem, Parkour.getMenuManager().getMenu(menuItem.getTypeValue()));
        if (menuItem.getType().equals("infinite-mode"))
            return getInfiniteMode(playerStats, menuItem);

        // Add in some '%player%' and such formatters for lore
        return menuItem.getItem();
    }

    private static ItemStack getInfiniteMode(PlayerStats playerStats, MenuItem menuItem) {
        ItemStack item = new ItemStack(menuItem.getItem());
        ItemMeta itemMeta = item.getItemMeta();

        String infiniteMode = StringUtils.capitalize(menuItem.getTypeValue().toLowerCase());

        itemMeta.setDisplayName(menuItem.getFormattedTitle());

        // glow if equal
        if (playerStats != null && playerStats.getInfiniteType() != null && playerStats.getInfiniteType().toString().equalsIgnoreCase(infiniteMode))
        {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemMeta.setLore(menuItem.getFormattedLore());
        item.setItemMeta(itemMeta);

        return item;
    }

    private static ItemStack getBankItem(MenuItem menuItem)
    {
        ItemStack item = new ItemStack(menuItem.getItem());
        String typeValue = menuItem.getTypeValue();
        ItemMeta itemMeta = item.getItemMeta();

        BankItemType bankItemType;
        BankItem bankItem;

        // if it ends in the total item, show it
        if (typeValue.endsWith("_total"))
        {
            bankItemType = BankItemType.valueOf(typeValue.split("_total")[0].toUpperCase());
            bankItem = Parkour.getBankManager().getItem(bankItemType);
            itemMeta.setDisplayName(Utils.translate(bankItem.getFormattedType() + " &d&lBank's Total"));
            List<String> lore = new ArrayList<String>() {{ add(Utils.translate("&6" + Utils.formatNumber(bankItem.getTotalBalance()) + " &eCoins")); }};
            itemMeta.setLore(lore);
        }
        else
        {
            bankItemType = BankItemType.valueOf(typeValue.toUpperCase());
            bankItem = Parkour.getBankManager().getItem(bankItemType);

            itemMeta.setDisplayName(Utils.translate(bankItem.getTitle()));

            List<String> lore = new ArrayList<>();

            if (bankItem.isLocked())
                lore.add(Utils.translate("&4&lLOCKED"));

            lore.add("");

            lore.add(Utils.translate(bankItem.getTitle() + " &7Modifier"));
            lore.add(Utils.translate(bankItem.getDescription()));

            lore.add("");

            if (!bankItem.hasCurrentHolder())
                lore.add(Utils.translate("&7Current Holder &dNone"));
            else
                lore.add(Utils.translate("&7Current Holder &d" + bankItem.getCurrentHolder()));

            // next bid
            lore.add(Utils.translate("&7Pay &6" + Utils.formatNumber(bankItem.getNextBid()) + " &eCoins &7to take"));

            itemMeta.setLore(lore);
        }

        // if glowing, add glow effect
        if (menuItem.isGlowing()) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);
        return item;
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

            int price = perk.getPrice();

            if (playerStats.hasModifier(ModifierTypes.SHOP_DISCOUNT))
            {
                Discount discount = (Discount) playerStats.getModifier(ModifierTypes.SHOP_DISCOUNT);
                price *= (1.00f - discount.getDiscount());
            }

            // Ownership Status Section
            itemLore.add("");
            if (perk.hasRequiredPermissions(player) || perk.hasRequirements(playerStats, player))
                itemLore.add(Utils.translate("&2You own this perk"));
            else {
                itemLore.add(Utils.translate("&cYou do not own this perk"));

                // Click to Buy Section
                if (perk.getPrice() > 0) {
                    int playerBalance = (int) playerStats.getCoins();

                    if (playerBalance > price)
                        itemLore.add(Utils.translate("&7  Click to buy "));
                    else
                        itemLore.add(Utils.translate("&7  Requires " + Utils.getCoinFormat(perk.getPrice() - playerBalance, price - playerBalance) + " &7more &eCoins"));
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

                    if (price > 0)
                        itemLore.add(Utils.translate("&7 - Pay " + Utils.getCoinFormat(perk.getPrice(), price) + " &eCoins"));
                }
            }

            // Sections Over
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
                return getFeaturedLevel(playerStats, menuItem, item);
            else
                return createLevelItem(playerStats, level, menuItem, item);
        }
        return item;
    }

    private static ItemStack getFeaturedLevel(PlayerStats playerStats, MenuItem menuItem, ItemStack itemStack)
    {
        return createLevelItem(playerStats, Parkour.getLevelManager().getTutorialLevel(), menuItem, itemStack);
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

        if (playerStats.getRank() != null)
        {
            String levelName = RanksYAML.getRankUpLevel(playerStats.getRank().getRankName());

            if (levelName != null)
            {
                Level level = Parkour.getLevelManager().get(levelName);

                return createLevelItem(playerStats, level, menuItem, item);
            }
        }
        return null;
    }

    private static ItemStack createLevelItem(PlayerStats playerStats, Level level, MenuItem menuItem, ItemStack item) {

        if (level != null) {

            ItemMeta itemMeta = item.getItemMeta();
            String formattedTitle = level.getFormattedTitle();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            // add featured title
            if (level.isFeaturedLevel())
                formattedTitle = Utils.translate("&cFeatured " + formattedTitle);
            else
            // add new if new level! but dont show new if featured (too messy)
            if (level.isNewLevel())
                formattedTitle = Utils.translate("&d&lNEW " + formattedTitle);

            if (level.needsRank() && !Parkour.getRanksManager().isPastOrAtRank(playerStats, level.getRequiredRank()))
                itemLore.add(Utils.translate("&cRequires rank " + level.getRequiredRank().getRankTitle()));

            BankManager bankManager = Parkour.getBankManager();

            // show they need to buy it and it is not the jackpot level if it is running
            if (!(bankManager.isJackpotRunning() &&
                bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName())) &&
                level.getPrice() > 0 && !playerStats.hasBoughtLevel(level.getName()) && playerStats.getLevelCompletionsCount(level.getName()) <= 0)
            {

                int price = level.getPrice();

                if (playerStats.hasModifier(ModifierTypes.LEVEL_DISCOUNT))
                {
                    Discount discount = (Discount) playerStats.getModifier(ModifierTypes.LEVEL_DISCOUNT);
                    price *= (1.00f - discount.getDiscount());
                }

                itemLore.add(Utils.translate("&7Click to buy " + level.getFormattedTitle() + " &7for " + Utils.getCoinFormat(level.getPrice(), price) + " &eCoins"));
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
            int newReward = oldReward;
            LevelCooldown cooldown = null;

            if (playerStats.hasModifier(ModifierTypes.LEVEL_BOOSTER))
            {
                // downcast and boost
                Booster booster = (Booster) playerStats.getModifier(ModifierTypes.LEVEL_BOOSTER);
                newReward *= booster.getMultiplier();
            }

            if (level.isFeaturedLevel())
            {
                newReward *= Parkour.getSettingsManager().featured_level_reward_multiplier;
            }
            // jackpot section
            else if (bankManager.isJackpotRunning() &&
                bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                !bankManager.getJackpot().hasCompleted(playerStats.getPlayerName()))
            {
                Jackpot jackpot = bankManager.getJackpot();

                int bonus = jackpot.getBonus();

                if (playerStats.hasModifier(ModifierTypes.JACKPOT_BOOSTER))
                {
                    // downcast and boost
                    Booster booster = (Booster) playerStats.getModifier(ModifierTypes.JACKPOT_BOOSTER);
                    bonus *= booster.getMultiplier();
                }
                newReward += bonus;
            }
            // only do these if jackpot is not running!
            else
            {

                if (playerStats.getPrestiges() > 0 && level.getReward() > 0)
                    newReward *= playerStats.getPrestigeMultiplier();

                // set cooldown modifier last!
                if (level.hasCooldown() && Parkour.getLevelManager().inCooldownMap(playerStats.getPlayerName()))
                {
                    cooldown = Parkour.getLevelManager().getLevelCooldown(playerStats.getPlayerName());
                    newReward *= cooldown.getModifier();
                }
            }

            // set modified, extra check for times of when max prestige = +25% and cooldown = -25%
            if (oldReward != newReward)
            {
                itemLore.add(Utils.translate("  &c&m" + Utils.formatNumber(oldReward) + "&6 " + Utils.formatNumber(newReward) + " Coin &7Reward"));

                // on cooldown!
                if (cooldown != null && cooldown.getModifier() != 1.00f)
                {
                    itemLore.add(Utils.translate("  &7On cooldown &6(-" + ((int) ((1.00f - cooldown.getModifier()) * 100)) + "%)"));
                    itemLore.add(Utils.translate("    &7For &e" +
                            Time.elapsedShortened((Parkour.getSettingsManager().cooldown_calendar.getTimeInMillis() - System.currentTimeMillis()), false)) // get date - current and format
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

            // Required Levels Section, but only show it if not featured
            if (level.getRequiredLevels().size() > 0 && !level.isFeaturedLevel()) {
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

                String beatenMessage = "&7Beaten &6" + Utils.formatNumber(levelCompletionsCount) + " &7Time";
                if (levelCompletionsCount > 1)
                    beatenMessage += "s";

                itemLore.add(Utils.translate(beatenMessage));

                LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(level.getName());
                if (fastestCompletion != null) {
                    double completionTime = ((double) fastestCompletion.getCompletionTimeElapsed()) / 1000;
                    long timeSince = System.currentTimeMillis() - fastestCompletion.getTimeOfCompletion();

                    String bestTime = "&7  Best Time &6" + completionTime + "s";

                    // add record if there is one
                    List<LevelCompletion> leaderboard = level.getLeaderboard();
                    if (!leaderboard.isEmpty())
                    {
                        LevelCompletion record = leaderboard.get(0);

                        // add number 1
                        if (record.getPlayerName().equalsIgnoreCase(playerStats.getPlayerName()))
                            bestTime += " &e#1";
                        else
                            bestTime += " &e+" + (completionTime - (record.getCompletionTimeElapsed() / 1000D)) + "s";
                    }

                    itemLore.add(Utils.translate(bestTime));

                    // this makes it so it will not have " ago" if they just completed it
                    String timeSinceString;
                    if (Time.elapsedShortened(timeSince, false).equalsIgnoreCase(""))
                        timeSinceString = Utils.translate("  &7Just now");
                    else
                        timeSinceString = Utils.translate("  &7" + Time.elapsedShortened(timeSince, false) + "ago");

                    itemLore.add(timeSinceString);
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