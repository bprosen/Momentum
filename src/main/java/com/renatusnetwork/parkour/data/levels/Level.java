package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level {

    private String name;
    private String title;
    private int reward;
    private int price;
    private float rating;
    private HashMap<String, Integer> ratings;
    private Location startLocation;
    private Location completionLocation;
    private int maxCompletions;
    private int playersInLevel;
    private boolean broadcast;
    private String requiredPermissionNode;
    private List<String> requiredLevels;
    private int respawnY;
    private boolean liquidResetPlayer = true; // default is true
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Location raceLocation1;
    private Location raceLocation2;
    private Material raceLevelItemType;
    private int totalCompletionsCount;
    private List<LevelCompletion> leaderboardCache = new ArrayList<>();
    private List<String> commands = new ArrayList<>();
    private Rank requiredRank;
    private int difficulty;
    private boolean cooldown;
    private LevelType type;
    private boolean newLevel;

    public Level(String levelName)
    {
        this.name = levelName;
        load();
    }

    public void setName(String name) { this.name = name; }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    public int getPlayersInLevel() { return playersInLevel; }

    public void setPlayersInLevel(int playersInLevel) { this.playersInLevel = playersInLevel; }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) { this.startLocation = startLocation;}

    public Location getCompletionLocation() { return completionLocation; }

    public void setCompletionLocation(Location completionLocation) { this.completionLocation = completionLocation; }

    public void toggleLiquidReset() { liquidResetPlayer = !liquidResetPlayer; }

    public boolean doesLiquidResetPlayer() { return liquidResetPlayer; }

    public boolean hasPermissionNode() {
        return requiredPermissionNode != null;
    }

    public String getRequiredPermissionNode() {
        return requiredPermissionNode;
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public void setMaxCompletions(int maxCompletions) { this.maxCompletions = maxCompletions; }

    public boolean isBroadcasting() { return broadcast; }

    public void toggleBroadcast() { this.broadcast = !this.broadcast; }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating() {
        return rating;
    }

    public int getPrice() { return price; }

    public boolean isBuyable() { return price > 0; }

    public void setPrice(int price) { this.price = price; }

    public int getRatingsCount() { return ratings.size(); }

    public boolean hasRated(String playerName)
    {
        return ratings.containsKey(playerName);
    }

    public int getRating(String playerName)
    {
        return ratings.getOrDefault(playerName, -1);
    }

    public void addRating(String playerName, int rating)
    {
        ratings.put(playerName, rating);
        calcRating();
    }

    public void removeRating(String playerName)
    {
        ratings.remove(playerName);
        calcRating();
    }

    public List<String> getUsersWhoRated(int rating)
    {
        List<String> tempList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : ratings.entrySet())
            if (entry.getValue() == rating)
                tempList.add(entry.getKey());

        return tempList;
    }

    private void calcRating()
    {
        long sumRatings = 0;

        for (Integer value : ratings.values())
            sumRatings += value;

        double newAverageRating = ((double) sumRatings) / ratings.size();

        // this makes it seperate digits by commands and .2 means round decimal by 2 places
        rating = Float.parseFloat(String.format("%,.2f", newAverageRating));
    }

    public List<String> getCommands() { return commands; }

    public boolean hasCommands() {
        return !commands.isEmpty();
    }

    public void setTotalCompletionsCount(int count) {
        totalCompletionsCount = count;
    }

    public int getTotalCompletionsCount() {
        return totalCompletionsCount;
    }

    public boolean isRankUpLevel()
    {
        return type == LevelType.RANKUP;
    }

    public boolean isEventLevel()
    {
        return type == LevelType.EVENT_ASCENT ||
               type == LevelType.EVENT_MAZE ||
               type == LevelType.EVENT_PVP ||
               type == LevelType.EVENT_FALLING_ANVIL ||
               type == LevelType.EVENT_RISING_WATER;
    }

    public void setLevelType(LevelType levelType)
    {
        this.type = levelType;
    }


    public int getDifficulty() { return difficulty; }

    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }

    public EventType getEventType()
    {
        EventType event = null;

        if (eventLevel)
            event = eventType;

        return event;
    }

    public void sortNewCompletion(LevelCompletion levelCompletion) {
        List<LevelCompletion> newLeaderboard = new ArrayList<>(leaderboardCache);

        if (newLeaderboard.size() > 0)
        {
            newLeaderboard.add(levelCompletion);

            boolean done = false;
            int currentIndex = newLeaderboard.size() - 1;

            // working from the tail iteration
            while (!done)
            {
                int nextIndex = currentIndex - 1;

                // if next is negative and current is less than next (higher lb position), swap!
                if (nextIndex >= 0 &&
                    newLeaderboard.get(nextIndex).getCompletionTimeElapsed() > newLeaderboard.get(currentIndex).getCompletionTimeElapsed())
                {
                    // swap
                    LevelCompletion temp = newLeaderboard.get(nextIndex);

                    newLeaderboard.set(nextIndex, newLeaderboard.get(currentIndex));
                    newLeaderboard.set(currentIndex, temp);
                }
                else
                {
                    done = true;
                }
                currentIndex--;
            }

            // Trimming potential #11 datapoint
            if (newLeaderboard.size() > 10)
                newLeaderboard.remove(10);
        } else
            newLeaderboard.add(0, levelCompletion);

        leaderboardCache = newLeaderboard;
    }

    public void addCompletion(String playerName, LevelCompletion levelCompletion)
    {
        if (totalCompletionsCount < 0)
            totalCompletionsCount = 0;

        boolean alreadyFirstPlace = false;
        boolean firstCompletion = false;
        boolean validCompletion = false;

        totalCompletionsCount += 1;

        if (levelCompletion.getCompletionTimeElapsed() <= 0.0)
            return;

        if (!Parkour.getStatsManager().isLoadingLeaderboards())
        {
            if (!leaderboardCache.isEmpty())
            {
                // Compare completion against scoreboard
                if (leaderboardCache.size() < 10 ||
                    leaderboardCache.get(leaderboardCache.size() - 1).getCompletionTimeElapsed() > levelCompletion.getCompletionTimeElapsed())
                {
                    LevelCompletion firstPlace = leaderboardCache.get(0);

                    // check for first place
                    if (firstPlace.getPlayerName().equalsIgnoreCase(playerName) && firstPlace.getCompletionTimeElapsed() > levelCompletion.getCompletionTimeElapsed())
                    {
                        leaderboardCache.remove(firstPlace);
                        alreadyFirstPlace = true;
                    }
                    // otherwise, search for where it is
                    else
                    {
                        LevelCompletion completionToRemove = null;
                        boolean completionSlower = false;

                        for (LevelCompletion completion : leaderboardCache)
                        {
                            if (completion.getPlayerName().equalsIgnoreCase(playerName))
                                if (completion.getCompletionTimeElapsed() > levelCompletion.getCompletionTimeElapsed())
                                    completionToRemove = completion;
                                else
                                    completionSlower = true;
                        }
                        if (completionToRemove != null)
                            leaderboardCache.remove(completionToRemove);
                        else if (completionSlower)
                            return;
                    }
                    sortNewCompletion(levelCompletion);
                    validCompletion = true;
                }
            }
            else
            {
                leaderboardCache.add(levelCompletion);
                firstCompletion = true;
            }
            // only do record mod if it is a valid or first completion
            if (validCompletion || firstCompletion)
                doRecordModification(levelCompletion, alreadyFirstPlace, firstCompletion);
        }
    }

    public void doRecordModification(LevelCompletion levelCompletion, boolean alreadyFirstPlace, boolean firstCompletion)
    {

        // broadcast when record is beaten
        if (leaderboardCache.get(0).getPlayerName().equalsIgnoreCase(levelCompletion.getPlayerName()))
        {
            String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

            // if first completion, make it record set
            if (firstCompletion)
                brokenRecord = "&e✦ &d&lRECORD SET &e✦";

            double completionTime = ((double) levelCompletion.getCompletionTimeElapsed()) / 1000;

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate(brokenRecord));
            Bukkit.broadcastMessage(Utils.translate("&d" + levelCompletion.getPlayerName() +
                    " &7has the new &8" + getFormattedTitle() +
                    " &7record with &a" + completionTime + "s"));
            Bukkit.broadcastMessage("");

            Utils.spawnFirework(respawnLocation, Color.PURPLE, Color.FUCHSIA, true);

            if (!alreadyFirstPlace)
            {
                // update new #1 records
                PlayerStats playerStats = Parkour.getStatsManager().getByName(levelCompletion.getPlayerName());

                playerStats.addRecord();

                // if more than 1, remove
                if (leaderboardCache.size() > 1)
                {
                    LevelCompletion previousRecord = leaderboardCache.get(1);

                    PlayerStats previousStats = Parkour.getStatsManager().getByName(previousRecord.getPlayerName());

                    if (previousStats != null)
                        previousStats.removeRecord();

                    // remove previous
                    CompletionsDB.updateRecord(previousRecord);
                }
                // update new
                CompletionsDB.updateRecord(levelCompletion);
            }
            PlayerStats playerStats = Parkour.getStatsManager().getByName(levelCompletion.getPlayerName()); // get player that got record

            if (playerStats.hasModifier(ModifierTypes.RECORD_BONUS))
            {
                Bonus bonus = (Bonus) playerStats.getModifier(ModifierTypes.RECORD_BONUS);

                // add coins
                Parkour.getStatsManager().addCoins(playerStats, bonus.getBonus());
                playerStats.getPlayer().sendMessage(Utils.translate("&7You got &6" + Utils.formatNumber(bonus.getBonus()) + " &eCoins &7for getting the record!"));
            }
            // do gg run
            Parkour.getStatsManager().runGGTimer();
        }
    }

    private void load() {
        if (LevelsYAML.exists(name))
        {

            if (LevelsYAML.isSet(name, "title"))
                title = LevelsYAML.getTitle(name);
            else
                title = name;

            String startLocationName = name + "-spawn";
            if (Parkour.getLocationManager().exists(startLocationName))
                startLocation = Parkour.getLocationManager().get(startLocationName);
            else
                startLocation = Parkour.getLocationManager().get("spawn");

            String respawnLocationName = name + "-completion";
            if (Parkour.getLocationManager().exists(respawnLocationName))
                respawnLocation = Parkour.getLocationManager().get(respawnLocationName);
            else
                respawnLocation = Parkour.getLocationManager().get("spawn");

            // this acts as a boolean for races
            if (LevelsYAML.isSection(name, "race")) {
                // this checks if player1 and player2 has locations
                raceLevel = true;

                if (LevelsYAML.isSet(name, "race.player1_loc") &&
                    LevelsYAML.isSet(name, "race.player2_loc")) {

                    raceLocation1 = LevelsYAML.getPlayerRaceLocation("player1", name);
                    raceLocation2 = LevelsYAML.getPlayerRaceLocation("player2", name);
                }

                if (LevelsYAML.isSet(name, "race.menu_item"))
                    raceLevelItemType = LevelsYAML.getRaceMenuItemType(name);
            }

            if (LevelsYAML.isSet(name, "event")) {
                eventLevel = true;
                eventType = LevelsYAML.getEventType(name);
            }

            isRankUpLevel = LevelsYAML.getRankUpLevelSwitch(name);
            maxCompletions = LevelsYAML.getMaxCompletions(name);
            broadcastCompletion = LevelsYAML.getBroadcastSetting(name);
            requiredLevels = LevelsYAML.getRequiredLevels(name);
            potionEffects = LevelsYAML.getPotionEffects(name);
            commands = LevelsYAML.getCommands(name);
            liquidResetPlayer = LevelsYAML.getLiquidResetSetting(name);
            requiredPermissionNode = LevelsYAML.getRequiredPermissionNode(name);
            respawnY = LevelsYAML.getRespawnY(name);
            elytraLevel = LevelsYAML.isElytraLevel(name);
            dropperLevel = LevelsYAML.isDropperLevel(name);
            tcLevel = LevelsYAML.isTCLevel(name);
            ascendanceLevel = LevelsYAML.isAscendanceLevel(name);
            price = LevelsYAML.getPrice(name);
            newLevel = LevelsYAML.getNewLevel(name);
            difficulty = LevelsYAML.getDifficulty(name);
            cooldown = LevelsYAML.hasCooldown(name);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    // special case where it needs to be run 1 tick later since the rank manager isnt registered yet, it will be if we skip this tick.
                    requiredRank = LevelsYAML.getRankRequired(name);
                }
            }.runTaskLater(Parkour.getPlugin(), 1);
        }
    }

    public boolean hasCooldown() { return cooldown; }

    public void toggleCooldown() { cooldown = !cooldown; }

    public boolean needsRank() { return requiredRank != null; }

    public Rank getRequiredRank() { return requiredRank; }

    public void setLeaderboardCache(List<LevelCompletion> levelCompletions) {
        leaderboardCache = levelCompletions;
    }

    public boolean hasRespawnY() {
        return respawnY > -1;
    }

    public int getRespawnY() {
        return respawnY;
    }

    public void setRespawnY(int respawnY) { this.respawnY = respawnY; }

    public boolean isElytra() { return type == LevelType.ELYTRA; }

    public boolean isDropper() { return type == LevelType.DROPPER; }

    public boolean isTC() { return type == LevelType.TC; }

    public boolean isNew() { return newLevel; }

    public void toggleNew() { newLevel = !newLevel; }

    public boolean isAscendance() { return type == LevelType.ASCENDANCE; }

    public List<LevelCompletion> getLeaderboard() {
        return leaderboardCache;
    }

    public boolean isRequiredLevel(String levelName) { return requiredLevels.contains(levelName); }

    public boolean hasRequiredLevels() { return !requiredLevels.isEmpty(); }

    public List<String> getRequiredLevels() { return requiredLevels; }

    public void addRequiredLevel(String levelName) { requiredLevels.add(levelName); }

    public void removeRequiredLevel(String levelName) { requiredLevels.remove(levelName); }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public Location getRaceLocation1() {
        return raceLocation1;
    }

    public Location getRaceLocation2() {
        return raceLocation2;
    }

    public boolean isRaceLevel() {
        return type == LevelType.RACE;
    }

    public boolean isFeaturedLevel()
    {
        return name.equalsIgnoreCase(Parkour.getLevelManager().getFeaturedLevel().getName());
    }

    public boolean hasValidRaceLocations() {
        if (LevelsYAML.isSet(name, "race.player1-loc") && LevelsYAML.isSet(name, "race.player2-loc"))
            return true;
        return false;
    }

    public Material getRaceLevelMenuItemType() {
        return raceLevelItemType;
    }

    public boolean hasRequiredLevels(PlayerStats playerStats)
    {
        for (String levelName : requiredLevels)
            if (playerStats.getLevelCompletionsCount(levelName) < 1)
                return false;

        return true;
    }

    public boolean equals(Level level)
    {
        return this.name.equalsIgnoreCase(level.getName());
    }

    public boolean equals(String levelName) { return this.name.equalsIgnoreCase(levelName); }

}