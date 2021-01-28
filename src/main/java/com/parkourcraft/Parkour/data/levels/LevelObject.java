package com.parkourcraft.Parkour.data.levels;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class  LevelObject {

    private String name;
    private String title;
    private int reward = 0;
    private Location startLocation;
    private Location respawnLocation;
    private String message;
    private int maxCompletions;
    private boolean broadcastCompletion;
    private List<String> requiredLevels;
    private int ID = -1;
    private int scoreModifier = 1;

    private int totalCompletionsCount = -1;
    private List<LevelCompletion> leaderboardCache = new ArrayList<>();

    public LevelObject(String levelName) {
        this.name = levelName;

        load();
    }

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

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage(PlayerStats playerStats) {
        if (message != null) {
            String returnMessage = Utils.translate(message);

            returnMessage = returnMessage.replace("%title%", getFormattedTitle());
            returnMessage = returnMessage.replace("%reward%", Integer.toString(reward));
            returnMessage = returnMessage.replace(
                    "%completions%",
                    Integer.toString(playerStats.getLevelCompletionsCount(name))
            );

            return returnMessage;
        }

        return "";
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

    public void setTotalCompletionsCount(int count) {
        totalCompletionsCount = count;
    }

    public int getTotalCompletionsCount() {
        return totalCompletionsCount;
    }

    public void addCompletion() {
        if (totalCompletionsCount < 0)
            totalCompletionsCount = 0;

        totalCompletionsCount += 1;
    }

    public void setScoreModifier(int scoreModifier) {
        this.scoreModifier = scoreModifier;
    }

    public int getScoreModifier() {
        return scoreModifier;
    }

    private void load() {
        if (Levels_YAML.exists(name)) {

            if (Levels_YAML.isSet(name, "title"))
                title = Levels_YAML.getTitle(name);
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

            if (Levels_YAML.isSet(name, "message"))
                message = Levels_YAML.getMessage(name);
            else
                message = Parkour.getSettingsManager().levels_message_completion;

            maxCompletions = Levels_YAML.getMaxCompletions(name);

            broadcastCompletion = Levels_YAML.getBroadcastSetting(name);

            requiredLevels = Levels_YAML.getRequiredLevels(name);
        }
    }

    public void setLeaderboardCache(List<LevelCompletion> levelCompletions) {
        leaderboardCache = levelCompletions;
    }

    public List<LevelCompletion> getLeaderboard() {
        return leaderboardCache;
    }

    public List<String> getRequiredLevels() {
        return requiredLevels;
    }

    public boolean hasRequiredLevels(PlayerStats playerStats) {
        for (String levelName : requiredLevels)
            if (playerStats.getLevelCompletionsCount(levelName) < 1)
                return false;

        return true;
    }
}