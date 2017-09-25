package com.parkourcraft.Parkour.data.levels;

import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.settings.Settings_YAML;
import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelObject {

    private String name;
    private String title;
    private int reward = 0;
    private Location startLocation;
    private Location respawnLocation;
    private String message;
    private int maxCompletions;
    private boolean broadcastCompletion;
    private int ID = -1;

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
        return ChatColor.translateAlternateColorCodes('&', title);
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
            String returnMessage = ChatColor.translateAlternateColorCodes('&', message);

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

    private void load() {
        if (Levels_YAML.exists(name)) {

            if (Levels_YAML.isSet(name, "title"))
                title = Levels_YAML.getTitle(name);
            else
                title = name;

            reward = Levels_YAML.getReward(name);

            String startLocationName = name + "-spawn";
            if (LocationManager.exists(startLocationName))
                startLocation = LocationManager.get(startLocationName);
            else
                startLocation = LocationManager.get("spawn");

            String respawnLocationName = name + "-completion";
            if (LocationManager.exists(respawnLocationName))
                respawnLocation = LocationManager.get(respawnLocationName);
            else
                respawnLocation = LocationManager.get("spawn");

            if (Levels_YAML.isSet(name, "message"))
                message = Levels_YAML.getMessage(name);
            else
                message = Settings_YAML.getLevelCompletionMessage();

            maxCompletions = Levels_YAML.getMaxCompletions(name);

            broadcastCompletion = Levels_YAML.getBroadcastSetting(name);
        }
    }

    public void loadLeaderboard() {
        leaderboardCache = new ArrayList<>();

        if (totalCompletionsCount != 0
                && ID != -1) {
            List<Map<String, String>> results = DatabaseQueries.getResults(
                    "completions",
                    "player_id, time_taken, UNIX_TIMESTAMP(completion_date) AS date",
                    "WHERE level_id=" + ID + " AND time_taken > 0 ORDER BY time_taken ASC LIMIT 10"
            );

            if (results.size() > 0) {
                for (Map<String, String> result : results) {
                    int playerID = Integer.parseInt(result.get("player_id"));

                    StatsManager.requiredID(playerID);

                    LevelCompletion levelCompletion =  new LevelCompletion(
                            Long.parseLong(result.get("date")),
                            Long.parseLong(result.get("time_taken")),
                            true
                    );

                    levelCompletion.setPlayerID(playerID);

                    leaderboardCache.add(levelCompletion);
                }
            } else
                totalCompletionsCount = 0;
        }
    }

    public List<LevelCompletion> getLeaderboard() {
        return leaderboardCache;
    }

}
