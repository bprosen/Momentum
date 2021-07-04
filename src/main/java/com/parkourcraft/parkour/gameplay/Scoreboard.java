package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.ranks.Rank;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Time;
import com.parkourcraft.parkour.utils.Utils;
import me.winterguardian.easyscoreboards.ScoreboardUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Scoreboard {

    private static int boardWidth = 23;

    public static void startScheduler(Plugin plugin) {
        /*
            we can run the heaviest part when under stress in async,
            the stats display, but have to still run scoreboard creation in sync
         */
        new BukkitRunnable() {
            @Override
            public void run() {
                displayScoreboards();
            }
        }.runTaskTimerAsynchronously(plugin, 20, 10);
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
        for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
            displayScoreboard(playerStats);
    }

    private static void displayScoreboard(PlayerStats playerStats) {
        if (playerStats != null && playerStats.getPlayer() != null) {

            List<String> board = new ArrayList<>();
            Level level = Parkour.getLevelManager().get(playerStats.getLevel());
            EventManager eventManager = Parkour.getEventManager();

            board.add(Utils.translate("&c&lRenatus Network"));
            board.add(Utils.translate("&7"));

            String coinBalance = Utils.translate("  &e&lCoins &6" + (int) Parkour.getEconomy().getBalance(playerStats.getPlayer()));
            board.add(coinBalance);

            // if they have a rank, show it
            if (playerStats.getRank() != null) {
                String rankString = Utils.translate("  &e&lRank &6" + playerStats.getRank().getRankTitle());
                board.add(rankString);
            }

            // if they have a clan, show it
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

                Player opponent = Parkour.getRaceManager().get(playerStats.getPlayer()).getOpponent(playerStats.getPlayer());
                PlayerStats opponentStats = Parkour.getStatsManager().get(opponent);

                board.add(formatSpacing(Utils.translate("&6You are in a race!")));
                board.add(formatSpacing(Utils.translate("&7vs. &c" + opponent.getName())));

                board.add("");

                // add wins, losses, winrate
                board.add(formatSpacing(Utils.translate("&6Your W/L &e" +
                        Utils.shortStyleNumber(playerStats.getRaceWins()) + "/" + Utils.shortStyleNumber(playerStats.getRaceLosses()))));
                board.add(formatSpacing(Utils.translate("&6Their W/L &e" +
                        Utils.shortStyleNumber(opponentStats.getRaceWins()) + "/" + Utils.shortStyleNumber(opponentStats.getRaceLosses()))));


                // event section of scoreboard
            } else if (playerStats.isEventParticipant()) {

                board.add(formatSpacing(Utils.translate("&7You are in an event!")));
                board.add(formatSpacing(Utils.translate("&2&l" +
                        eventManager.formatName(eventManager.getEventType()))));
                board.add("");
                board.add(formatSpacing(Utils.translate("&6&lTime Left")));
                board.add(formatSpacing(Utils.translate("&e" + Time.elapsedShortened(eventManager.getTimeLeftMillis(), true))));

                // infinite parkour section of scoreboard
            } else if (playerStats.isInInfinitePK()) {

                board.add(formatSpacing(Utils.translate("&5Infinite Parkour")));

                // add best if they have one
                String scoreString = "&7Score &d" + Parkour.getInfinitePKManager().get(playerStats.getPlayerName()).getScore();
                if (playerStats.getInfinitePKScore() > 0)
                    scoreString += " &7(&dBest " + playerStats.getInfinitePKScore() + "&7)";

                board.add(formatSpacing(Utils.translate(scoreString)));

                // level section of scoreboard
            } else if (level != null) {
                // change the entire scoreboard if it is a rankup level
                if (level.isRankUpLevel()) {
                    Rank rank = playerStats.getRank();

                    // null check their rank to avoid NPE and same with next rank
                    if (rank != null) {
                        Rank nextRank = Parkour.getRanksManager().get(rank.getRankId() + 1);
                        if (nextRank != null) {
                            board.add(formatSpacing(Utils.translate("&cRankup Level")));
                            board.add(formatSpacing(Utils.translate("&7To &a" + nextRank.getRankTitle())));
                        }
                    }
                } else {
                    // normal scoreboard
                    String rewardString;

                    // add title and adjust rewardstring if it is a featured level
                    if (level.isFeaturedLevel()) {
                        board.add(formatSpacing(Utils.translate("&dFeatured Level")));

                        // proper cast
                        rewardString = Utils.translate("&c&m" +
                                ((int) (level.getReward() / Parkour.getSettingsManager().featured_level_reward_multiplier)) +
                                "&6 " + level.getReward());

                    } else if (playerStats.getPrestiges() > 0 && level.getReward() > 0)
                        rewardString = Utils.translate("&c&m" + level.getReward() + "&6 " + ((int) (level.getReward() * playerStats.getPrestigeMultiplier())));
                    else
                        rewardString = Utils.translate("&6" + level.getReward());

                    String title = level.getFormattedTitle();
                    board.add(formatSpacing(title));
                    board.add(formatSpacing(rewardString));

                    if (playerStats.getLevelStartTime() > 0) {
                        double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                        String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                        board.add(formatSpacing(timing));
                    } else {
                        board.add(formatSpacing(Utils.translate("&7-")));
                    }
                }
            }

            // now run in sync to have proper scoreboard creation
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (playerStats != null && playerStats.getPlayer() != null)
                        ScoreboardUtil.unrankedSidebarDisplay(playerStats.getPlayer(), board.toArray(new String[board.size()]));
                }
            }.runTask(Parkour.getPlugin());
        }
    }
}