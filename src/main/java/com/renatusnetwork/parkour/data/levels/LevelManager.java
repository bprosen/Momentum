package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.locations.LocationsDB;
import com.renatusnetwork.parkour.data.menus.*;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.ranks.RanksManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class LevelManager {

    private Level featuredLevel;
    private Level tutorialLevel;
    private long totalLevelCompletions;

    private int masteryLevels;
    private HashMap<String, Level> levels;
    private HashMap<Menu, Set<Level>> menuLevels;
    private HashMap<Integer, Level> globalLevelCompletionsLB;
    private HashMap<Integer, Level> topRatedLevelsLB;
    private HashMap<String, HashMap<Integer, Level>> buyingLevels;
    private HashMap<String, LevelCooldown> cooldowns;

    public LevelManager(Plugin plugin)
    {
        this.levels = new HashMap<>();
        this.menuLevels = new HashMap<>();
        this.globalLevelCompletionsLB = new HashMap<>(Parkour.getSettingsManager().max_global_level_completions_leaderboard_size);
        this.topRatedLevelsLB = new HashMap<>(Parkour.getSettingsManager().max_rated_levels_leaderboard_size);
        this.buyingLevels = new HashMap<>();
        this.cooldowns = new HashMap<>();

        load(); // Loads levels from configuration
        CompletionsDB.loadLeaderboards();
        loadLevelsInMenus();
        pickFeatured();
        totalLevelCompletions = LevelsDB.getGlobalCompletions();
        tutorialLevel = get(Parkour.getSettingsManager().tutorial_level_name);
        startScheduler(plugin);
    }

    public void load()
    {
        levels = LevelsDB.getLevels();
        tutorialLevel = get(Parkour.getSettingsManager().tutorial_level_name);

        // pre-computation optimization
        for (Level level : levels.values())
            if (level.hasMastery())
                masteryLevels++;

        Parkour.getPluginLogger().info("Levels loaded: " + levels.size());
    }

    public void add(Level level)
    {
        levels.put(level.getName(), level);
    }

    public void create(String levelName)
    {
        LevelsDB.insertLevel(levelName);
        levels.put(levelName, new Level(levelName));
    }

    public void remove(String levelName)
    {
        LevelsDB.removeLevel(levelName);

        HashMap<String, PlayerStats> players = Parkour.getStatsManager().getPlayerStats();

        // thread safety
        synchronized (players)
        {
            for (PlayerStats playerStats : players.values())
            {
                // loop through and reset if applicable
                if (playerStats.inLevel() && playerStats.getLevel().equals(levelName))
                {
                    playerStats.resetLevel();
                    PracticeHandler.resetDataOnly(playerStats);
                    playerStats.resetCurrentCheckpoint();

                    if (playerStats.isAttemptingRankup())
                        Parkour.getRanksManager().leftRankup(playerStats);

                    // toggle off elytra armor
                    Parkour.getStatsManager().toggleOffElytra(playerStats);
                }
            }
        }
        levels.remove(levelName);
    }

    public Level get(String levelName) {
        return levels.get(levelName);
    }

    public Level getFromTitle(String levelTitle) {
        levelTitle = ChatColor.stripColor(levelTitle);

        for (Level level : levels.values())
            if (ChatColor.stripColor(level.getFormattedTitle()).equalsIgnoreCase(levelTitle))
                return level;

        return null;
    }

    public void addRating(Player player, Level level, int rating)
    {
        level.addRating(player.getName(), rating);
        RatingDB.addRating(player, level, rating);
    }

    public void removeRating(String playerName, Level level)
    {
        level.removeRating(playerName);
        RatingDB.removeRating(playerName, level);
    }

    public void toggleLiquidReset(Level level)
    {
        level.toggleLiquidReset();
        LevelsDB.updateLiquidReset(level.getName());
    }

    public void toggleNew(Level level)
    {
        level.toggleNew();
        LevelsDB.updateNew(level.getName());
    }

    public void toggleCooldown(Level level)
    {
        level.toggleCooldown();
        LevelsDB.updateCooldown(level.getName());
    }

    public void setDifficulty(Level level, int difficulty)
    {
        level.setDifficulty(difficulty);
        LevelsDB.updateDifficulty(level.getName(), difficulty);
    }
    public void setLevelType(Level level, LevelType type)
    {
        level.setLevelType(type);
        LevelsDB.setLevelType(level.getName(), type);
    }

    public void setTitle(Level level, String title)
    {
        level.setTitle(title);
        LevelsDB.updateTitle(level.getName(), title);
    }

    public void setPrice(Level level, int price)
    {
        level.setPrice(price);
        LevelsDB.updatePrice(level.getName(), price);
    }

    public void setRespawnY(Level level, int respawnY)
    {
        level.setRespawnY(respawnY);
        LevelsDB.updateRespawnY(level.getName(), respawnY);
    }

    public void setReward(Level level, int reward)
    {
        level.setReward(reward);
        LevelsDB.updateReward(level.getName(), reward);
    }

    public void setStartLocation(Level level, String locationName, Location location)
    {
        level.setStartLocation(location);
        LocationsDB.insertLocation(locationName, location);
    }

    public void setCompletionLocation(Level level, String locationName, Location location)
    {
        level.setCompletionLocation(location);
        LocationsDB.insertLocation(locationName, location);
    }

    public void setMaxCompletions(Level level, int maxCompletions)
    {
        level.setMaxCompletions(maxCompletions);
        LevelsDB.updateMaxCompletions(level.getName(), maxCompletions);
    }

    public void toggleBroadcastCompletion(Level level)
    {
        level.toggleBroadcast();
        LevelsDB.updateBroadcast(level.getName());
    }

    public void toggleHasMastery(Level level)
    {
        level.toggleHasMastery();
        LevelsDB.updateHasMastery(level.getName());

        // means we toggled it on (+1)
        if (level.hasMastery())
            masteryLevels++;
        // means we toggled it off (-1)
        else
            masteryLevels--;
    }

    public int getNumMasteryLevels() { return masteryLevels; }

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

    public void addCompletion(PlayerStats playerStats, Level level, LevelCompletion levelCompletion)
    {
        boolean alreadyFirstPlace = false;
        boolean firstCompletion = false;
        boolean validCompletion = false;

        level.addTotalCompletionsCount();

        if (levelCompletion.wasTimed())
        {
            if (!Parkour.getStatsManager().isLoadingLeaderboards())
            {
                HashMap<Integer, LevelCompletion> leaderboard = level.getLeaderboard();

                if (!leaderboard.isEmpty())
                {
                    // Compare completion against scoreboard
                    if (leaderboard.size() < 10 ||
                        leaderboard.get(leaderboard.size() - 1).getCompletionTimeElapsedMillis() > levelCompletion.getCompletionTimeElapsedMillis())
                    {
                        LevelCompletion firstPlace = level.getRecordCompletion();
                        String playerName = playerStats.getName();

                        // check for first place
                        if (firstPlace.getName().equalsIgnoreCase(playerName) &&
                            firstPlace.getCompletionTimeElapsedMillis() > levelCompletion.getCompletionTimeElapsedMillis())
                        {
                            leaderboard.remove(firstPlace);
                            alreadyFirstPlace = true;
                        }
                        // otherwise, search for where it is
                        else
                        {
                            int lbPositionToRemove = -1;
                            boolean completionSlower = false;

                            for (int i = 1; i < leaderboard.size(); i++)
                            {
                                LevelCompletion completion = leaderboard.get(i);

                                if (completion.getName().equalsIgnoreCase(playerName))
                                {
                                    if (completion.getCompletionTimeElapsedMillis() > levelCompletion.getCompletionTimeElapsedMillis())
                                        lbPositionToRemove = i;
                                    else
                                        completionSlower = true;

                                    break;
                                }
                            }

                            if (lbPositionToRemove > -1)
                                leaderboard.remove(lbPositionToRemove);
                            else if (completionSlower)
                                return;
                        }
                        sortNewCompletion(level, levelCompletion);
                        validCompletion = true;
                    }
                }
                else
                {
                    leaderboard.put(1, levelCompletion);
                    firstCompletion = true;
                }
                // only do record mod if it is a valid or first completion
                if (validCompletion || firstCompletion)
                    doRecordModification(playerStats, level, levelCompletion, alreadyFirstPlace, firstCompletion);
            }
        }
    }

    private void sortNewCompletion(Level level, LevelCompletion levelCompletion)
    {
        HashMap<Integer, LevelCompletion> leaderboard = level.getLeaderboard();
        HashMap<Integer, LevelCompletion> newLeaderboard = new HashMap<>(leaderboard);

        newLeaderboard.put(newLeaderboard.size() + 1, levelCompletion);

        for (int i = newLeaderboard.size(); i > 1; i--)
        {
            LevelCompletion completion = newLeaderboard.get(i);
            LevelCompletion nextCompletion = newLeaderboard.get(i - 1);

            if (nextCompletion.getCompletionTimeElapsedMillis() > completion.getCompletionTimeElapsedMillis())
            {
                // swap
                newLeaderboard.replace((i - 1), completion);
                newLeaderboard.replace(i, nextCompletion);
            }
        }
        // Trimming potential #11 datapoint
        if (newLeaderboard.size() > 10)
            newLeaderboard.remove(11);

        level.setLeaderboard(newLeaderboard);
    }

    private void doRecordModification(PlayerStats playerStats, Level level, LevelCompletion levelCompletion, boolean alreadyFirstPlace, boolean firstCompletion)
    {

        HashMap<Integer, LevelCompletion> leaderboard = level.getLeaderboard();
        // broadcast when record is beaten
        if (level.getRecordCompletion().getName().equalsIgnoreCase(levelCompletion.getName()))
        {
            String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

            // if first completion, make it record set
            if (firstCompletion)
                brokenRecord = "&e✦ &d&lRECORD SET &e✦";

            double completionTime = levelCompletion.getCompletionTimeElapsedSeconds();

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate(brokenRecord));
            Bukkit.broadcastMessage(Utils.translate("&d" + playerStats.getName() +
                    " &7has the new &8" + level.getFormattedTitle() +
                    " &7record with &a" + completionTime + "s"));
            Bukkit.broadcastMessage("");

            Utils.spawnFirework(level.getCompletionLocation(), Color.PURPLE, Color.FUCHSIA, true);

            if (!alreadyFirstPlace)
            {
                // update new #1 records
                playerStats.addRecord(levelCompletion);

                // if more than 1, remove
                if (leaderboard.size() > 1)
                {
                    LevelCompletion previousRecord = leaderboard.get(1);

                    PlayerStats previousStats = Parkour.getStatsManager().getByName(previousRecord.getName());

                    if (previousStats != null)
                        previousStats.removeRecord(previousRecord);

                    // remove previous
                    CompletionsDB.updateRecord(previousRecord);
                }
                // update new
                CompletionsDB.updateRecord(levelCompletion);
            }
            if (playerStats.hasModifier(ModifierType.RECORD_BONUS))
            {
                Bonus bonus = (Bonus) playerStats.getModifier(ModifierType.RECORD_BONUS);

                // add coins
                Parkour.getStatsManager().addCoins(playerStats, bonus.getBonus());
                playerStats.getPlayer().sendMessage(Utils.translate("&7You got &6" + Utils.formatNumber(bonus.getBonus()) + " &eCoins &7for getting the record!"));
            }
            // do gg run
            Parkour.getStatsManager().runGGTimer();
        }
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
                for (PlayerStats playerStats : Parkour.getStatsManager().getOnlinePlayers())
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
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60);

        // update global level completions and top rated completions lb every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalLevelCompletionsLB();
                loadTopRatedLevelsLB();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 15, 20 * 180);
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
                Parkour.getPluginLogger().info("Featured Level: " + level.getName());
            }
        }
    }

    /*
        this method first loops through the menus in config, then the pages, then the items, to find ALL levels that
        originate from the GUI so no featured level is picked from something like ascendance, etc
     */
    public void loadLevelsInMenus() {

        menuLevels.clear();
        for (String menuName : MenusYAML.getNames()) {
            Menu menu = Parkour.getMenuManager().getMenu(menuName);
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
                                        levelsInMenu.add(menuLevel);
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

    public Set<Level> getLevelsInAllMenus() {
        Set<Level> levelsInMenus = new HashSet<>();

        // loop through then add to new hashset
        for (Set<Level> levels : menuLevels.values())
            for (Level level : levels)
                levelsInMenus.add(level);

        return levelsInMenus;
    }

    public Set<Level> getLevelsFromMenu(Menu menu)
    {
        return menuLevels.get(menu);
    }

    public Level getFeaturedLevel() {
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

    public List<RaceLevel> getRaceLevels()
    {
        List<RaceLevel> temporaryRaceLevelList = new ArrayList<>();

        for (Level level : levels.values())
        {
            if (level.isRaceLevel())
                temporaryRaceLevelList.add((RaceLevel) level);
        }
        return temporaryRaceLevelList;
    }

    public long getTotalLevelCompletions() { return totalLevelCompletions; }

    public void addTotalLevelCompletion() { totalLevelCompletions++; }

    public void removeTotalLevelCompletion() { totalLevelCompletions--; }


    // top rated levels lb
    public void loadTopRatedLevelsLB() {
        try {

            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;
            topRatedLevelsLB.clear();
            int leaderboardPos = 1;

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {
                for (Level level : levels.values())
                    if (level.getRatingsCount() >= 5 && (highestLevel == null || (!addedLevels.contains(level.getName()) &&
                        level.getRating() >= highestLevel.getRating())))
                        highestLevel = level;

                // add 1 and add to added levels
                lbSize++;

                if (highestLevel != null) {
                    addedLevels.add(highestLevel.getName());
                    // add to temp lb
                    if (highestLevel.hasRating())
                    {
                        topRatedLevelsLB.put(leaderboardPos, highestLevel);
                        leaderboardPos++;
                    }

                    highestLevel = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, Level> getTopRatedLevelsLB() { return topRatedLevelsLB; }

    public HashMap<Integer, Level> getGlobalLevelCompletionsLB() {
        return globalLevelCompletionsLB;
    }

    public void loadGlobalLevelCompletionsLB() {
        try {
            Level highestLevel = null;
            Set<String> addedLevels = new HashSet<>();
            int lbSize = 0;
            int leaderboardPos = 1;

            globalLevelCompletionsLB.clear();

            while (Parkour.getSettingsManager().max_global_level_completions_leaderboard_size > lbSize) {

                for (Level level : levels.values())
                    if (level.getTotalCompletionsCount() > 0 &&
                       (highestLevel == null ||
                       (!addedLevels.contains(level.getName()) && level.getTotalCompletionsCount() >= highestLevel.getTotalCompletionsCount())))
                        highestLevel = level;

                // null check jic
                if (highestLevel != null)
                {
                    globalLevelCompletionsLB.put(leaderboardPos, highestLevel);
                    addedLevels.add(highestLevel.getName());
                    highestLevel = null;
                    leaderboardPos++;
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

        // since ascendance is a free-roam map...
        if (!level.isAscendance())
        {
            RanksManager ranksManager = Parkour.getRanksManager();
            if (!(level.needsRank() && ranksManager.isPastOrAtRank(playerStats, level.getRequiredRank())))
            {
                if (!level.isEventLevel())
                {
                    if (!level.isRaceLevel())
                    {
                        boolean teleport = true;

                        // not all levels have a price, so do a boolean switch
                        if (level.requiresBuying() && !playerStats.hasBoughtLevel(level) && !playerStats.hasCompleted(level))
                        {
                            teleport = false;
                            player.sendMessage(Utils.translate("&7You first need to buy &c" + level.getFormattedTitle() + "&7 before teleporting to it"));
                            player.sendMessage(Utils.translate(
                                    "&7Type &c&m/level buy " + level.getName() + "&7 &6(" + Utils.formatNumber(level.getPrice()) + " &eCoins&e) to buy " + ChatColor.stripColor(level.getFormattedTitle()
                            )));
                        }

                        // if still allowed, tp them!
                        if (teleport)
                            MenuItemAction.performLevelTeleport(playerStats, player, level);
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

    public void regionLevelCheck(PlayerStats playerStats, Location location)
    {
        // region null check
        ProtectedRegion region = WorldGuard.getRegion(location);
        if (region != null) {

            Level level = Parkour.getLevelManager().get(region.getId());

            // make sure the area they are spawning in is a level
            if (level != null)
            {
                playerStats.setLevel(level);

                // if elytra level, toggle on
                if (playerStats.getLevel().isElytra())
                    Parkour.getStatsManager().toggleOnElytra(playerStats);

                // if they have a cp, load it
                Location checkpoint = playerStats.getCheckpoint(level);
                if (checkpoint != null)
                    playerStats.setCurrentCheckpoint(checkpoint);
            }
        }
    }

    public int numLevels() { return levels.size(); }

    public Set<String> getNames() {
        return levels.keySet();
    }

    public void shutdown()
    {
        for (PlayerStats playerStats : Parkour.getStatsManager().getOnlinePlayers())
            Parkour.getStatsManager().toggleOffElytra(playerStats);
    }
}