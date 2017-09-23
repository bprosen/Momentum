package com.parkourcraft.Parkour.data.levels;

import com.parkourcraft.Parkour.data.settings.Settings_YAML;
import com.parkourcraft.Parkour.data.LocationManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class LevelObject {

    private String levelName;
    private String title;
    private int reward = 0;
    private Location startLocation;
    private Location respawnLocation;
    private String message;
    private int maxCompletions;
    private boolean broadcastCompletion;

    public LevelObject(String levelName) {
        this.levelName = levelName;

        load();
    }

    private void load() {
        if (Levels_YAML.exists(levelName)) {

            if (Levels_YAML.isSet(levelName, "title"))
                title = Levels_YAML.getTitle(levelName);
            else
                title = levelName;

            reward = Levels_YAML.getReward(levelName);

            String startLocationName = levelName + "-spawn";
            if (LocationManager.exists(startLocationName))
                startLocation = LocationManager.get(startLocationName);
            else
                startLocation = LocationManager.get("spawn");

            String respawnLocationName = levelName + "-completion";
            if (LocationManager.exists(respawnLocationName))
                respawnLocation = LocationManager.get(respawnLocationName);
            else
                respawnLocation = LocationManager.get("spawn");

            if (Levels_YAML.isSet(levelName, "message"))
                message = Levels_YAML.getMessage(levelName);
            else
                message = Settings_YAML.getLevelCompletionMessage();

            maxCompletions = Levels_YAML.getMaxCompletions(levelName);

            broadcastCompletion = Levels_YAML.getBroadcastSetting(levelName);
        }
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

    public String getFormattedMessage(int completionsCount) {
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);

            message = message.replace("%title%", getFormattedTitle());
            message = message.replace("%reward%", Integer.toString(reward));
            message = message.replace("%completions%", Integer.toString(completionsCount));

            return message;
        }

        return "";
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

    public boolean getBroadcastCompletion() {
        return broadcastCompletion;
    }

}
