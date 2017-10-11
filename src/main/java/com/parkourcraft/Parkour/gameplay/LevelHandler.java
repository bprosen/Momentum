package com.parkourcraft.Parkour.gameplay;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.LevelCompletion;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.data.stats.Stats_DB;
import com.parkourcraft.Parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;

public class LevelHandler {

    static void levelCompletion(Player player, String levelName) {
        PlayerStats playerStats = Parkour.stats.get(player);
        LevelObject level = Parkour.levels.get(levelName);

        if (playerStats != null
                && playerStats.getPlayerToSpectate() == null
                && level != null) {
            if (level.hasRequiredLevels(playerStats)) {
                int playerLevelCompletions = playerStats.getLevelCompletionsCount(levelName);

                if (level.getMaxCompletions() == -1
                        || playerLevelCompletions < level.getMaxCompletions()) {
                    Long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());
                    LevelCompletion levelCompletion = new LevelCompletion(
                            System.currentTimeMillis(),
                            elapsedTime
                    );

                    level.addCompletion(); // Update totalLevelCompletionsCount

                    // Update player information
                    playerStats.levelCompletion(levelName, levelCompletion);
                    Stats_DB.insertCompletion(playerStats, level, levelCompletion);
                    Parkour.perks.syncPermissions(player);
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
                    if (elapsedTime > 0L
                            && elapsedTime < 8388607L)
                        titleMessage += ChatColor.GRAY + " in " + ChatColor.GREEN + time;

                    // Run gameplay actions: teleport and messaging
                    player.teleport(level.getRespawnLocation());
                    player.sendMessage(messageFormatted);
                    TitleAPI.sendTitle(
                            player, 10, 40, 10,
                            "",
                            titleMessage
                    );

                    // Broadcast the completion if enabled for the level
                    if (level.getBroadcastCompletion()) {
                        String broadcastMessage = ChatColor.translateAlternateColorCodes(
                                '&',
                                Parkour.settings.levels_message_broadcast
                        );

                        broadcastMessage = broadcastMessage.replace("%player%", player.getDisplayName());
                        broadcastMessage = broadcastMessage.replace("%title%", level.getFormattedTitle());

                        Bukkit.broadcastMessage(broadcastMessage);

                    }
                } else
                    player.sendMessage(ChatColor.RED + "You've reached the maximum number of completions");
            } else
                player.sendMessage(ChatColor.RED + "You do not have the required levels to complete this level");
        }
    }

    static String getLocationLevelName(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = Parkour.levels.getNamesLower();

        for (String regionName : regionNames) {
            if (levelNamesLower.containsKey(regionName))
                return levelNamesLower.get(regionName);
        }

        return null;
    }

    static boolean locationInIgnoreArea(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = Parkour.levels.getNamesLower();

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
        LevelObject level = Parkour.levels.get(levelName);

        if (level != null
                && level.getStartLocation() != null)
            player.teleport(level.getStartLocation());
    }

    static void startedLevel(Player player) {
        PlayerStats playerStats = Parkour.stats.get(player);

        if (playerStats != null
                && playerStats.getPlayerToSpectate() == null) {
            LevelHandler.clearPotionEffects(player);
            playerStats.startedLevel();
        }
    }

    static void clearPotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }
}
