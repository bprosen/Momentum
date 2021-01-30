package com.parkourcraft.parkour.gameplay;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelHandler {

    static void levelCompletion(Player player, String levelName) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        LevelObject level = Parkour.getLevelManager().get(levelName);

        if (playerStats != null && playerStats.getPlayerToSpectate() == null && level != null) {
            if (level.hasRequiredLevels(playerStats)) {
                int playerLevelCompletions = playerStats.getLevelCompletionsCount(levelName);

                if (level.getMaxCompletions() == -1 || playerLevelCompletions < level.getMaxCompletions()) {
                    Long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());
                    LevelCompletion levelCompletion = new LevelCompletion(
                            System.currentTimeMillis(),
                            elapsedTime
                    );

                    levelCompletion.setPlayerName(player.getName());
                    Stats_DB.insertCompletion(playerStats, level, levelCompletion);
                    level.addCompletion(player, levelCompletion, level); // Update totalLevelCompletionsCount

                    // Update player information
                    playerStats.levelCompletion(levelName, levelCompletion);
                    Parkour.getEconomy().depositPlayer(player, level.getReward());

                    // This can be run in async, stops BIG sync loads and main thread pauses onCompletion
                    new BukkitRunnable() {
                        public void run() {
                            Parkour.getPerkManager().syncPermissions(player);
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());

                    String messageFormatted = level.getFormattedMessage(playerStats);
                    String time = (((double) elapsedTime) / 1000) + "s";
                    if (elapsedTime > 0L && elapsedTime < 8388607L)
                        messageFormatted = messageFormatted.replace("%time%", time);
                    else
                        messageFormatted = messageFormatted.replace("%time%","-");

                    String titleMessage = Utils.translate("&7You beat " + level.getFormattedTitle());
                    if (elapsedTime > 0L && elapsedTime < 8388607L)
                        titleMessage += Utils.translate("&7 in &2" + time);

                    // Run gameplay actions: teleport and messaging
                    player.teleport(level.getRespawnLocation());
                    List<String> getToRegions = WorldGuardUtils.getRegions(level.getRespawnLocation());
                    if (getToRegions.isEmpty())
                        Parkour.getLevelManager().removeFromLevelMap(player.getName());
                    else
                        Parkour.getLevelManager().addToLevelMap(player.getName(), getToRegions.get(0));

                    player.sendMessage(messageFormatted);
                    TitleAPI.sendTitle(
                            player, 10, 40, 10,
                            "",
                            titleMessage
                    );

                    // Broadcast the completion if enabled for the level
                    if (level.getBroadcastCompletion()) {
                        String broadcastMessage = Utils.translate(Parkour.getSettingsManager().levels_message_broadcast);

                        broadcastMessage = broadcastMessage.replace("%player%", player.getDisplayName());
                        broadcastMessage = broadcastMessage.replace("%title%", level.getFormattedTitle());

                        Bukkit.broadcastMessage(broadcastMessage);

                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou've reached the maximum number of completions"));
                }
            } else {
                    player.sendMessage(Utils.translate("&cYou do not have the required levels to complete this level"));
            }
        }
    }

    static String getLocationLevelName(Player player) {
        HashMap<String, String> playersInLevels = Parkour.getLevelManager().getPlayerRegionMap();

        if (playersInLevels.containsKey(player.getName()))
            return playersInLevels.get(player.getName());
        return null;
    }

    static boolean locationInIgnoreArea(Location location) {
        List<String> regionNames = WorldGuardUtils.getRegions(location);
        Map<String, String> levelNamesLower = Parkour.getLevelManager().getNamesLower();

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
        LevelObject level = Parkour.getLevelManager().get(levelName);

        if (level != null
                && level.getStartLocation() != null)
            player.teleport(level.getStartLocation());
    }

    static void startedLevel(Player player) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

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
