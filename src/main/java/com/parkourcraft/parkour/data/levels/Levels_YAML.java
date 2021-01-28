package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Levels_YAML {

    private static FileConfiguration levelsFile = Parkour.getConfigManager().get("levels");

    public static void commit(String levelName) {
        Parkour.getConfigManager().save("levels");
        Parkour.getLevelManager().load(levelName);
    }

    public static boolean exists(String levelName) {
        return levelsFile.isSet(levelName);
    }

    public static List<String> getNames() {
        return new ArrayList<String>(levelsFile.getKeys(false));
    }

    public static boolean isSet(String levelName, String valueName) {
        return levelsFile.isSet(levelName + "." + valueName);
    }

    public static void create(String levelName) {
        if (!exists(levelName)) {
            levelsFile.set(levelName + ".title", levelName);

            commit(levelName);
        }
    }

    public static void remove(String levelName) {
        if (exists(levelName)) {
            levelsFile.set(levelName, null);

            commit(levelName);
        }
    }

    public static void setTitle(String levelName, String title) {
        if (exists(levelName)) {
            levelsFile.set(levelName + ".title", title);

            commit(levelName);
        }
    }

    public static void setMessage(String levelName, String message) {
        if (exists(levelName)) {
            if (message.equals("default"))
                levelsFile.set(levelName + ".message", null);
            else
                levelsFile.set(levelName + ".message", message);

            commit(levelName);
        }
    }

    public static void setMaxCompletions(String levelName, int maxCompletions) {
        if (exists(levelName)) {
            if (maxCompletions == -1)
                levelsFile.set(levelName + ".max_completions", null);
            else
                levelsFile.set(levelName + ".max_completions", maxCompletions);

            commit(levelName);
        }
    }

    public static void setBroadcast(String levelName, boolean setting) {
        if (exists(levelName)) {
            if (!setting)
                levelsFile.set(levelName + ".broadcast_completion", null);
            else
                levelsFile.set(levelName + ".broadcast_completion", setting);

            commit(levelName);
        }
    }

    public static void setRequiredLevels(String levelName, List<String> requiredLevels) {
        if (exists(levelName)) {
            levelsFile.set(levelName + ".required_levels", requiredLevels);

            commit(levelName);
        }
    }

    public static List<String> getRequiredLevels(String levelName) {
        if (isSet(levelName, "required_levels"))
            return levelsFile.getStringList(levelName + ".required_levels");

        return new ArrayList<>();
    }

    public static String getTitle(String levelName) {
        if (levelsFile.isSet(levelName + ".title"))
            return levelsFile.getString(levelName + ".title");
        return "";
    }

    public static String getMessage(String levelName) {
        if (isSet(levelName, "message"))
            return levelsFile.getString(levelName + ".message");
        return "";
    }

    public static int getMaxCompletions(String levelName) {
        if (isSet(levelName, "max_completions"))
            return levelsFile.getInt(levelName + ".max_completions");
        return -1;
    }

    public static boolean getBroadcastSetting(String levelName) {
        if (isSet(levelName, "broadcast_completion"))
            return levelsFile.getBoolean(levelName + ".broadcast_completion");

        return false;
    }

}
