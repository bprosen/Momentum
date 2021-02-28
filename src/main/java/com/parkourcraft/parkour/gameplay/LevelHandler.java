package com.parkourcraft.parkour.gameplay;

import com.connorlinfoot.titleapi.TitleAPI;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class LevelHandler {

    static void levelCompletion(Player player, String levelName) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        LevelObject level = Parkour.getLevelManager().get(levelName);

        if (playerStats != null && playerStats.getPlayerToSpectate() == null && level != null) {
            if (level.hasRequiredLevels(playerStats)) {
                if (playerStats.getPracticeLocation() == null) {
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
                            messageFormatted = messageFormatted.replace("%time%", "-");

                        String titleMessage = Utils.translate("&7You beat " + level.getFormattedTitle());
                        if (elapsedTime > 0L && elapsedTime < 8388607L)
                            titleMessage += Utils.translate("&7 in &2" + time);

                        // run gameplay actions: teleport and messaging
                        player.teleport(level.getRespawnLocation());
                        List<String> getToRegions = WorldGuard.getRegions(level.getRespawnLocation());

                        // if area they are teleporting to is empty
                        // if not empty, make sure it is a level
                        // if not a level (like spawn), reset level
                        if (getToRegions.isEmpty())
                            playerStats.resetLevel();
                        else if (Parkour.getLevelManager().get(getToRegions.get(0)) != null)
                            playerStats.setLevel(getToRegions.get(0));
                        else
                            playerStats.resetLevel();

                        Parkour.getStatsManager().get(player).resetCheckpoint();

                        player.sendMessage(messageFormatted);
                        TitleAPI.sendTitle(
                                player, 10, 40, 10,
                                "",
                                titleMessage
                        );

                        // broadcast completed if it the featured level
                        if (levelName.equalsIgnoreCase(Parkour.getLevelManager().getFeaturedLevel().getName())) {
                            Bukkit.broadcastMessage(Utils.translate(
                                    "&c&l" + player.getName() + " &7has completed the &6Featured Level &4" + level.getFormattedTitle()
                            ));
                        } else if (level.getBroadcastCompletion()) {
                            String broadcastMessage = Utils.translate(Parkour.getSettingsManager().levels_message_broadcast);

                            broadcastMessage = broadcastMessage.replace("%player%", player.getDisplayName());
                            broadcastMessage = broadcastMessage.replace("%title%", level.getFormattedTitle());

                            Bukkit.broadcastMessage(broadcastMessage);

                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou've reached the maximum number of completions"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot complete a level in practice mode"));
                }
            } else {
                    player.sendMessage(Utils.translate("&cYou do not have the required levels to complete this level"));
            }
        }
    }

    // No idea what this is for
    /*static boolean locationInIgnoreArea(Location location) {
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
    }*/

    // Respawn player if checkpoint isn't there
    static void respawnPlayer(Player player, LevelObject level) {
        Location loc = level.getStartLocation();

        if (level != null && loc != null)
            player.teleport(loc);
    }

    static void startedLevel(Player player) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (playerStats != null && playerStats.getPlayerToSpectate() == null) {
            playerStats.startedLevel();
        }
    }
}
