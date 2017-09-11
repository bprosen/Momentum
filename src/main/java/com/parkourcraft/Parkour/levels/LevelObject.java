package com.parkourcraft.Parkour.levels;

import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

public class LevelObject {

    private String levelName;
    private String title;
    private int reward;
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
            if (levels.isSet(levelName + ".reward"))
                reward = levels.getInt(levelName + ".reward");
            if (levels.isSet(levelName + ".start_location"))
                startLocation = levels.getString(levelName + ".start_location");
            if (levels.isSet(levelName + ".respawn_location"))
                startLocation = levels.getString(levelName + ".respawn_location");
        }
    }

}
