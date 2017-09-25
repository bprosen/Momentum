package com.parkourcraft.Parkour.utils.dependencies;


import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.listeners.LevelHandler;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class FeatherboardPlaceholders {

    public static void registerPlaceholders() {

        PlaceholderAPI.registerPlaceholder(Parkour.getPlugin(), "parkour_title",
                new PlaceholderReplacer() {

                    @Override
                    public String onPlaceholderReplace(
                            PlaceholderReplaceEvent event) {
                        //boolean online = event.isOnline();
                        Player player = event.getPlayer();

                        if ((OfflinePlayer) event.getPlayer() == null)
                            return "Player needed!";

                        LevelObject level = LevelManager.get(LevelHandler.getLocationLevelName(player.getLocation()));

                        if (level != null)
                            return level.getFormattedTitle();

                        return ChatColor.GRAY + "" + ChatColor.BOLD + "Lobby";
                    }

                });

        PlaceholderAPI.registerPlaceholder(Parkour.getPlugin(), "parkour_time",
                new PlaceholderReplacer() {

                    @Override
                    public String onPlaceholderReplace(
                            PlaceholderReplaceEvent event) {
                        Player player = event.getPlayer();

                        if ((OfflinePlayer) event.getPlayer() == null)
                            return "Player needed!";

                        LevelObject level = LevelManager.get(LevelHandler.getLocationLevelName(player.getLocation()));
                        PlayerStats playerStats = StatsManager.get(player);

                        if (level != null) {
                            if (playerStats != null
                                    && playerStats.getLevelStartTime() > 0) {
                                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                                return Math.round((timeElapsed / 1000) * 10) / 10.0 + "s";
                            } else
                                return "-";
                        }

                        return "";
                    }

                });

        PlaceholderAPI.registerPlaceholder(Parkour.getPlugin(), "parkour_reward",
                new PlaceholderReplacer() {

                    @Override
                    public String onPlaceholderReplace(
                            PlaceholderReplaceEvent event) {
                        Player player = event.getPlayer();

                        if ((OfflinePlayer) event.getPlayer() == null)
                            return "Player needed!";

                        LevelObject level = LevelManager.get(LevelHandler.getLocationLevelName(player.getLocation()));

                        if (level != null)
                            return Integer.toString(level.getReward());

                        return "";
                    }

                });
    }

}
