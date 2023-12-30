package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Level
{
    private String name;
    private String title;
    private int reward;
    private int price;
    private float rating;
    private Location startLocation;
    private Location completionLocation;
    private int maxCompletions;
    private int playersInLevel;
    private boolean broadcast;
    private String requiredPermission;
    private int respawnY;
    private boolean liquidResetPlayer;
    private int totalCompletionsCount;
    private String requiredRank;
    private int difficulty;
    private boolean cooldown;
    private LevelType type;
    private boolean newLevel;
    private boolean hasMastery;

    private HashMap<String, Integer> ratings;
    private List<String> requiredLevels;
    private List<PotionEffect> potionEffects;
    private HashMap<Integer, LevelCompletion> leaderboard;
    private List<String> commands;

    public Level(String levelName)
    {
        this.name = levelName;
        this.ratings = new HashMap<>();
        this.requiredLevels = new ArrayList<>();
        this.potionEffects = new ArrayList<>();
        this.leaderboard = new HashMap<>();
        this.commands = new ArrayList<>();
        this.liquidResetPlayer = true; // default is true
    }

    public void setCommands(List<String> commands)
    {
        this.commands = commands;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects)
    {
        this.potionEffects = potionEffects;
    }

    public void setRatings(HashMap<String, Integer> ratings)
    {
        this.ratings = ratings;
    }

    public void setRequiredLevels(List<String> requiredLevels)
    {
        this.requiredLevels = requiredLevels;
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

    public boolean hasReward() { return reward > 0; }

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

    public void setLiquidResetPlayer(boolean liquidResetPlayer) { this.liquidResetPlayer = liquidResetPlayer; }

    public boolean hasPermissionNode() {
        return requiredPermission != null;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public void setMaxCompletions(int maxCompletions) { this.maxCompletions = maxCompletions; }

    public boolean isBroadcasting() { return broadcast; }

    public void toggleBroadcast() { this.broadcast = !this.broadcast; }

    public void setBroadcast(boolean broadcast) { this.broadcast = broadcast; }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating() {
        return rating;
    }

    public boolean hasRating() { return rating > 0.0; }

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

    public void calcRating()
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

    public void addTotalCompletionsCount() { this.totalCompletionsCount++; }

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
        // simple switch case
        switch (type)
        {
            case EVENT_ASCENT:
                return EventType.ASCENT;
            case EVENT_MAZE:
                return EventType.MAZE;
            case EVENT_PVP:
                return EventType.PVP;
            case EVENT_FALLING_ANVIL:
                return EventType.FALLING_ANVIL;
            case EVENT_RISING_WATER:
                return EventType.RISING_WATER;
        }
        return null;
    }

    public boolean hasCooldown() { return cooldown; }

    public void toggleCooldown() { cooldown = !cooldown; }

    public void setCooldown(boolean cooldown) { this.cooldown = cooldown; }

    public boolean needsRank() { return requiredRank != null; }

    public String getRequiredRank() { return requiredRank; }

    public void setRequiredRank(String requiredRank) { this.requiredRank = requiredRank; }

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

    public void setNew(boolean isNew) { this.newLevel = isNew; }

    public boolean hasMastery() { return hasMastery; }

    public void toggleHasMastery() { hasMastery = !hasMastery; }

    public void setHasMastery(boolean hasMastery) { this.hasMastery = hasMastery; }

    public boolean isAscendance() { return type == LevelType.ASCENDANCE; }

    public HashMap<Integer, LevelCompletion> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(HashMap<Integer, LevelCompletion> leaderboard) { this.leaderboard = leaderboard; }

    public LevelCompletion getRecordCompletion() { return leaderboard.get(1); }

    public boolean isRequiredLevel(String levelName) { return requiredLevels.contains(levelName); }

    public boolean hasRequiredLevels() { return !requiredLevels.isEmpty(); }

    public List<String> getRequiredLevels() { return requiredLevels; }

    public void addRequiredLevel(String levelName) { requiredLevels.add(levelName); }

    public void removeRequiredLevel(String levelName) { requiredLevels.remove(levelName); }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public boolean isRaceLevel() {
        return type == LevelType.RACE;
    }

    public boolean isFeaturedLevel()
    {
        return name.equalsIgnoreCase(Parkour.getLevelManager().getFeaturedLevel().getName());
    }

    public boolean playerHasRequiredLevels(PlayerStats playerStats)
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