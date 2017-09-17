package com.parkourcraft.Parkour.levels;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.storage.LocationManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class LevelObject {

    private String levelName;
    private String title;
    private int reward = 0;
    private String startLocation = "spawn";
    private String respawnLocation = "spawn";
    private String message;
    private int maxCompletions = -1;

    public LevelObject(String levelName) {
        this.levelName = levelName;

        loadLevel();
    }

    private void loadLevel() {
        FileConfiguration levels = FileManager.getFileConfig("levels");
        FileConfiguration settings = FileManager.getFileConfig("settings");

        if (levels.isSet(levelName)) {

            if (levels.isSet(levelName + ".title"))
                title = levels.getString(levelName + ".title");
            else
                title = levelName;

            if (levels.isSet(levelName + ".reward"))
                reward = levels.getInt(levelName + ".reward");

            if (levels.isSet(levelName + ".start_location"))
                startLocation = levels.getString(levelName + ".start_location");

            if (levels.isSet(levelName + ".respawn_location"))
                respawnLocation = levels.getString(levelName + ".respawn_location");

            if (levels.isSet(levelName + ".message"))
                message = levels.getString(levelName + ".message");
            else
                message = settings.getString("levels.completion_message");

            if (levels.isSet(levelName + ".max_completions"))
                maxCompletions = levels.getInt(levelName + ".max_completions");
        }
    }

    public boolean passesBasicConfig() {
        if (title == null
                || startLocation == null)
            return false;
        return true;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleFormatted() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public int getReward() {
        return reward;
    }

    public String getStartLocationName() {
        return startLocation;
    }

    public Location getStartLocation() {
        if (startLocation != null
                && LocationManager.exists(startLocation))
            return LocationManager.get(startLocation);
        return null;
    }

    public String getRespawnLocationName() {
        return respawnLocation;
    }

    public Location getRespawnLocation() {
        if (respawnLocation != null
                && LocationManager.exists(respawnLocation))
            return LocationManager.get(respawnLocation);
        return null;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageFormatted(int completionsCount) {
        if (message != null) {
            message = ChatColor.translateAlternateColorCodes('&', message);

            message = message.replace("%title%", getTitleFormatted());
            message = message.replace("%reward%", Integer.toString(reward));
            message = message.replace("%completions%", Integer.toString(completionsCount));

            return message;
        }

        return "";
    }

    public int getMaxCompletions() {
        return maxCompletions;
    }

}
