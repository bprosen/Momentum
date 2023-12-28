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
    private Location raceLocation1;
    private Location raceLocation2;
    private Material raceLevelItemType;
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

    public void sortNewCompletion(LevelCompletion levelCompletion)
    {
        HashMap<Integer, LevelCompletion> newLeaderboard = new HashMap<>(leaderboard);

        newLeaderboard.put(newLeaderboard.size() + 1, levelCompletion);

        for (int i = newLeaderboard.size(); i > 1; i--)
        {
            LevelCompletion completion = newLeaderboard.get(i);
            LevelCompletion nextCompletion = newLeaderboard.get(i - 1);

            if (nextCompletion.getCompletionTimeElapsed() > completion.getCompletionTimeElapsed())
            {
                // swap
                newLeaderboard.replace((i - 1), completion);
                newLeaderboard.replace(i, nextCompletion);
            }
        }
        // Trimming potential #11 datapoint
        if (newLeaderboard.size() > 10)
            newLeaderboard.remove(11);

        leaderboard = newLeaderboard;
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
            if (!leaderboard.isEmpty())
            {
                // Compare completion against scoreboard
                if (leaderboard.size() < 10 ||
                        leaderboard.get(leaderboard.size() - 1).getCompletionTimeElapsed() > levelCompletion.getCompletionTimeElapsed())
                {
                    LevelCompletion firstPlace = leaderboard.get(0);

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