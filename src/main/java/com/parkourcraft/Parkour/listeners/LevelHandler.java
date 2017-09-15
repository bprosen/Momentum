package com.parkourcraft.Parkour.listeners;

import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.utils.dependencies.WorldGuardUtils;
import com.parkourcraft.Parkour.utils.storage.LocationManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class LevelHandler {

    public static String getLocationLevelName(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = LevelManager.getLevelNamesLower();

        for (String regionName : regionNames) {
            if (levelNamesLower.containsKey(regionName))
                return levelNamesLower.get(regionName);
        }

        return null;
    }

    public static boolean locationInIgnoreArea(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = LevelManager.getLevelNamesLower();

        boolean inIgnoreArea = true;

        for (String regionName : regionNames) {
            if (regionName.contains("ignore"))
                return true;

            if (levelNamesLower.containsKey(regionName))
                inIgnoreArea = false;
        }

        return inIgnoreArea;
    }

    public static void respawnPlayerToStart(Player player, String levelName) {
        if (LevelManager.levelConfigured(levelName)) {
            Location startLocation = LevelManager.getLevel(levelName).getStartLocation();

            if (startLocation != null)
                player.teleport(startLocation);
        }
    }

}
