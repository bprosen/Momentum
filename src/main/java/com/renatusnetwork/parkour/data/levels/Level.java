package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventType;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Level {

    private String name;
    private String title;
    private int reward;
    private int price;
    private float rating;
    private int ratingsCount;
    private Location startLocation;
    private Location respawnLocation;
    private String message;
    private int maxCompletions;
    private int playersInLevel = 0;
    private boolean broadcastCompletion;
    private String requiredPermissionNode = null;
    private List<String> requiredLevels;
    private int respawnY;
    private int ID = -1;
    private int scoreModifier = 1;
    private boolean isRankUpLevel = false;
    private boolean liquidResetPlayer = true;
    private boolean elytraLevel = false;
    private boolean dropperLevel = false;
    private boolean newLevel = false;
    private List<PotionEffect> potionEffects = new ArrayList<>();

    private boolean raceLevel = false;

    private boolean eventLevel = false;
    private EventType eventType;

    private Location raceLocation1 = null;
    private Location raceLocation2 = null;
    private Material raceLevelItemType;

    private int totalCompletionsCount = 0;
    private List<LevelCompletion> leaderboardCache = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    private Rank requiredRank;

    private boolean ascendanceLevel = false;

    private int difficulty;

    public Level(String levelName) {
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

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public String getMessage() {
        return message;
    }

    public void toggleLiquidReset() { liquidResetPlayer = !liquidResetPlayer; }

    public boolean doesLiquidResetPlayer() { return liquidResetPlayer; }

    public String getFormattedMessage(PlayerStats playerStats) {
        if (message != null) {
            String returnMessage = Utils.translate(message);

            returnMessage = returnMessage.replace("%title%", getFormattedTitle());

            if (playerStats.getPrestiges() > 0)
                returnMessage = returnMessage.replace("%reward%", Utils.translate("&c&m" +
                        Utils.formatNumber(reward) + "&6 " +
                        Utils.formatNumber(reward * playerStats.getPrestigeMultiplier())));
            else if (isFeaturedLevel())
                returnMessage = returnMessage.replace("%reward%", Utils.translate("&c&m" +
                        Utils.formatNumber(reward) + "&6 " +
                        Utils.formatNumber((reward * Parkour.getSettingsManager().featured_level_reward_multiplier))));
            else
                returnMessage = returnMessage.replace("%reward%", Utils.formatNumber(reward));

            returnMessage = returnMessage.replace(
                    "%completions%",
                    Utils.shortStyleNumber(playerStats.getLevelCompletionsCount(name))
            );

            if (playerStats.inFailMode())
                returnMessage += Utils.translate(" &7with &6" + playerStats.getFails() + " Fails");

            return returnMessage;
        }

        return "";
    }

    public boolean hasPermissionNode() {
        if (requiredPermissionNode != null)
            return true;
        return false;
    }

    public String getRequiredPermissionNode() {
        return requiredPermissionNode;
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public boolean getBroadcastCompletion() {
        return broadcastCompletion;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating() {
        return rating;
    }

    public int getPrice() { return price; }

    public void setPrice(int price) { this.price = price; }

    public int getRatingsCount() { return ratingsCount; }

    public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }

    public void addRatingAndCalc(int rating) {

        // run new average = old average + (new rating + old average) / new count formula
        double newAverageRating = this.rating + (rating - this.rating) / (ratingsCount + 1);
        double newAmount = Double.valueOf(new BigDecimal(newAverageRating).toPlainString());
        // this makes it seperate digits by commands and .2 means round decimal by 2 places
        this.rating = Float.parseFloat(String.format("%,.2f", newAmount));
        ratingsCount += 1;
    }

    public void removeRatingAndCalc(int rating) {

        if (ratingsCount - 1 <= 0) {
            this.rating = 0.00f;
            this.ratingsCount = 0;

        } else {
            // run new average = old average + (new rating + old average) / new count formula
            double newAverageRating = this.rating + (rating - this.rating) / (ratingsCount - 1);
            double newAmount = Double.valueOf(new BigDecimal(newAverageRating).toPlainString());
            // this makes it seperate digits by commands and .2 means round decimal by 2 places
            this.rating = Float.parseFloat(String.format("%,.2f", newAmount));
            ratingsCount -= 1;
        }
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

    public boolean isRankUpLevel() {
        return isRankUpLevel;
    }

    public boolean isEventLevel() { return eventLevel; }

    public int getDifficulty() { return difficulty; }

    public EventType getEventType() {
        EventType event = null;

        if (eventLevel)
            event = eventType;

        return event;
    }

    public void setRankUpLevel(boolean isRankupLevel) {
        this.isRankUpLevel = isRankupLevel;
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

    public void addCompletion(String playerName, LevelCompletion levelCompletion) {
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
        String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

        // if first completion, make it record set
        if (firstCompletion)
            brokenRecord = "&e✦ &d&lRECORD SET &e✦";

        // broadcast when record is beaten
        if (leaderboardCache.get(0).getPlayerName().equalsIgnoreCase(levelCompletion.getPlayerName()))
        {
            double completionTime = ((double) levelCompletion.getCompletionTimeElapsed()) / 1000;

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate(brokenRecord));
            Bukkit.broadcastMessage(Utils.translate("&d" + levelCompletion.getPlayerName() +
                    " &7has the new &8" + getFormattedTitle() +
                    " &7record with &a" + completionTime + "s"));
            Bukkit.broadcastMessage("");

            Parkour.getLevelManager().doRecordBreakingFirework(respawnLocation);
            if (!alreadyFirstPlace)
            {
                // update new #1 records
                PlayerStats playerStats = Parkour.getStatsManager().getByName(levelCompletion.getPlayerName());

                if (playerStats != null)
                    Parkour.getStatsManager().addRecord(playerStats, playerStats.getRecords());
                else
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            StatsDB.addRecordsName(levelCompletion.getPlayerName());
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }

                // if more than 1, remove
                if (leaderboardCache.size() > 1)
                {
                    LevelCompletion previousRecord = leaderboardCache.get(1);

                    PlayerStats previousStats = Parkour.getStatsManager().getByName(previousRecord.getPlayerName());

                    if (previousStats != null)
                        Parkour.getStatsManager().removeRecord(previousStats, previousStats.getRecords());
                    else
                    {
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                StatsDB.removeRecordsName(previousRecord.getPlayerName());
                            }
                        }.runTaskAsynchronously(Parkour.getPlugin());
                    }
                }
            }
            // do gg run
            Parkour.getStatsManager().runGGTimer();
        }
    }

    public void setScoreModifier(int scoreModifier) {
        this.scoreModifier = scoreModifier;
    }

    public int getScoreModifier() {
        return scoreModifier;
    }

    private void load() {
        if (LevelsYAML.exists(name)) {

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

            if (LevelsYAML.isSet(name, "message"))
                message = LevelsYAML.getMessage(name);
            else
                message = Parkour.getSettingsManager().levels_message_completion;

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
            ascendanceLevel = LevelsYAML.isAscendanceLevel(name);
            price = LevelsYAML.getPrice(name);
            newLevel = LevelsYAML.getNewLevel(name);
            requiredRank = LevelsYAML.getRankRequired(name);
            difficulty = LevelsYAML.getDifficulty(name);
        }
    }

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

    public boolean isElytraLevel() { return elytraLevel; }

    public boolean isDropperLevel() { return dropperLevel; }

    public boolean isNewLevel() { return newLevel; }

    public void toggleNewLevel() { newLevel = !newLevel; }

    public boolean isAscendanceLevel() { return ascendanceLevel; }

    public List<LevelCompletion> getLeaderboard() {
        return leaderboardCache;
    }

    public List<String> getRequiredLevels() {
        return requiredLevels;
    }

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
        return raceLevel;
    }

    public boolean isFeaturedLevel() {
        if (name.equalsIgnoreCase(Parkour.getLevelManager().getFeaturedLevel().getName()))
            return true;
        return false;
    }

    public boolean hasValidRaceLocations() {
        if (LevelsYAML.isSet(name, "race.player1-loc") && LevelsYAML.isSet(name, "race.player2-loc"))
            return true;
        return false;
    }

    public Material getRaceLevelMenuItemType() {
        return raceLevelItemType;
    }

    public boolean hasRequiredLevels(PlayerStats playerStats) {
        for (String levelName : requiredLevels)
            if (playerStats.getLevelCompletionsCount(levelName) < 1)
                return false;

        return true;
    }
}