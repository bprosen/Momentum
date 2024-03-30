package com.renatusnetwork.momentum.data.levels;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.api.JackpotRewardEvent;
import com.renatusnetwork.momentum.api.LevelCompletionEvent;
import com.renatusnetwork.momentum.data.bank.BankManager;
import com.renatusnetwork.momentum.data.bank.items.Jackpot;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.events.types.EventType;
import com.renatusnetwork.momentum.data.leaderboards.LevelLBPosition;
import com.renatusnetwork.momentum.data.leaderboards.RecordsLBPosition;
import com.renatusnetwork.momentum.data.locations.LocationsDB;
import com.renatusnetwork.momentum.data.menus.*;
import com.renatusnetwork.momentum.data.menus.gui.Menu;
import com.renatusnetwork.momentum.data.menus.gui.MenuItem;
import com.renatusnetwork.momentum.data.menus.gui.MenuPage;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.races.gamemode.RaceEndReason;
import com.renatusnetwork.momentum.data.races.gamemode.RacePlayer;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.ranks.RanksManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class LevelManager
{
    private Level featuredLevel;
    private Level tutorialLevel;
    private long totalLevelCompletions;
    private boolean loadingLeaderboards;
    private int masteryLevels;

    private HashMap<String, Level> levels;
    private HashMap<Menu, Set<Level>> menuLevels;
    private HashMap<Level, MenuItem> levelMenuItems;

    private ArrayList<Level> globalLevelCompletionsLB;
    private ArrayList<Level> topRatedLevelsLB;
    private ArrayList<RecordsLBPosition> recordsLB;

    private HashMap<String, HashMap<Integer, Level>> buyingLevels;
    private HashMap<String, LevelCooldown> cooldowns;

    private static final int LEVEL_SEARCH_MULTIPLE_CAPACITY = 7;

    public LevelManager(Plugin plugin) {
        this.levels = new HashMap<>();
        this.menuLevels = new HashMap<>();
        this.levelMenuItems = new HashMap<>();
        this.globalLevelCompletionsLB = new ArrayList<>(Momentum.getSettingsManager().max_global_level_completions_leaderboard_size);
        this.topRatedLevelsLB = new ArrayList<>(Momentum.getSettingsManager().max_rated_levels_leaderboard_size);
        this.buyingLevels = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.recordsLB = new ArrayList<>(Momentum.getSettingsManager().max_records_leaderboard_size);

        load(); // Loads levels from configuration
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
        tutorialLevel = get(Momentum.getSettingsManager().tutorial_level_name);
        startScheduler(plugin);
    }

    public void load() {
        levels = LevelsDB.getLevels();
        tutorialLevel = get(Momentum.getSettingsManager().tutorial_level_name);

        // pre-computation optimization
        for (Level level : levels.values())
            if (level.hasMastery())
                masteryLevels++;

        Momentum.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public void add(Level level) {
        levels.put(level.getName(), level);
    }

    public void create(String levelName)
    {
        long systemMillis = System.currentTimeMillis();
        LevelsDB.insertLevel(levelName, systemMillis);
        levels.put(levelName, new Level(levelName, systemMillis));
    }

    public void remove(String levelName) {
        LevelsDB.removeLevel(levelName);
        StatsManager statsManager = Momentum.getStatsManager();
        HashMap<String, PlayerStats> players = statsManager.getPlayerStats();

        // thread safety
        synchronized (players)
        {
            for (PlayerStats playerStats : players.values())
            {
                // loop through and reset if applicable
                if (playerStats.inLevel() && playerStats.getLevel().equals(levelName))
                    statsManager.leaveLevelAndReset(playerStats, false);
            }
        }
        levels.remove(levelName);
    }

    public Level get(String levelName) {
        return levels.get(levelName);
    }

    public Level getFromTitle(String levelTitle)
    {
        levelTitle = ChatColor.stripColor(levelTitle);

        for (Level level : levels.values())
            if (ChatColor.stripColor(level.getTitle()).equalsIgnoreCase(levelTitle))
                return level;

        return null;
    }

    public void addRating(Player player, Level level, int rating) {
        level.addRating(player.getName(), rating);
        RatingDB.addRating(player, level, rating);
    }

    public void updateRating(Player player, Level level, int rating)
    {
        level.updateRating(player.getName(), rating);
        RatingDB.updateRating(player, level, rating);
    }

    public void removeRating(String playerName, Level level) {
        level.removeRating(playerName);
        RatingDB.removeRating(playerName, level);
    }

    public void toggleLiquidReset(Level level) {
        level.toggleLiquidReset();
        LevelsDB.updateLiquidReset(level.getName());
    }

    public void toggleNew(Level level) {
        level.toggleNew();
        LevelsDB.updateNew(level.getName());
    }

    public void toggleTC(Level level) {
        level.toggleTC();
        LevelsDB.updateTC(level.getName());
    }

    public void toggleCooldown(Level level) {
        level.toggleCooldown();
        LevelsDB.updateCooldown(level.getName());
    }

    public void setDifficulty(Level level, int difficulty) {
        level.setDifficulty(difficulty);
        LevelsDB.updateDifficulty(level.getName(), difficulty);
    }

    public void setLevelType(Level level, LevelType type) {
        level.setLevelType(type);
        LevelsDB.setLevelType(level.getName(), type);
    }

    public void setTitle(Level level, String title) {
        level.setTitle(title);
        LevelsDB.updateTitle(level.getName(), title);
    }

    public void setPrice(Level level, int price) {
        level.setPrice(price);
        LevelsDB.updatePrice(level.getName(), price);
    }

    public void setRespawnY(Level level, int respawnY) {
        level.setRespawnY(respawnY);
        LevelsDB.updateRespawnY(level.getName(), respawnY);
    }

    public void setReward(Level level, int reward) {
        level.setReward(reward);
        LevelsDB.updateReward(level.getName(), reward);
    }

    public void setStartLocation(Level level, String locationName, Location location) {
        level.setStartLocation(location);
        LocationsDB.insertLocation(locationName, location);
    }

    public void setCompletionLocation(Level level, String locationName, Location location) {
        level.setCompletionLocation(location);
        LocationsDB.insertLocation(locationName, location);
    }

    public void setMaxCompletions(Level level, int maxCompletions) {
        level.setMaxCompletions(maxCompletions);
        LevelsDB.updateMaxCompletions(level.getName(), maxCompletions);
    }

    public void toggleBroadcastCompletion(Level level) {
        level.toggleBroadcast();
        LevelsDB.updateBroadcast(level.getName());
    }

    public void toggleHasMastery(Level level) {
        level.toggleHasMastery();
        LevelsDB.updateHasMastery(level.getName());

        // means we toggled it on (+1)
        if (level.hasMastery())
            masteryLevels++;
            // means we toggled it off (-1)
        else
            masteryLevels--;
    }

    public void setMasteryMultiplier(Level level, float amount)
    {
        level.setMasteryMultiplier(amount);
        LevelsDB.updateMasteryMultiplier(level.getName(), amount);
    }

    public int getNumMasteryLevels() { return masteryLevels; }

    public void setRequiredPermission(Level level, String permission)
    {
        level.setRequiredPermission(permission);
        LevelsDB.updateRequiredPermission(level.getName(), permission);
    }

    public void setRequiredRank(Level level, Rank rank)
    {
        level.setRequiredRank(rank.getName());
        LevelsDB.updateRequiredRank(level.getName(), rank.getName());
    }

    public void removeRequiredPermission(Level level)
    {
        level.setRequiredPermission(null);
        LevelsDB.removeRequiredPermission(level.getName());
    }

    public void addRequiredLevel(Level level, String requiredLevelName)
    {
        level.addRequiredLevel(requiredLevelName);
        LevelsDB.insertLevelRequired(level.getName(), requiredLevelName);
    }

    public void removeRequiredLevel(Level level, String requiredLevelName)
    {
        level.removeRequiredLevel(requiredLevelName);
        LevelsDB.removeLevelRequired(level.getName(), requiredLevelName);
    }

    public void loadGlobalLevelCompletions()
    {
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
    }

    public boolean isLoadingLeaderboards() { return loadingLeaderboards; }

    public void setLoadingLeaderboards(boolean loadingLeaderboards) { this.loadingLeaderboards = loadingLeaderboards; }

    public void addCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion)
    {
        level.addTotalCompletionsCount();

        if (levelCompletion.wasTimed())
        {
            if (!isLoadingLeaderboards())
            {
                List<LevelLBPosition> leaderboard = level.getLeaderboard();

                if (!leaderboard.isEmpty())
                {
                    // Compare completion against scoreboard
                    if (leaderboard.size() < 10 ||
                        leaderboard.get(leaderboard.size() - 1).getTimeTaken() > levelCompletion.getCompletionTimeElapsedMillis())
                    {
                        LevelLBPosition firstPlace = level.getRecordCompletion();
                        String playerName = playerStats.getName();

                        // check for first place
                        if (
                            firstPlace.getPlayerName().equalsIgnoreCase(playerName) &&
                            firstPlace.getTimeTaken() > levelCompletion.getCompletionTimeElapsedMillis()
                            )
                            leaderboard.remove(0);
                        // otherwise, search for where it is
                        else
                        {
                            int lbPositionToRemove = -1;

                            for (int i = 0; i < leaderboard.size(); i++)
                            {
                                LevelLBPosition completion = leaderboard.get(i);

                                if (completion.getPlayerName().equalsIgnoreCase(playerName))
                                {
                                    if (completion.getTimeTaken() > levelCompletion.getCompletionTimeElapsedMillis())
                                    {
                                        lbPositionToRemove = i;
                                        break;
                                    }
                                    else return;
                                }
                            }

                            if (lbPositionToRemove > -1)
                                leaderboard.remove(lbPositionToRemove);
                        }
                        sortNewCompletion(level, levelCompletion);
                    }
                }
                else
                    leaderboard.add(
                            new LevelLBPosition(
                            levelCompletion.getLevelName(), levelCompletion.getName(), levelCompletion.getCompletionTimeElapsedMillis()
                            ));
            }
        }
    }

    private void sortNewCompletion(Level level, LevelCompletion levelCompletion)
    {
        List<LevelLBPosition> leaderboard = level.getLeaderboard();

        leaderboard.add(
                new LevelLBPosition(levelCompletion.getLevelName(), levelCompletion.getName(), levelCompletion.getCompletionTimeElapsedMillis())
        );

        for (int i = (leaderboard.size() - 1); i > 0; i--)
        {
            LevelLBPosition completion = leaderboard.get(i);
            LevelLBPosition nextCompletion = leaderboard.get(i - 1);

            if (nextCompletion.getTimeTaken() > completion.getTimeTaken())
            {
                // swap
                leaderboard.set((i - 1), completion);
                leaderboard.set(i, nextCompletion);
            }
        }
        // Trimming potential #max datapoint
        int maxSize = Momentum.getSettingsManager().levels_lb_size;
        if (leaderboard.size() > maxSize)
            leaderboard.remove(maxSize);
    }

    private void startScheduler(Plugin plugin)
    {

        // update player count in levels every 60 seconds
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                HashMap<Level, Integer> amountInLevel = new HashMap<>();

                // loop through levels then all players online to determine how many are in each level
                HashMap<String, PlayerStats> stats = Momentum.getStatsManager().getPlayerStats();

                synchronized (stats)
                {
                    for (PlayerStats playerStats : stats.values())
                    {
                        Level level = playerStats.getLevel();

                        // if in a level, add to map
                        if (level != null)
                            if (amountInLevel.containsKey(level))
                                amountInLevel.replace(level, amountInLevel.get(level) + 1);
                            else
                                amountInLevel.put(level, 1);
                    }

                    // set in level amount
                    for (Map.Entry<Level, Integer> entry : amountInLevel.entrySet())
                        entry.getKey().setPlayersInLevel(entry.getValue());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);

        // update global level completions and top rated completions lb every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalLevelCompletionsLB();
                loadTopRatedLevelsLB();
                loadRecordsLB();
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 30, 20 * 180);
    }

    public void pickFeatured()
    {

        List<Level> temporaryList = new ArrayList<>();

        /*
           sort through all levels that are in menus
           to make sure it does not have required levels and has more than 0 coin reward
         */
        for (Level level : getLevelsInAllMenus())
        {
            if (
                !level.hasRequiredLevels() &&
                !level.hasPermissionNode() &&
                !level.isAscendance() &&
                !level.isRankUpLevel() &&
                !level.isDropper() &&
                level.hasReward() &&
                level.getReward() < 50000
            )
                temporaryList.add(level);
        }

        if (!temporaryList.isEmpty())
        {
            Random ran = new Random();
            Level level = temporaryList.get(ran.nextInt(temporaryList.size()));

            if (level != null)
            {
                featuredLevel = level;
                Momentum.getPluginLogger().info("Featured Level: " + level.getName());
            }
        }
    }

    /*
        this method first loops through the menus in config, then the pages, then the items, to find ALL levels that
        originate from the GUI so no featured level is picked from something like ascendance, etc
     */
    public void loadLevelsInMenus() {

        menuLevels.clear();
        levelMenuItems.clear();
        for (String menuName : MenusYAML.getNames()) {
            Menu menu = Momentum.getMenuManager().getMenu(menuName);
            // null check menu
            if (menu != null) {
                Set<Level> levelsInMenu = new HashSet<>();
                boolean menuHasLevels = false;

                // then loop through each page in the menu
                for (int i = 1; i <= menu.getPageCount(); i++) {

                    MenuPage menuPage = menu.getPage(i);
                    // null check the page
                    if (menuPage != null) {
                        // finally, loop through each item in the gui
                        for (int j = 0; j < (menuPage.getRowCount() * 9); j++) {
                            MenuItem menuItem = menuPage.getMenuItem(j);

                            // null check the item
                            if (menuItem != null) {
                                // then check if the item is a level type, if so, add to list
                                if (menuItem.getType().equalsIgnoreCase("level")) {
                                    menuHasLevels = true; // enable it as a menu with levels
                                    Level menuLevel = get(menuItem.getTypeValue());

                                    // last step! null check level
                                    if (menuLevel != null)
                                    {
                                        levelsInMenu.add(menuLevel);
                                        levelMenuItems.put(menuLevel, menuItem);
                                    }
                                }
                            }
                        }
                    }
                }
                if (menuHasLevels)
                    // add to menu map
                    menuLevels.put(menu, levelsInMenu);
            }
        }
    }

    public boolean isBuyingLevel(String playerName, Level level)
    {
        boolean result = false;

        if (buyingLevels.containsKey(playerName))
        {
            HashMap<Integer, Level> levelsSet = buyingLevels.get(playerName);
            result = levelsSet.containsValue(level);
        }

        return result;
    }

    public void addBuyingLevel(String playerName, Level level, int slot)
    {
        HashMap<Integer, Level> newMap;

        if (buyingLevels.containsKey(playerName))
        {
            newMap = buyingLevels.get(playerName);
            newMap.put(slot, level);
        }
        else
        {
            newMap = new HashMap<Integer, Level>() {{
                put(slot, level);
            }};
        }

        buyingLevels.put(playerName, newMap);
    }

    public int getTotalBuyingLevelsCost(String playerName)
    {
        HashMap<Integer, Level> levels = buyingLevels.get(playerName);
        int totalCost = 0;

        if (levels != null)
            for (Level level : levels.values())
                totalCost += level.getPrice();

        return totalCost;
    }

    public void removeBuyingLevel(String playerName)
    {
        buyingLevels.remove(playerName);
    }

    public boolean isBuyingLevelMenu(String playerName)
    {
        return buyingLevels.containsKey(playerName);
    }

    public void updateStuckURL(Level level, String url)
    {
        level.setStuckURL(url);
        LevelsDB.updateStuckURL(level.getName(), url);
    }

    public void resetStuckURL(Level level)
    {
        level.setStuckURL(null);
        LevelsDB.resetStuckURL(level.getName());
    }

    public boolean inCooldownMap(String playerName)
    {
        return cooldowns.containsKey(playerName);
    }

    public LevelCooldown getLevelCooldown(String playerName)
    {
        return cooldowns.get(playerName);
    }

    public void addLevelCooldown(String playerName, Level level)
    {
        if (level.hasCooldown())
        {
            // add to both conditions
            if (cooldowns.containsKey(playerName))
                cooldowns.get(playerName).addCompletion();
            else
                cooldowns.put(playerName, new LevelCooldown(level));
        }
    }

    public List<Integer> getBuyingLevelsSlots(String playerName)
    {
        HashMap<Integer, Level> levels = buyingLevels.get(playerName);
        List<Integer> levelsToBuy = new ArrayList<>();

        // if non null, add all from keyset
        if (levels != null)
            levels.keySet().forEach(value -> levelsToBuy.add(value));

        return levelsToBuy;
    }

    public Level getTutorialLevel() { return tutorialLevel; }

    public HashMap<Integer, Level> getBuyingLevels(String playerName)
    {
        return buyingLevels.get(playerName);
    }

    public Set<Level> getLevelsInAllMenus()
    {
        Set<Level> levelsInMenus = new HashSet<>();

        // loop through then add to new hashset
        for (Set<Level> levels : menuLevels.values())
            levelsInMenus.addAll(levels);

        return levelsInMenus;
    }

    public Level getNameThenTitle(String levelName)
    {
        Level level = levels.get(levelName.toLowerCase());

        if (level == null)
            level = getFromTitle(levelName);

        return level;
    }

    public Set<Level> getLevelsFromMenu(Menu menu)
    {
        return menuLevels.get(menu);
    }

    public HashMap<Menu, Set<Level>> getMenuLevels() { return menuLevels; }

    public boolean isLevelInMenus(Level level)
    {
        return getLevelsInAllMenus().contains(level);
    }

    public MenuItem getMenuItemFromLevel(Level level)
    {
        return levelMenuItems.get(level);
    }

    public ArrayList<MenuItem> searchMenuLevelsIgnoreCase(String levelTitle)
    {
        levelTitle = levelTitle.toLowerCase();
        ArrayList<MenuItem> filtered = new ArrayList<>();

        for (Map.Entry<Level, MenuItem> entry : levelMenuItems.entrySet())
        {
            MenuItem menuItem = entry.getValue();
            String levelString = ChatColor.stripColor(entry.getKey().getFormattedTitle()).toLowerCase();

            if (levelString.equals(levelTitle))
            {
                filtered.clear();
                filtered.add(menuItem);
                break;
            }
            else if (levelString.contains(levelTitle) && filtered.size() <= LEVEL_SEARCH_MULTIPLE_CAPACITY)
                filtered.add(menuItem);
        }

        return filtered;
    }

    public Level getFeaturedLevel()
    {
        return featuredLevel;
    }

    public List<Level> getEventLevels()
    {
        List<Level> tempList = new ArrayList<>();

        for (Level level : levels.values())
            if (level.isEventLevel())
                tempList.add(level);

        return tempList;
    }

    public List<Level> getEventLevelsFromType(EventType eventType)
    {
        List<Level> tempList = new ArrayList<>();

        for (Level level : getEventLevels())
            if (level.getEventType() == eventType)
                tempList.add(level);

        return tempList;
    }

    public List<Level> getRaceLevels()
    {
        List<Level> temporaryRaceLevelList = new ArrayList<>();

        for (Level level : levels.values())
        {
            if (level.isRaceLevel())
                temporaryRaceLevelList.add(level);
        }
        return temporaryRaceLevelList;
    }

    public long getTotalLevelCompletions() { return totalLevelCompletions; }

    public void removeTotalLevelCompletion() { totalLevelCompletions--; }


    // top rated levels lb
    public void loadTopRatedLevelsLB()
    {
        try
        {

            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;
            topRatedLevelsLB.clear();

            while (Momentum.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize)
            {
                for (Level level : levels.values())
                    if (level.getRatingsCount() >= 5 &&
                        (highestLevel == null || (!addedLevels.contains(level.getName()) && level.getRating() >= highestLevel.getRating())))
                        highestLevel = level;

                // add 1 and add to added levels
                lbSize++;

                if (highestLevel != null)
                {
                    addedLevels.add(highestLevel.getName());
                    // add to temp lb
                    if (highestLevel.hasRating())
                        topRatedLevelsLB.add(highestLevel);

                    highestLevel = null;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ArrayList<Level> getTopRatedLevelsLB() { return topRatedLevelsLB; }

    public ArrayList<Level> getGlobalLevelCompletionsLB() {
        return globalLevelCompletionsLB;
    }

    public void loadGlobalLevelCompletionsLB()
    {
        try
        {
            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;

            globalLevelCompletionsLB.clear();

            while (Momentum.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {

                for (Level level : levels.values())
                    if (level.getTotalCompletionsCount() > 0 &&
                       (highestLevel == null ||
                       (!addedLevels.contains(level.getName()) && level.getTotalCompletionsCount() >= highestLevel.getTotalCompletionsCount())))
                        highestLevel = level;

                // null check jic
                if (highestLevel != null)
                {
                    globalLevelCompletionsLB.add(highestLevel);
                    addedLevels.add(highestLevel.getName());
                    highestLevel = null;
                }
                lbSize++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Level> getLevels() {
        return levels;
    }

    public boolean exists(String levelName) {
        return get(levelName) != null;
    }

    public void teleportToLevel(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();

        if (playerStats.isLoaded())
        {
            // since ascendance is a free-roam map...
            if (!level.isAscendance())
            {
                RanksManager ranksManager = Momentum.getRanksManager();
                if (!(level.needsRank() && ranksManager.isPastOrAtRank(playerStats, level.getRequiredRank())))
                {
                    if (!level.isEventLevel())
                    {
                        if (!level.isRaceLevel())
                        {
                            if (!playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
                            {
                                boolean teleport = true;

                                // not all levels have a price, so do a boolean switch
                                if (level.requiresBuying() && !playerStats.hasBoughtLevel(level) && !playerStats.hasCompleted(level))
                                {
                                    teleport = false;
                                    player.sendMessage(Utils.translate("&7You first need to buy &c" + level.getTitle() + "&7 before teleporting to it"));
                                    player.sendMessage(Utils.translate(
                                            "&7Type &c/level buy " + ChatColor.stripColor(level.getTitle()) + "&7 for &6" + Utils.formatNumber(level.getPrice()) + " &eCoins &7to buy " + level.getTitle())
                                    );
                                }

                                // if still allowed, tp them!
                                if (teleport)
                                    MenuItemAction.performLevelTeleport(playerStats, level);
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou cannot teleport to a level from the plot world, do /spawn first"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot teleport to a Race level"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot teleport to an Event level"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot teleport to a level you do not have the rank to"));
            }
            else
                player.sendMessage(Utils.translate("&cYou cannot teleport to an Ascendance level"));
        }
        else
            player.sendMessage(Utils.translate("&cYou cannot teleport to a level while loading your stats"));
    }

    public void regionLevelCheck(PlayerStats playerStats, Location location)
    {
        // region null check
        ProtectedRegion region = WorldGuard.getRegion(location);
        if (region != null) {

            Level level = Momentum.getLevelManager().get(region.getId());

            // make sure the area they are spawning in is a level
            if (level != null)
            {
                playerStats.setLevel(level);

                // if elytra level, toggle on
                if (playerStats.getLevel().isElytra())
                    Momentum.getStatsManager().toggleOnElytra(playerStats);

                // if they have a cp, load it
                Location checkpoint = playerStats.getCheckpoint(level);
                if (checkpoint != null)
                    playerStats.setCurrentCheckpoint(checkpoint);
            }
        }
    }

    /*
    completions management
     */
    public LevelCompletion createLevelCompletion(String uuid, String playerName, String levelName, long creationDate, long timeElapsed)
    {
        return timeElapsed >= 0 ?
                new LevelCompletion(
                        levelName, uuid, playerName, creationDate, timeElapsed
                )
                :
                new LevelCompletion(
                        levelName, uuid, playerName, creationDate
                );
    }

    public void validateAndRunLevelCompletion(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();
        EventManager eventManager = Momentum.getEventManager();

        // if playerstats and level exists
        if (level != null)
        {
            // if they are not spectating
            if (!playerStats.isSpectating())
            {
                // if they are not previewing
                if (!playerStats.isPreviewingLevel())
                {
                    // if they do have the required level
                    if (level.playerHasRequiredLevels(playerStats))
                    {
                        // if does not have a practice location
                        if (!playerStats.inPracticeMode())
                        {
                            int playerLevelCompletions = playerStats.getLevelCompletionsCount(level);

                            if (!level.hasMaxCompletions() || playerLevelCompletions < level.getMaxCompletions())
                            {
                                // if level is not an event level, it is guaranteed normal completion
                                if (!level.isEventLevel())
                                    doLevelCompletion(playerStats, level);
                                    // otherwise, if there is an event running, end!
                                else if (eventManager.isEventRunning())
                                    eventManager.endEvent(player, false, false);
                                    // otherwise, they are clicking the sign when the event is not running
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot do this when an Event is not running!"));
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou've reached the maximum number of completions"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot complete a level in practice mode"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou do not have the required levels to complete this level"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot do this while previewing"));
            }
            else
                player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
        }
    }

    private void doLevelCompletion(PlayerStats playerStats, Level level)
    {
        LevelCompletionEvent event = new LevelCompletionEvent(playerStats, level);
        Bukkit.getPluginManager().callEvent(event);
        Player player = playerStats.getPlayer();

        // continue if not cancelled
        if (!event.isCancelled())
        {
            LevelLBPosition oldRecord = level.getRecordCompletion();
            RacePlayer race = playerStats.getRace();

            // get current PB
            LevelCompletion fastestCompletion = playerStats.getQuickestCompletion(level);

            boolean inRace = race != null;
            boolean runGG = inRace;

            totalLevelCompletions++;

            // if they have not completed this individual level, then add and add to level stats
            if (!playerStats.hasCompleted(level))
            {
                playerStats.addIndividualLevelsBeaten();
                level.addTotalUniqueCompletionsCount();
            }

            long elapsedTime = System.currentTimeMillis() - playerStats.getLevelStartTime();
            String time = TimeUtils.formatCompletionTimeTaken(elapsedTime, 3);

            // create level completion with appropriate timing
            LevelCompletion levelCompletion;
            if (playerStats.isLevelBeingTimed())
                levelCompletion = createLevelCompletion(
                        playerStats.getUUID(), playerStats.getName(), level.getName(), System.currentTimeMillis(), elapsedTime
                );
            else
                levelCompletion = createLevelCompletion(
                        playerStats.getUUID(), playerStats.getName(), level.getName(), System.currentTimeMillis(), -1
                );

            // disable when complete
            if (level.equals(Momentum.getLevelManager().getTutorialLevel()))
                playerStats.setTutorial(false);

            playerStats.addTotalLevelCompletions();

            boolean completedMastery = level.hasMastery() && playerStats.isAttemptingMastery();

            addCompletion(playerStats, level, levelCompletion); // Update totalLevelCompletionsCount

            // run commands if there is any
            for (String commandString : level.getCommands())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandString.replace("%player%", player.getName()));

            // Update player information
            playerStats.levelCompletion(levelCompletion);

            // add mastery
            if (completedMastery)
            {
                playerStats.addMasteryCompletion(level.getName());
                Momentum.getStatsManager().leftMastery(playerStats);
            }
            BankManager bankManager = Momentum.getBankManager();

            // give higher reward if prestiged
            int reward = event.getReward();

            // level booster
            if (playerStats.hasModifier(ModifierType.LEVEL_BOOSTER))
            {
                Booster booster = (Booster) playerStats.getModifier(ModifierType.LEVEL_BOOSTER);
                reward *= booster.getMultiplier();
            }

            // if mastery, boost it
            if (completedMastery)
                reward *= level.getMasteryMultiplier();
                // if featured, set reward!
            else if (level.isFeaturedLevel())
                reward *= Momentum.getSettingsManager().featured_level_reward_multiplier;
                // jackpot section
            else if (bankManager.isJackpotRunning() &&
                    bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                    !bankManager.getJackpot().hasCompleted(playerStats.getName()))
            {
                Jackpot jackpot = bankManager.getJackpot();

                JackpotRewardEvent jackpotEvent = new JackpotRewardEvent(playerStats, jackpot.getLevel(), jackpot.getBonus());
                Bukkit.getPluginManager().callEvent(jackpotEvent);

                if (!jackpotEvent.isCancelled())
                {
                    int bonus = jackpotEvent.getBonus();

                    // jackpot booster
                    if (playerStats.hasModifier(ModifierType.JACKPOT_BOOSTER))
                    {
                        Booster booster = (Booster) playerStats.getModifier(ModifierType.JACKPOT_BOOSTER);
                        bonus *= booster.getMultiplier();
                    }

                    // add coins and add to completed, as well as broadcast completion
                    jackpot.addCompleted(player.getName());
                    jackpot.broadcastCompletion(player);
                    reward += bonus;
                }
            }
            // prestige/cooldown section
            else
            {
                if (playerStats.hasPrestiges() && level.hasReward())
                    reward *= playerStats.getPrestigeMultiplier();

                // set cooldown modifier last!
                if (level.hasCooldown() && inCooldownMap(playerStats.getName()))
                    reward *= getLevelCooldown(playerStats.getName()).getModifier();
            }

            Momentum.getStatsManager().addCoins(playerStats, reward);

            String completionMessage = "";

            if (levelCompletion.wasTimed())
                completionMessage = "&7 in &a" + time;

            String completion = "&7Rewarded &6" + Utils.getCoinFormat(level.getReward(), reward) +
                    " &eCoins &7for " + level.getTitle() + completionMessage +
                    "&a (" + Utils.shortStyleNumber(playerStats.getLevelCompletionsCount(level)) +
                    ")";

            if (playerStats.inFailMode())
                completion += " &7in &6" + playerStats.getFails() + " fails";

            player.sendMessage(Utils.translate(completion));
            player.sendMessage(Utils.translate("&7Rate &e" + level.getTitle() + "&7 with &6/rate "
                    + ChatColor.stripColor(level.getFormattedTitle())));

            // if new pb, send message to player
            if (levelCompletion.wasTimed() && fastestCompletion != null && fastestCompletion.getCompletionTimeElapsedMillis() > elapsedTime)
            {
                String oldTimeString = TimeUtils.formatCompletionTimeTaken(fastestCompletion.getCompletionTimeElapsedMillis(), 3); // need to format the long
                player.sendMessage(Utils.translate("&7You have broken your personal best &c(" + oldTimeString + ")&7 with &a" + time));
            }

            // broadcast completed if it is the featured level
            if (level.isFeaturedLevel())
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + player.getDisplayName() + " &7has completed &c&lFEATURED &4" + level.getTitle()
                ));
            else if (completedMastery)
            {
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + playerStats.getDisplayName() + "&7 has completed the &5&lMASTERY &7for &2" + level.getTitle()
                ));
                runGG = true;
            }
            else if (level.isBroadcasting())
            {
                Bukkit.broadcastMessage(Utils.translate("&a" + player.getDisplayName() + "&7 completed " + level.getTitle()));
                runGG = true;
            }

            // used for playing sound!
            int beforeClanLevel = -1;

            if (playerStats.inClan())
            {
                beforeClanLevel = playerStats.getClan().getLevel();

                // do clan xp algorithm if they are in clan and level has higher reward than configurable amount
                if (level.getReward() > Momentum.getSettingsManager().clan_calc_level_reward_needed)
                    Momentum.getClansManager().doClanXPCalc(playerStats.getClan(), playerStats, reward);

                // do clan reward split algorithm if they are in clan and level has higher reward than configurable amount
                if (level.getReward() > Momentum.getSettingsManager().clan_split_reward_min_needed)
                {
                    // async for database querying
                    int finalReward = reward;

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            Momentum.getClansManager().doSplitClanReward(playerStats.getClan(), player, level, finalReward);
                        }
                    }.runTaskAsynchronously(Momentum.getPlugin());
                }
            }

            if (!playerStats.isGrinding())
                Momentum.getStatsManager().toggleOffElytra(playerStats);

            Momentum.getPluginLogger().info(playerStats.getName() + " beat " + ChatColor.stripColor(level.getFormattedTitle())); // log to console

            // reset cp and saves before teleport
            if (!inRace)
            {
                Momentum.getCheckpointManager().deleteCheckpoint(playerStats, level);
                Momentum.getSavesManager().removeSave(playerStats, level);
            }

            // clear potion effects
            playerStats.clearPotionEffects();

            if (!inRace)
            {
                String titleMessage = Utils.translate("&7Beat " + level.getTitle());
                if (levelCompletion.wasTimed())
                    titleMessage += Utils.translate("&7 in &2" + time);

                String subTitleMessage = Utils.translate("&7Rate &e" + level.getTitle() + "&7 with &6/rate "
                        + ChatColor.stripColor(level.getFormattedTitle()));

                playerStats.sendTitle(
                        titleMessage,
                        subTitleMessage,
                        10, 60, 10
                );
            }

            // play sound if they did not level up their clan
            if (!(beforeClanLevel > -1 && beforeClanLevel < playerStats.getClan().getLevel()))
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0f);

            Location locationTo = level.getCompletionLocation();

            if (inRace)
            {
                locationTo = race.getOriginalLocation();
                RacePlayer opponent = race.getOpponent();
                PlayerStats opponentStats = opponent.getPlayerStats();

                setLevelInfoOnTeleport(opponentStats, opponent.getOriginalLocation());
                opponentStats.disableLevelStartTime();
                opponentStats.teleport(opponent.getOriginalLocation());

                playerStats.endRace(race, RaceEndReason.WON);
            }
            else
                // If not rank up level or has a start location and is grinding, set to start loc
                if (!playerStats.isAttemptingMastery() && !playerStats.isAttemptingRankup() && level.getStartLocation() != Momentum.getLocationManager().get("spawn") && playerStats.isGrinding())
                {
                    locationTo = level.getStartLocation();
                    playerStats.resetFails(); // reset fails in grinding
                }

            // rank them up!
            if (level.isRankUpLevel() && playerStats.isAttemptingRankup())
            {
                Momentum.getRanksManager().doRankUp(player);
                Momentum.getStatsManager().leftRankup(playerStats);
            }

            // add cooldown
            addLevelCooldown(playerStats.getName(), level);
            setLevelInfoOnTeleport(playerStats, locationTo);

            // teleport
            player.teleport(locationTo);

            LevelLBPosition recordCompletion = level.getRecordCompletion();

            boolean isRecord =
                    level.hasLeaderboard() &&
                            recordCompletion.getPlayerName().equalsIgnoreCase(levelCompletion.getName()) &&
                            recordCompletion.getTimeTaken() == levelCompletion.getCompletionTimeElapsedMillis();

            if (isRecord)
            {
                // update new #1 records
                playerStats.addRecord(level, levelCompletion.getCompletionTimeElapsedMillis());
                String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

                // update old record
                if (oldRecord != null)
                {
                    PlayerStats previousStats = Momentum.getStatsManager().getByName(oldRecord.getPlayerName());

                    if (previousStats != null && !playerStats.equals(previousStats))
                        previousStats.removeRecord(level);
                }
                else
                    brokenRecord = "&e✦ &d&lRECORD SET &e✦";

                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate(brokenRecord));
                Bukkit.broadcastMessage(Utils.translate("&d" + playerStats.getDisplayName() +
                        " &7has the new &8" + level.getTitle() +
                        " &7record with &a" + TimeUtils.formatCompletionTimeTaken(levelCompletion.getCompletionTimeElapsedMillis(), 3)));
                Bukkit.broadcastMessage("");

                Utils.spawnFirework(level.getCompletionLocation(), Color.PURPLE, Color.FUCHSIA, true);

                if (playerStats.hasModifier(ModifierType.RECORD_BONUS))
                {
                    Bonus bonus = (Bonus) playerStats.getModifier(ModifierType.RECORD_BONUS);

                    // add coins
                    Momentum.getStatsManager().addCoins(playerStats, bonus.getBonus());
                    playerStats.getPlayer().sendMessage(Utils.translate("&7You got &6" + Utils.formatNumber(bonus.getBonus()) + " &eCoins &7for getting the record!"));
                }
                // do gg run if it wasnt a race completion (gg already runs)
                runGG = true;
            }

            // run gg in specific cases
            if (runGG)
                Momentum.getStatsManager().runGGTimer();

            CompletionsDB.insertCompletion(levelCompletion, completedMastery);
        }
    }

    // Respawn player if checkpoint isn't there
    public void respawnPlayer(PlayerStats playerStats, Level level)
    {
        // make sure the water reset is toggled on
        if (level != null)
        {
            Location loc = level.getStartLocation();

            if (loc != null)
            {
                playerStats.getPlayer().teleport(loc);
                playerStats.addFail(); // used in multiple areas
            }
        }
    }

    public void setLevelInfoOnTeleport(PlayerStats playerStats, Location location)
    {
        ProtectedRegion getToRegion = WorldGuard.getRegion(location);
        Player player = playerStats.getPlayer();
        playerStats.disableLevelStartTime();

        // if area they are teleporting to is empty
        // if not empty, make sure it is a level
        // if not a level (like spawn), reset level
        if (getToRegion == null)
            playerStats.resetLevel();
        else
        {
            Level newLevel = get(getToRegion.getId());

            if (newLevel != null)
            {
                // apply potion effects if any exist
                for (PotionEffect potionEffect : newLevel.getPotionEffects())
                    player.addPotionEffect(potionEffect);

                // if elytra level, give elytra
                if (newLevel.isElytra() && !playerStats.getLevel().isElytra())
                    Momentum.getStatsManager().toggleOnElytra(playerStats);

                playerStats.setLevel(newLevel);

                if (playerStats.hasCheckpoint(newLevel))
                    playerStats.setCurrentCheckpoint(playerStats.getCheckpoint(newLevel));
            }
            else
                playerStats.resetLevel();
        }
    }

    public int numLevels() { return levels.size(); }

    public void loadRecordsLB() {

        if (!isLoadingLeaderboards())
        {
            recordsLB.clear();

            HashMap<String, Integer> tempRecords = new HashMap<>();
            for (Level level : Momentum.getLevelManager().getLevels().values())
            {
                LevelLBPosition record = level.getRecordCompletion();

                if (record != null)
                {
                    String playerName = record.getPlayerName();
                    if (tempRecords.containsKey(playerName))
                        tempRecords.replace(playerName, tempRecords.get(playerName) + 1);
                    else
                        tempRecords.put(playerName, 1);
                }
            }

            HashSet<String> seenNames = new HashSet<>();
            for (int lbPos = 0; lbPos < 10; lbPos++)
            {
                String currentMax = null;
                int currentMaxRecords = 0;
                for (Map.Entry<String, Integer> entry : tempRecords.entrySet())
                    if ((currentMax == null || entry.getValue() > currentMaxRecords) && !seenNames.contains(entry.getKey()))
                    {
                        currentMax = entry.getKey();
                        currentMaxRecords = entry.getValue();
                        seenNames.add(entry.getKey());
                    }

                recordsLB.add(lbPos, new RecordsLBPosition(currentMax, currentMaxRecords));
            }
        }
    }

    public ArrayList<RecordsLBPosition> getRecordsLB() { return recordsLB; }

    public HashMap<Level, Long> getRecords(String name)
    {
        HashMap<Level, Long> result = new HashMap<>();

        for (Level level : levels.values())
        {
            LevelLBPosition record = level.getRecordCompletion();

            if (record != null && record.getPlayerName().equalsIgnoreCase(name))
                result.put(get(record.getLevelName()), record.getTimeTaken());
        }
        return result;
    }

    public Set<String> getNames() {
        return levels.keySet();
    }
}