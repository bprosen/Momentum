package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Time;
import com.parkourcraft.parkour.utils.Utils;
import me.winterguardian.easyscoreboards.ScoreboardUtil;
import org.bukkit.Bukkit;
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
        Level level = Parkour.getLevelManager().get(playerStats.getLevel());
        EventManager eventManager = Parkour.getEventManager();

        // Title
        board.add(Utils.translate("&c&lRenatus Network"));

        board.add(Utils.translate("&7"));

        String coinBalance = Utils.translate("  &e&lCoins &6" + (int) Parkour.getEconomy().getBalance(player));
        board.add(coinBalance);

        if (playerStats.getRank() != null) {
            String rankString = Utils.translate("  &e&lRank &6" + playerStats.getRank().getRankTitle());
            board.add(rankString);
        }

        if (playerStats.getClan() != null) {
            String clanString = Utils.translate("  &e&lClan &6" + playerStats.getClan().getTag());
            board.add(clanString);
        }

        board.add(formatSpacing(Utils.translate("&7")));

        // spectator section of scoreboard
        if (playerStats.getPlayerToSpectate() != null) {

            board.add(formatSpacing(Utils.translate("&c&lSpectating &6" + playerStats.getPlayerToSpectate().getPlayerName())));
            board.add(formatSpacing(Utils.translate("&c/spectate &7to exit")));

        // practice section of scoreboard
        } else if (playerStats.getPracticeLocation() != null) {

            board.add(formatSpacing(Utils.translate("&6Practice &a&lOn")));
            board.add(formatSpacing(Utils.translate("&c/prac &7to exit")));

        // race section of scoreboard
        } else if (playerStats.inRace()) {

            board.add(formatSpacing(Utils.translate("&6You are in a race!")));
            board.add(formatSpacing(Utils.translate("&7vs. &c" + Parkour.getRaceManager().get(player)
                    .getOpponent(player).getName())));

        // event section of scoreboard
        } else if (playerStats.isEventParticipant()) {

            board.add(formatSpacing(Utils.translate("&7You are in an event!")));
            board.add(formatSpacing(Utils.translate("&2&l" +
                                    eventManager.formatName(eventManager.getEventType()))));
            board.add("");
            board.add(formatSpacing(Utils.translate("&6&lTime Left")));
            board.add(formatSpacing(Utils.translate("&e" + Time.elapsedShortened(eventManager.getTimeLeftMillis(), true))));

        // level section of scoreboard
        } else if (level != null) {

            String rewardString;

            // add title and adjust rewardstring if it is a featured level
            if (level.isFeaturedLevel()) {
                board.add(formatSpacing(Utils.translate("&dFeatured Level")));

                // proper cast
                rewardString = Utils.translate("&c&m" +
                        ((int) (level.getReward() / Parkour.getSettingsManager().featured_level_reward_multiplier)) +
                        "&6 " + level.getReward());
            } else {
                rewardString = Utils.translate("&6" + level.getReward());
            }

            String title = level.getFormattedTitle();
            board.add(formatSpacing(title));
            board.add(formatSpacing(rewardString));

            if (playerStats != null && playerStats.getLevelStartTime() > 0) {
                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                board.add(formatSpacing(timing));
            } else {
                board.add(formatSpacing(Utils.translate("&7-")));
            }
        }
        ScoreboardUtil.unrankedSidebarDisplay(player, board.toArray(new String[board.size()]));
    }

}
