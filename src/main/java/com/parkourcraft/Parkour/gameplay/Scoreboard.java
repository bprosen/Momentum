package com.parkourcraft.Parkour.gameplay;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import me.winterguardian.easyscoreboards.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Scoreboard {

    private static int boardWidth = 23;

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
        LevelObject level = LevelManager.get(LevelHandler.getLocationLevelName(player.getLocation()));

        // Title
        board.add(
                ChatColor.GREEN + "" + ChatColor.BOLD + "Parkour"
                + ChatColor.WHITE + ChatColor.BOLD + "Craft "
        );

        String coinBalance =
                ChatColor.GOLD +  Integer.toString((int) Parkour.economy.getBalance(player))
                + ChatColor.YELLOW + ChatColor.BOLD + " Coins";
        board.add(formatSpacing(coinBalance));

        board.add(formatSpacing(ChatColor.GRAY + ""));

        if (level != null) {
            PlayerStats playerStats = StatsManager.get(player);

            String title = level.getFormattedTitle();
            board.add(formatSpacing(title));

            String reward = ChatColor.GOLD + Integer.toString(level.getReward());
            board.add(formatSpacing(reward));

            if (playerStats != null
                    && playerStats.getLevelStartTime() > 0) {
                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                String timing = ChatColor.GRAY + Double.toString(Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                board.add(formatSpacing(timing));
            } else
                board.add(formatSpacing(ChatColor.GRAY  + "-"));
        }


        ScoreboardUtil.unrankedSidebarDisplay(player, board.toArray(new String[board.size()]));
    }

}
