package com.parkourcraft.Parkour.data.settings;

import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings_YAML {

    private static FileConfiguration settings = FileManager.getFileConfig("settings");

    public static String getLevelCompletionMessage() {
        return settings.getString("levels.message.completion");
    }

    public static String getLevelBroadcastCompletionMessage() {
        return settings.getString("levels.message.broadcast");
    }

}
