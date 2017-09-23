package com.parkourcraft.Parkour.listeners;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.settings.Settings_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.dependencies.WorldGuardUtils;
import com.parkourcraft.Parkour.data.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class LevelHandler {

    public static void levelCompletion(Player player, String levelName) {
        PlayerStats playerStats = StatsManager.getPlayerStats(player.getUniqueId().toString());

        if (playerStats != null
                && LevelManager.exists(levelName)) {
            LevelObject level = LevelManager.getLevel(levelName);
            Location respawnLocation = level.getRespawnLocation();

            if (respawnLocation != null) {
                playerStats.levelCompletion(
                        levelName,
                        System.currentTimeMillis(),
                        (System.currentTimeMillis() - playerStats.getLevelStartTime()),
                        false
                );

                respawnPlayerToLobby(player);

                Parkour.economy.depositPlayer(player, level.getReward());

                String messageFormatted = level.getFormattedMessage(playerStats.getLevelCompletionsCount(levelName));

                player.sendMessage(messageFormatted);

                if (level.getBroadcastCompletion()) {
                    String broadcastMessage = ChatColor.translateAlternateColorCodes(
                            '&',
                            Settings_YAML.getLevelBroadcastCompletionMessage()
                    );

                    broadcastMessage = broadcastMessage.replace("%player%", player.getDisplayName());
                    broadcastMessage = broadcastMessage.replace("%title%", level.getFormattedTitle());

                    Bukkit.broadcastMessage(broadcastMessage);
                }
            }
        }
    }

    public static String getLocationLevelName(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = LevelManager.getNamesLower();

        for (String regionName : regionNames) {
            if (levelNamesLower.containsKey(regionName))
                return levelNamesLower.get(regionName);
        }

        return null;
    }

    public static boolean locationInIgnoreArea(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = LevelManager.getNamesLower();

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
        if (LevelManager.exists(levelName)) {
            Location startLocation = LevelManager.getLevel(levelName).getStartLocation();

            if (startLocation != null)
                player.teleport(startLocation);
        }
    }

    public static void respawnPlayerToLobby(Player player) {
        if (LocationManager.exists("spawn")) {
            LocationManager.teleport(player, "spawn");
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "You have been teleported to the Parkour Lobby");
        }
    }

}
