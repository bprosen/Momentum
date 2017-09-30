package com.parkourcraft.Parkour.gameplay;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.PerkManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.settings.Settings_YAML;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DataQueries;
import com.parkourcraft.Parkour.utils.dependencies.WorldGuardUtils;
import com.parkourcraft.Parkour.data.LocationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class LevelHandler {

    static void levelCompletion(Player player, String levelName) {
        PlayerStats playerStats = StatsManager.get(player);
        LevelObject level = LevelManager.get(levelName);
        Location lobby = LocationManager.getLobbyLocation();

        if (playerStats != null
                && level != null
                && lobby != null) {
            Long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());
            LevelCompletion levelCompletion = new LevelCompletion(
                    System.currentTimeMillis(),
                    elapsedTime
            );

            level.addCompletion(); // Update totalLevelCompletionsCount

            // Update player information
            playerStats.levelCompletion(levelName, levelCompletion);
            DataQueries.insertCompletion(playerStats, level, levelCompletion);
            PerkManager.syncPermissions(player);
            Parkour.economy.depositPlayer(player, level.getReward());

            String messageFormatted = level.getFormattedMessage(playerStats);
            String time = (((double) elapsedTime) / 1000) + "s";
            if (elapsedTime > 0L
                    && elapsedTime < 8388607L)
                messageFormatted = messageFormatted.replace(
                        "%time%",
                        time
                );
            else
                messageFormatted = messageFormatted.replace(
                        "%time%",
                        "-"
                );

            String titleMessage = ChatColor.GRAY + "You Beat " + level.getFormattedTitle();
            if (elapsedTime > 0L)
                titleMessage += ChatColor.GRAY + " in " + ChatColor.GREEN + time;

            // Run gameplay actions: teleport and messaging
            player.teleport(lobby);
            player.sendMessage(messageFormatted);
            TitleAPI.sendTitle(
                    player, 10, 40, 10,
                    "",
                    titleMessage);

            // Broadcast the completion if enabled for the level
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

    static String getLocationLevelName(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = LevelManager.getNamesLower();

        for (String regionName : regionNames) {
            if (levelNamesLower.containsKey(regionName))
                return levelNamesLower.get(regionName);
        }

        return null;
    }

    static boolean locationInIgnoreArea(Location location) {
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

    static void respawnPlayerToStart(Player player, String levelName) {
        LevelObject level = LevelManager.get(levelName);

        if (level != null
                && level.getStartLocation() != null)
            player.teleport(level.getStartLocation());
    }

}
