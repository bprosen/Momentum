package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import me.winterguardian.easyscoreboards.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Scoreboard {

    private static int boardWidth = 23;

    public static void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                displayScoreboards();
            }
        }, 20L, 4L);
    }

    private static String getSpaces(int length) {
        String spaces = "";

        for (int i = 1; i <= length; i++)
            spaces += " ";

        return spaces;
    }

    private static String formatSpacing(String input) {
        int padding = boardWidth - input.length();

        if (padding > 0)
            return getSpaces(padding / 2) + input;

        return input;
    }

    public static void displayScoreboards() {
        for (Player player : Bukkit.getOnlinePlayers())
            displayScoreboard(player);
    }

    private static void displayScoreboard(Player player) {
        List<String> board = new ArrayList<>();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        LevelObject level = Parkour.getLevelManager().get(playerStats.getLevel());

        // Title
        board.add(Utils.translate("&c&lRenatus Network"));

        board.add(Utils.translate("&7"));

        if (playerStats.getRank() != null) {
            String rankString = Utils.translate("  &e&lRank &6" + playerStats.getRank().getRankTitle());
            board.add(rankString);
        }

        String coinBalance = Utils.translate("  &e&lCoins &6" + (int) Parkour.getEconomy().getBalance(player));
        board.add(coinBalance);
        board.add(formatSpacing(Utils.translate("&7")));

        // spectator section of scoreboard
        if (playerStats.getPlayerToSpectate() != null) {

            board.add(formatSpacing(Utils.translate("&c&lSpectating &6" + playerStats.getPlayerToSpectate().getPlayerName())));
            board.add(formatSpacing(Utils.translate("&c/spectate &7to exit")));

        // practice section of scoreboard
        } else if (playerStats.getPracticeLocation() != null) {

            board.add(formatSpacing(Utils.translate("&6Practice &a&lOn")));
            board.add(formatSpacing(Utils.translate("&c/prac &7to exit")));

        // level section of scoreboard
        } else if (level != null) {

            String title = level.getFormattedTitle();
            board.add(formatSpacing(title));

            String reward = Utils.translate("&6" + level.getReward());
            board.add(formatSpacing(reward));

            if (playerStats != null && playerStats.getLevelStartTime() > 0) {
                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                board.add(formatSpacing(timing));
            } else
                board.add(formatSpacing(Utils.translate("&7-")));
        }
        ScoreboardUtil.unrankedSidebarDisplay(player, board.toArray(new String[board.size()]));
    }

}
