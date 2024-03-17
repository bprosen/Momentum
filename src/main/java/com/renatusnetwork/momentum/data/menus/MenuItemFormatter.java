package com.renatusnetwork.momentum.data.menus;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.BankManager;
import com.renatusnetwork.momentum.data.bank.items.BankItem;
import com.renatusnetwork.momentum.data.bank.items.BankItemType;
import com.renatusnetwork.momentum.data.bank.items.Jackpot;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.clans.ClanMember;
import com.renatusnetwork.momentum.data.elo.ELOTier;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.leaderboards.ELOLBPosition;
import com.renatusnetwork.momentum.data.leaderboards.LevelLBPosition;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.levels.LevelCooldown;
import com.renatusnetwork.momentum.data.levels.LevelManager;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.modifiers.discounts.Discount;
import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.levels.LevelCompletion;
import com.renatusnetwork.momentum.data.races.gamemode.ChoosingLevel;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuItemFormatter
{
    public static ItemStack format(PlayerStats playerStats, MenuItem menuItem)
    {
        if (menuItem.getType().equals("race"))
        {
            return getRaceLevel(playerStats, menuItem);
        }
        if (menuItem.getType().equals("level"))
        {
            if (menuItem.getTypeValue().equals("featured"))
                return getFeaturedLevel(playerStats, menuItem);

            if (menuItem.getTypeValue().equals("rankup"))
                return getRankUpLevel(playerStats, menuItem);

            if (menuItem.getTypeValue().startsWith("favorite-level"))
            {
                int index = Integer.parseInt(menuItem.getTypeValue().split("favorite-level-")[1]) - 1;
                Level favoriteLevel = playerStats.getFavoriteLevel(index);

                if (favoriteLevel != null)
                {
                    MenuItem foundItem = Momentum.getLevelManager().getMenuItemFromLevel(favoriteLevel);
                    return getFavoriteLevel(playerStats, favoriteLevel, menuItem, foundItem.getItem());
                }
            }
            return getLevel(playerStats, menuItem, menuItem.getItem());
        }

        if (menuItem.getType().equals("perk"))
            return getPerk(playerStats, menuItem);
        if (menuItem.getType().equals("bank"))
            return getBankItem(menuItem);
        if (menuItem.hasOpenMenu() && !menuItem.getOpenMenu().getMenu().getName().equalsIgnoreCase(Momentum.getSettingsManager().main_menu_name))
            return enchantMenuItem(
                    playerStats, menuItem,
                    menuItem.getOpenMenu().getMenu());
        if (menuItem.getType().equals("infinite-mode"))
            return getInfiniteMode(playerStats, menuItem);
        if (menuItem.getType().equals("type") && menuItem.getTypeValue().equals("level-sorting"))
            return getSortingType(playerStats, menuItem);
        if (menuItem.getType().equals("profile"))
            return getProfileStats(playerStats, menuItem);

        // Add in some '%player%' and such formatters for lore
        return menuItem.getItem();
    }

    private static ItemStack getProfileStats(PlayerStats playerStats, MenuItem menuItem)
    {
        ItemStack item = new ItemStack(menuItem.getItem());
        ItemMeta itemMeta = item.getItemMeta();
        List<String> newLore = new ArrayList<>();

        switch (menuItem.getTypeValue())
        {
            case "clan":
            {
                Clan clan = playerStats.getClan();

                // if they have a clan, check for clan item
                if (clan != null)
                {
                    newLore.add("&7Clan &e" + clan.getTag());
                    newLore.add("&7Level &e" + clan.getLevel());
                    newLore.add("&7Total XP &e" + Utils.formatNumber(clan.getTotalXP()));
                    newLore.add("&7Level XP &e" + Utils.formatNumber(clan.getXP()));
                    newLore.add("");
                    newLore.add("&7Members &e" + clan.numMembers());

                    for (ClanMember clanMember : clan.getMembers())
                    {
                        // make string for online/offline
                        String onlineStatus = "&cOffline";
                        if (Bukkit.getPlayer(clanMember.getName()) != null)
                            onlineStatus = "&aOnline";

                        String ownerStatus = "";
                        if (clan.getOwner().equals(clanMember))
                            ownerStatus = "&e(Owner)";

                        newLore.add("  &7" + clanMember.getName() + " " + onlineStatus + " " + ownerStatus);
                    }
                }
                else
                    newLore.add(Utils.translate("&7Not in a clan"));

                break;
            }
            case "game":
            {
                newLore.add("&7Hours &c" + Utils.formatNumber(playerStats.getPlayer().getStatistic(Statistic.PLAY_ONE_TICK) / 72000));
                newLore.add("&7Jumps &c" + Utils.formatNumber(playerStats.getPlayer().getStatistic(Statistic.JUMP)));

                newLore.add("&7ELO Tier &c" + playerStats.getELOTier().getTitle() + " &c(" + Utils.formatNumber(playerStats.getELO()) + ")");

                ELOLBPosition elolbPosition = Momentum.getStatsManager().getELOLBPositionIfExists(playerStats.getName());
                if (elolbPosition != null)
                    newLore.add("&7ELO Position &c#" + elolbPosition.getPosition());

                newLore.add("&7Coins &c" + Utils.formatNumber(playerStats.getCoins()));
                newLore.add("&7Perks/Total &c" + playerStats.getGainedPerksCount() + "/" + Momentum.getPerkManager().numPerks());
                newLore.add("&7Rank &c" + playerStats.getRank().getTitle());
                newLore.add("&7Prestige &c" + Utils.formatNumber(playerStats.getPrestiges()));
                newLore.add("&7Best Classic Infinite &c" + Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.CLASSIC)));
                newLore.add("&7Best Sprint Infinite &c" + Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.SPRINT)));
                newLore.add("&7Best Speedrun Infinite &c" + Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.SPEEDRUN)));
                newLore.add("&7Best Timed Infinite &c" + Utils.formatNumber(playerStats.getBestInfiniteScore(InfiniteType.TIMED)));
                newLore.add("&7Race Wins &c" + Utils.formatNumber(playerStats.getRaceWins()));
                newLore.add("&7Race Losses &c" + Utils.formatNumber(playerStats.getRaceLosses()));
                newLore.add("&7Race Win Rate &c" + playerStats.getRaceWinRate());
                newLore.add("&7Event Wins &c" + Utils.formatNumber(playerStats.getEventWins()));
                break;
            }
            case "level":
            {
                if (playerStats.hasFavoriteLevels())
                {
                    newLore.add("&7Favorite ");
                    ArrayList<Level> favoriteLevels = playerStats.getFavoriteLevels();
                    for (Level level : favoriteLevels)
                    {
                        newLore.add(" " + level.getTitle());
                        newLore.add("  &7Completions &a" + Utils.formatNumber(playerStats.getLevelCompletionsCount(level)));

                        LevelCompletion levelCompletion = playerStats.getQuickestCompletion(level);
                        if (levelCompletion != null)
                            newLore.add("  &7Fastest &a" + TimeUtils.formatCompletionTimeTaken(levelCompletion.getCompletionTimeElapsedMillis(), 3));
                    }
                }
                else
                    newLore.add("&7Favorite &cNone");

                newLore.add("&7Records &e✦ &a" + Utils.formatNumber(playerStats.getNumRecords()));
                newLore.add("&7Total Completions &a" + Utils.formatNumber(playerStats.getTotalLevelCompletions()));
                newLore.add("&7Levels Completed/Total &a" + Utils.formatNumber(playerStats.getIndividualLevelsBeaten()) + "/" + Utils.formatNumber(Momentum.getLevelManager().numLevels()));
                newLore.add("&7Mastery Levels Completed/Total &a" + Utils.formatNumber(playerStats.getNumMasteryCompletions()) + "/" + Utils.formatNumber(Momentum.getLevelManager().getNumMasteryLevels()));
                newLore.add("&7Rated Levels &a" + Utils.formatNumber(playerStats.getRatedLevelsCount()));
                break;
            }
        }
        itemMeta.setLore(Utils.formatLore(newLore));
        item.setItemMeta(itemMeta);

        return item;
    }

    private static ItemStack getFavoriteLevel(PlayerStats playerStats, Level favoriteLevel, MenuItem menuItem, ItemStack newItem)
    {
        // make it the featured in normal gui section too for consistency
        if (Momentum.getLevelManager().getFeaturedLevel().equals(favoriteLevel))
            return getFeaturedLevel(playerStats, menuItem);
        else
            return createLevelItem(playerStats, favoriteLevel, menuItem, newItem, null);
    }

    private static ItemStack getSortingType(PlayerStats playerStats, MenuItem menuItem)
    {
        ItemStack item = new ItemStack(menuItem.getItem());
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(Utils.translate("&b&lSort By"));
        List<String> lore = new ArrayList<>();
        lore.add("");

        for (LevelSortingType type : LevelSortingType.values())
            if (type == playerStats.getLevelSortingType())
                lore.add(" &7→ &b" + LevelSortingType.toString(type));
            else
                lore.add(" &7→ &8" + LevelSortingType.toString(type));

        lore.add("");
        lore.add("&7Click to switch");
        itemMeta.setLore(Utils.formatLore(lore));

        item.setItemMeta(itemMeta);

        return item;
    }

    private static ItemStack getInfiniteMode(PlayerStats playerStats, MenuItem menuItem)
    {
        ItemStack item = new ItemStack(menuItem.getItem());
        ItemMeta itemMeta = item.getItemMeta();

        String infiniteMode = StringUtils.capitalize(menuItem.getTypeValue().toLowerCase());

        itemMeta.setDisplayName(menuItem.getFormattedTitle());

        // glow if equal
        if (playerStats != null && playerStats.getInfiniteType() != null && playerStats.getInfiniteType().toString().equalsIgnoreCase(infiniteMode))
            Utils.addGlow(itemMeta);

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
            bankItem = Momentum.getBankManager().getItem(bankItemType);
            itemMeta.setDisplayName(Utils.translate(bankItem.getFormattedType() + " &d&lBank's Total"));
            List<String> lore = new ArrayList<String>() {{ add(Utils.translate("&6" + Utils.formatNumber(bankItem.getTotalBalance()) + " &eCoins")); }};
            itemMeta.setLore(lore);
        }
        else
        {
            bankItemType = BankItemType.valueOf(typeValue.toUpperCase());
            bankItem = Momentum.getBankManager().getItem(bankItemType);

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
        if (menuItem.isGlowing())
            Utils.addGlow(itemMeta);

        item.setItemMeta(itemMeta);
        return item;
    }
    //
    // Perk Section
    //
    private static ItemStack getPerk(PlayerStats playerStats, MenuItem menuItem)
    {
        ItemStack item = new ItemStack(menuItem.getItem());
        String perkName = menuItem.getTypeValue();
        Perk perk = Momentum.getPerkManager().get(perkName);

        if (perk != null) {
            ItemMeta itemMeta = item.getItemMeta();

            // if glowing, add glow effect
            if (menuItem.isGlowing())
                Utils.addGlow(itemMeta);

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>();
            itemMeta.setDisplayName(perk.getFormattedTitle());

            int price = perk.getPrice();

            if (playerStats.hasModifier(ModifierType.SHOP_DISCOUNT))
            {
                Discount discount = (Discount) playerStats.getModifier(ModifierType.SHOP_DISCOUNT);
                price *= (1.00f - discount.getDiscount());
            }

            // Ownership Status Section
            itemLore.add("");
            if (perk.hasAccess(playerStats))
                itemLore.add(Utils.translate("&2You own this perk"));
            else
            {
                itemLore.add(Utils.translate("&cYou do not own this perk"));

                itemLore.add("");
                itemLore.add(Utils.translate("&7Requirements"));

                // if it has shortened custom lore, add it, otherwise do normal lore
                if (menuItem.hasSpecificLore())
                    itemLore.addAll(menuItem.getFormattedLore());
                else
                {
                    // Level Requirements Section
                    for (Level requirement : perk.getRequiredLevels())
                        itemLore.add(Utils.translate(" " + requirement.getTitle()));

                    if (price > 0)
                    {
                        int playerBalance = playerStats.getCoins();

                        if (playerBalance > price)
                            itemLore.add(Utils.translate("&a  Click to buy "));
                        else
                            itemLore.add(Utils.translate(
                                    "&7 Requires " + Utils.getCoinFormat(perk.getPrice() - playerBalance, price - playerBalance) + " &7more &eCoins"
                            ));
                    }

                    ELOTier eloTier = perk.getRequiredELOTier();
                    if (eloTier != null)
                        itemLore.add(Utils.translate("&7 ELO Tier " + eloTier.getTitle()));
                }
            }

            // Sections Over
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    private static ItemStack getRaceLevel(PlayerStats playerStats, MenuItem menuItem)
    {
        String levelName = menuItem.getTypeValue();
        Level level = Momentum.getLevelManager().get(levelName);
        ItemStack item = menuItem.getItem();

        return level != null && level.isRaceLevel() ? createLevelItem(playerStats, level, menuItem, item, Momentum.getRaceManager().getChoosingLevelData(playerStats.getName())) : item;
    }

    private static ItemStack getLevel(PlayerStats playerStats, MenuItem menuItem, ItemStack item)
    {
        String levelName = menuItem.getTypeValue();
        Level level = Momentum.getLevelManager().get(levelName);

        if (level != null)
        {
            // make it the featured in normal gui section too for consistency
            if (Momentum.getLevelManager().getFeaturedLevel().equals(level))
                return getFeaturedLevel(playerStats, menuItem);
            else
                return createLevelItem(playerStats, level, menuItem, item, null);
        }
        return item;
    }

    private static ItemStack getFeaturedLevel(PlayerStats playerStats, MenuItem menuItem)
    {
        LevelManager levelManager = Momentum.getLevelManager();
        MenuItem itemFromLevel = levelManager.getMenuItemFromLevel(levelManager.getFeaturedLevel());

        return createLevelItem(playerStats, levelManager.getFeaturedLevel(), menuItem, itemFromLevel.getItem(), null);
    }

    private static ItemStack enchantMenuItem(PlayerStats playerStats, MenuItem menuItem, Menu menu)
    {
        // get item and levels, clone so it can change properly
        ItemStack item = new ItemStack(menuItem.getItem());
        Set<Level> levelsInMenu = Momentum.getMenuManager().getLevelsFromMenuDeep(menuItem.getMenu(), menu);

        if (levelsInMenu != null && !levelsInMenu.isEmpty())
        {
            int count = 0;

            // more optimized: start as true and if a level is not completed, toggle to false and break
            boolean enchant = true;
            for (Level level : levelsInMenu)
            {
                if (!playerStats.hasCompleted(level))
                    enchant = false;
                else
                    count++;
            }

            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(Utils.translate(
                    itemMeta.getDisplayName() + "&a " + (int) (((double) count / levelsInMenu.size()) * 100) + "%"));

            // if enchanting, add durability and hide it for glow effect
            if (enchant)
                Utils.addGlow(itemMeta);

            item.setItemMeta(itemMeta);
        }
        return item;
    }

    private static ItemStack getRankUpLevel(PlayerStats playerStats, MenuItem menuItem)
    {

        ItemStack item = new ItemStack(menuItem.getItem());

        if (playerStats.getRank() != null)
        {
            if (playerStats.isLastRank())
            {
                // override itemstack
                item = new ItemStack(Material.EXP_BOTTLE);

                // make simple item to tell them they need to do /prestige!
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(Utils.translate("&6&lPrestige"));
                itemMeta.setLore(new ArrayList<String>() {{
                    add(Utils.translate("&cYou are now at the max rank"));
                    add(Utils.translate("&cPrestige by doing &4/prestige &cin chat"));
                }});

                item.setItemMeta(itemMeta);

                return item;
            }
            else
            {
                Level level = playerStats.getRank().getRankupLevel();
                return createLevelItem(playerStats, level, menuItem, item, null);
            }
        }
        return null;
    }

    private static ItemStack createLevelItem(PlayerStats playerStats, Level level, MenuItem menuItem, ItemStack item, ChoosingLevel choosingRaceLevel)
    {
        if (level != null)
        {
            item = new ItemStack(item); // clone
            ItemMeta itemMeta = item.getItemMeta();
            String formattedTitle = level.getFormattedTitle();

            // Existing Lore Section
            List<String> itemLore = new ArrayList<>(menuItem.getFormattedLore());

            // add featured title
            if (level.isFeaturedLevel())
                formattedTitle = Utils.translate("&c&lFEATURED " + formattedTitle);
            else
            // add new if new level! but dont show new if featured (too messy)
            if (level.isNew())
                formattedTitle = Utils.translate("&d&lNEW " + formattedTitle);

            BankManager bankManager = Momentum.getBankManager();

            if (choosingRaceLevel == null)
            {
                // show they need to buy it and it is not the jackpot level if it is running
                if (!(bankManager.isJackpotRunning() &&
                        bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName())) &&
                        level.requiresBuying() && !playerStats.hasBoughtLevel(level) && !playerStats.hasCompleted(level))
                {

                    int price = level.getPrice();

                    if (playerStats.hasModifier(ModifierType.LEVEL_DISCOUNT))
                    {
                        Discount discount = (Discount) playerStats.getModifier(ModifierType.LEVEL_DISCOUNT);
                        price *= (1.00f - discount.getDiscount());
                    }

                    itemLore.add(Utils.translate("&7Click to buy " + level.getTitle() + "&7 for " + Utils.getCoinFormat(level.getPrice(), price) + " &eCoins"));
                    itemLore.add(Utils.translate("  &6Shift click to preview"));
                    itemLore.add(Utils.translate("&7You have &6" + Utils.formatNumber(playerStats.getCoins()) + " &eCoins"));
                }
                else
                {
                    Rank requiredRank = Momentum.getRanksManager().get(level.getRequiredRank());

                    if (level.needsRank() && !Momentum.getRanksManager().isPastOrAtRank(playerStats, requiredRank))
                        itemLore.add(Utils.translate("&cRequires rank " + requiredRank.getTitle()));
                    else if (playerStats.hasSave(level))
                        itemLore.add(Utils.translate("&7Click to go to your &aSave"));
                    else if (playerStats.hasCheckpoint(level))
                        itemLore.add(Utils.translate("&7Click to go to &eCheckpoint"));
                    else
                        itemLore.add(Utils.translate("&7Click to go to " + level.getTitle()));
                }
            }
            else
            {
                PlayerStats opponentStats = choosingRaceLevel.getRequested();

                // display access if they do not have it
                if (!playerStats.hasAccessTo(level))
                    itemLore.add(Utils.translate("&cYou do not have access to this level"));
                else if (!opponentStats.hasAccessTo(level))
                    itemLore.add(Utils.translate("&4" + opponentStats.getDisplayName() + "&c does not have access to this level"));
                else
                {
                    itemLore.add(Utils.translate("&7Click to race &c" + opponentStats.getDisplayName() + "&7 on " + level.getTitle()));

                    if (choosingRaceLevel.hasBet())
                        itemLore.add(Utils.translate("  &6" + Utils.formatNumber(choosingRaceLevel.getBet()) + "&e Coins &7Bet"));
                }
            }

            // Item Title Section
            if (level.getPlayersInLevel() > 0)
                formattedTitle += Utils.translate(" &7(" + level.getPlayersInLevel() + " Playing)");

            if (level.hasDifficulty()) // has difficulty
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

            if (playerStats.hasModifier(ModifierType.LEVEL_BOOSTER))
            {
                // downcast and boost
                Booster booster = (Booster) playerStats.getModifier(ModifierType.LEVEL_BOOSTER);
                newReward *= booster.getMultiplier();
            }

            if (level.isFeaturedLevel())
                newReward *= Momentum.getSettingsManager().featured_level_reward_multiplier;
            // jackpot section
            else if (bankManager.isJackpotRunning() &&
                bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                !bankManager.getJackpot().hasCompleted(playerStats.getName()))
            {
                Jackpot jackpot = bankManager.getJackpot();

                int bonus = jackpot.getBonus();

                if (playerStats.hasModifier(ModifierType.JACKPOT_BOOSTER))
                {
                    // downcast and boost
                    Booster booster = (Booster) playerStats.getModifier(ModifierType.JACKPOT_BOOSTER);
                    bonus *= booster.getMultiplier();
                }
                newReward += bonus;
            }
            // only do these if jackpot is not running!
            else
            {
                if (playerStats.hasPrestiges() && level.hasReward())
                    newReward *= playerStats.getPrestigeMultiplier();

                // set cooldown modifier last!
                if (level.hasCooldown() && Momentum.getLevelManager().inCooldownMap(playerStats.getName()))
                {
                    cooldown = Momentum.getLevelManager().getLevelCooldown(playerStats.getName());
                    newReward *= cooldown.getModifier();
                }
            }

            // set modified, extra check for times of when max prestige = +25% and cooldown = -25%
            if (oldReward != newReward)
            {
                itemLore.add(Utils.translate("  &c&m" + Utils.formatNumber(oldReward) + "&6 " + Utils.formatNumber(newReward) + "&e Coin &7Reward"));

                // on cooldown!
                if (cooldown != null && cooldown.getModifier() != 1.00f)
                {
                    itemLore.add(Utils.translate("  &7On cooldown &6-" + ((int) ((1.00f - cooldown.getModifier()) * 100)) + "%"));
                    itemLore.add(Utils.translate("    &7For &e" +
                            TimeUtils.formatTime(Momentum.getSettingsManager().cooldown_calendar.getTimeInMillis() - System.currentTimeMillis())) // get date - current and format
                    );
                }
            }
            else
                itemLore.add(Utils.translate("  &6" + Utils.formatNumber(oldReward) + "&e Coin &7Reward"));


            // only show rating if above 5
            if (level.getRatingsCount() >= 5)
            {
                int rating = level.getRating(playerStats.getName());

                itemLore.add(Utils.translate("  &6" + level.getRating() + (rating > -1 ? " &e(" + rating + ")" : "") + " &7Rating"));
                itemLore.add(Utils.translate("    &7Out of &e" + Utils.formatNumber(level.getRatingsCount()) + " &7ratings"));
            }

            if (level.getTotalCompletionsCount() > 0)
            {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Completions"));
                itemLore.add(Utils.translate("  &6" + Utils.formatNumber(level.getTotalCompletionsCount()) + " &7Total"));
                itemLore.add(Utils.translate("  &6" + Utils.formatNumber(level.getTotalUniqueCompletionsCount()) + " &7Unique"));

                if (level.hasAverageTimeTaken())
                {
                    String time = "";
                    long millisAverage = level.getAverageTimeTaken();

                    int seconds = (int) (millisAverage / 1000);
                    int minutes = seconds / 60;
                    int hours = minutes / 60;

                    if (hours > 0)
                        time += hours + "h ";
                    if (minutes > 0)
                        time += (minutes % 60) + "m ";
                    if (seconds > 0)
                        time += (seconds % 60) + "s";

                    itemLore.add(Utils.translate("  &6" + time + " &7Average"));
                }

                LevelLBPosition lbPosition = level.getRecordCompletion();
                if (lbPosition != null)
                {
                    itemLore.add(Utils.translate("  &6" + TimeUtils.formatCompletionTimeTaken(lbPosition.getTimeTaken(), 3) + " &7Fastest"));
                    itemLore.add(Utils.translate("    &e" + lbPosition.getPlayerName()));
                }
            }

            // Personal Level Stats Section
            int levelCompletionsCount = playerStats.getLevelCompletionsCount(level);
            if (levelCompletionsCount > 0)
            {
                Utils.addGlow(itemMeta);

                itemLore.add("");

                String beatenMessage = "&7Beaten &6" + Utils.formatNumber(levelCompletionsCount) + " &7time";
                if (levelCompletionsCount > 1)
                    itemLore.add(Utils.translate(beatenMessage + "s"));
                else
                    itemLore.add(Utils.translate(beatenMessage));

                LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(level);
                if (fastestCompletion != null)
                {
                    // add record if there is one
                    LevelLBPosition record = level.getRecordCompletion();
                    String bestTimeValue = "  &6" + TimeUtils.formatCompletionTimeTaken(fastestCompletion.getCompletionTimeElapsedMillis(), 3);

                    if (record != null)
                    {
                        // add number 1
                        if (playerStats.hasRecord(level))
                            bestTimeValue += " &e#1";
                        else
                            bestTimeValue += " &e+" + TimeUtils.formatCompletionTimeTaken(fastestCompletion.getCompletionTimeElapsedMillis() - record.getTimeTaken(), 3);
                    }

                    itemLore.add(Utils.translate(bestTimeValue));
                    itemLore.add(Utils.translate("    &7" + TimeUtils.getDate(fastestCompletion.getTimeOfCompletionMillis())));
                }

                if (choosingRaceLevel == null && level.hasMastery())
                    if (playerStats.hasMasteryCompletion(level))
                        itemLore.add(Utils.translate("&7  Mastery &a✔"));
                    else
                    {
                        itemLore.add(Utils.translate("&7  Mastery &c✖ &6Shift click"));
                        itemLore.add(Utils.translate("    &6" + Utils.formatNumber(level.getReward() * level.getMasteryMultiplier()) + " &eCoins &7" + level.getMasteryMultiplier() + "x"));
                    }
            }

            // Required Levels Section, but only show it if not featured
            if (choosingRaceLevel == null && level.hasRequiredLevels() && !level.isFeaturedLevel())
            {
                itemLore.add("");
                itemLore.add(Utils.translate("&7Requires"));

                for (String requiredLevelName : level.getRequiredLevels())
                {
                    Level requiredLevel = Momentum.getLevelManager().get(requiredLevelName);

                    if (requiredLevel != null)
                        itemLore.add(Utils.translate("&7  " + requiredLevel.getTitle()));
                }
            }

            // Sections over
            itemMeta.setDisplayName(formattedTitle);
            itemMeta.setLore(itemLore);
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}