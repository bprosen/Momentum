package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketEvent;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.events.types.AscentEvent;
import com.renatusnetwork.parkour.data.infinite.gamemode.Infinite;
import com.renatusnetwork.parkour.data.infinite.gamemode.Sprint;
import com.renatusnetwork.parkour.data.infinite.gamemode.Timed;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Time;
import com.renatusnetwork.parkour.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
        }.runTaskTimerAsynchronously(plugin, 20, 2);
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

    public static void displayScoreboards()
    {
        HashMap<String, PlayerStats> stats = Parkour.getStatsManager().getPlayerStats();

        // ensure thread safety
        synchronized (stats)
        {
            for (PlayerStats playerStats : stats.values())
                displayScoreboard(playerStats);
        }
    }

    private static void displayScoreboard(PlayerStats playerStats)
    {
        if (playerStats != null && playerStats.hasBoard())
        {
            List<String> board = new ArrayList<>();
            BlackMarketManager blackMarketManager = Parkour.getBlackMarketManager();

            if (playerStats.isInBlackMarket() && blackMarketManager.isRunning())
            {
                playerStats.getBoard().updateTitle(Utils.translate("&8&lBlack Market"));

                board.add(Utils.translate("&7"));
                board.add(Utils.translate("  &8&lCoins &7" + Utils.formatNumber(playerStats.getCoins())));

                BlackMarketEvent blackMarketEvent = blackMarketManager.getRunningEvent();

                // add bidding section
                if (blackMarketEvent.isBiddingAllowed())
                {
                    if (blackMarketEvent.hasHighestBidder())
                    {
                        board.add(Utils.translate("  &8&lBid &7" + Utils.formatNumber(blackMarketEvent.getHighestBid())));
                        board.add(Utils.translate("  &8&lHolder &7" + blackMarketEvent.getHighestBidder().getName()));
                    }
                    board.add(Utils.translate("&7"));

                    board.add(Utils.translate("  &8&lNext Bid &7" + Utils.formatNumber(blackMarketEvent.getNextMinimumBid())));
                }

                board.add(Utils.translate("  &8&lPlaying &7" + Utils.formatNumber(blackMarketEvent.getPlayerCount())));

                board.add(Utils.translate("&7"));
                board.add(Utils.translate(formatSpacing("&crenatus.cc")));
            }
            else
            {
                playerStats.getBoard().updateTitle(Utils.translate("&c&lParkour"));

                Level level = playerStats.getLevel();
                EventManager eventManager = Parkour.getEventManager();

                board.add(Utils.translate("&7"));

                String coinBalance = Utils.translate("  &e&lCoins &6" + Utils.formatNumber(playerStats.getCoins()));
                board.add(coinBalance);

                // if they have a rank, show it
                if (playerStats.getRank() != null) {
                    String rankString = Utils.translate("  &e&lRank &6" + playerStats.getRank().getTitle());
                    board.add(rankString);
                }

                // if they have a clan, show it
                if (playerStats.getClan() != null) {
                    String clanString = Utils.translate("  &e&lClan &6" + playerStats.getClan().getTag());
                    board.add(clanString);
                }

                int fails = playerStats.getFails();
                if (!playerStats.isInInfinite() && !playerStats.isEventParticipant() && !playerStats.inRace() &&
                        !playerStats.isSpectating() && playerStats.inLevel() && !playerStats.getLevel().isAscendance() &&
                        playerStats.inFailMode() && !playerStats.isInTutorial() && fails > 0)
                    board.add(Utils.translate("  &e&lFails &6" + fails));

                // spectator section of scoreboard
                if (playerStats.isSpectating()) {

                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&c&lSpectating &6" + playerStats.getPlayerToSpectate().getName())));
                    board.add(formatSpacing(Utils.translate("&c/spectate &7to exit")));

                    // practice section of scoreboard
                } else if (playerStats.inPracticeMode()) {

                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&6Practice &a&lOn")));
                    board.add(formatSpacing(Utils.translate("&c/prac &7to exit")));

                    // race section of scoreboard
                } else if (playerStats.inRace()) {

                    board.add(Utils.translate("&7"));
                    Player opponent = Parkour.getRaceManager().get(playerStats.getPlayer()).getOpponent(playerStats.getPlayer());
                    PlayerStats opponentStats = Parkour.getStatsManager().get(opponent);

                    board.add(formatSpacing(Utils.translate("&6You are in a race!")));
                    board.add(formatSpacing(Utils.translate("&7vs. &c" + opponent.getName())));
                    // add timer
                    if (playerStats.getLevelStartTime() > 0) {
                        double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                        String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                        board.add(formatSpacing(timing));
                    }
                    board.add(Utils.translate("&7"));

                    // add wins, losses, winrate
                    board.add(formatSpacing(Utils.translate("&6Your W/L &e" +
                            Utils.shortStyleNumber(playerStats.getRaceWins()) + "/" + Utils.shortStyleNumber(playerStats.getRaceLosses()))));
                    board.add(formatSpacing(Utils.translate("&6Their W/L &e" +
                            Utils.shortStyleNumber(opponentStats.getRaceWins()) + "/" + Utils.shortStyleNumber(opponentStats.getRaceLosses()))));


                    // event section of scoreboard
                } else if (playerStats.isEventParticipant()) {

                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&2&l" + eventManager.getRunningEvent().getFormattedName())));
                    board.add(formatSpacing(Utils.translate("&6" + eventManager.getParticipants().size() + " &e&lPlaying")));
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&6&lTime Left")));
                    board.add(formatSpacing(Utils.translate("&7" + Time.elapsedShortened(eventManager.getTimeLeftMillis(), true))));

                    if (eventManager.isAscentEvent()) {
                        board.add(Utils.translate(""));
                        AscentEvent event = (AscentEvent) eventManager.getRunningEvent();
                        board.add(formatSpacing(Utils.translate("&e&lLevel &6" + event.getLevelID(playerStats.getPlayer()) + "/" + event.getLevelCount())));
                    }

                    // infinite parkour section of scoreboard
                } else if (playerStats.isInInfinite()) {

                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&d" + StringUtils.capitalize(playerStats.getInfiniteType().toString().toLowerCase()) + " &5Infinite")));

                    // add best if they have one
                    String scoreString = "&7Score &d" + Parkour.getInfiniteManager().get(playerStats.getName()).getScore();
                    if (playerStats.getBestInfiniteScore() > 0)
                        scoreString += " &7(&dBest " + playerStats.getBestInfiniteScore() + "&7)";

                    Infinite infinite = Parkour.getInfiniteManager().get(playerStats.getName());

                    board.add(formatSpacing(Utils.translate(scoreString)));

                    double timeLeft = 0.0;
                    if (infinite.isTimed())
                        timeLeft = ((Timed) infinite).getTimeLeft();
                    if (infinite.isSprint())
                        timeLeft = ((Sprint) infinite).getTimeLeft();

                    if (timeLeft > 0.0)
                        board.add(formatSpacing(Utils.translate("&7Time Left &d" + timeLeft + "s")));

                    // level section of scoreboard
                } else if (level != null) {

                    board.add(Utils.translate("&7"));

                    // change the entire scoreboard if it is a rankup level
                    if (level.isRankUpLevel() && playerStats.isAttemptingRankup()) {
                        Rank rank = playerStats.getRank();

                        // null check their rank to avoid NPE and same with next rank
                        if (rank != null) {
                            Rank nextRank = Parkour.getRanksManager().getNextRank(rank);

                            if (nextRank != null) {
                                board.add(Utils.translate("  &c&lRankup"));
                                board.add(Utils.translate("  &a" + rank.getTitle() + " &7-> &a" + nextRank.getTitle()));
                            }
                        }
                    } else {

                        if (level.isAscendance()) {

                            // add scoreboard
                            board.add(formatSpacing(Utils.translate("&8&lAscendance")));
                            board.add(formatSpacing(Utils.translate("&7Exploring")));

                            // do time if in a timed level
                            if (playerStats.getLevelStartTime() > 0) {
                                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                                String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                                board.add(formatSpacing(timing));
                            }
                        } else {

                            // normal scoreboard
                            String rewardString = Utils.translate("&6" + Utils.formatNumber(level.getReward()));
                            BankManager bankManager = Parkour.getBankManager();

                            int newReward = level.getReward();

                            // always modify level booster
                            if (playerStats.hasModifier(ModifierType.LEVEL_BOOSTER)) {
                                // downcast and boost
                                Booster booster = (Booster) playerStats.getModifier(ModifierType.LEVEL_BOOSTER);
                                newReward *= booster.getMultiplier();
                            }

                            // if level has mastery and player is in mastery
                            if (level.hasMastery() && playerStats.isAttemptingMastery())
                            {
                                board.add(formatSpacing(Utils.translate("&5&lMastery")));
                                newReward *= level.getMasteryMultiplier();
                            }
                            // add title and adjust rewardstring if it is a featured level
                            else if (level.isFeaturedLevel()) {
                                board.add(formatSpacing(Utils.translate("&dFeatured Level")));
                                newReward *= Parkour.getSettingsManager().featured_level_reward_multiplier;
                            } else if (bankManager.isJackpotRunning() &&
                                    bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                                    !bankManager.getJackpot().hasCompleted(playerStats.getName())) {
                                Jackpot jackpot = bankManager.getJackpot();
                                int bonus = jackpot.getBonus();

                                if (playerStats.hasModifier(ModifierType.JACKPOT_BOOSTER)) {
                                    // downcast and boost
                                    Booster booster = (Booster) playerStats.getModifier(ModifierType.JACKPOT_BOOSTER);
                                    bonus *= booster.getMultiplier();
                                }
                                // add bonus multiplier
                                newReward += bonus;

                                board.add(formatSpacing(Utils.translate("&a&lJACKPOT LEVEL")));
                            }
                            // modifier section
                            else
                            {
                                if (playerStats.hasPrestiges() && level.hasReward())
                                    newReward *= playerStats.getPrestigeMultiplier();

                                LevelManager levelManager = Parkour.getLevelManager();

                                // set cooldown modifier last!
                                if (level.hasCooldown() && levelManager.inCooldownMap(playerStats.getName()) && levelManager.getLevelCooldown(playerStats.getName()).getModifier() != 1.00)
                                    newReward *= levelManager.getLevelCooldown(playerStats.getName()).getModifier();
                            }

                            if (newReward != level.getReward())
                                rewardString = Utils.translate("&c&m" + Utils.formatNumber(level.getReward()) + "&6 " + Utils.formatNumber(newReward));

                            board.add(formatSpacing(level.getFormattedTitle()));
                            board.add(formatSpacing(rewardString));

                            if (playerStats.getLevelStartTime() > 0) {
                                double timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                                String timing = Utils.translate("&7" + Math.round((timeElapsed / 1000) * 10) / 10.0) + "s";
                                board.add(formatSpacing(timing));
                            } else {
                                board.add(formatSpacing(Utils.translate("&7-")));
                            }
                        }

                        // grind mode on scoreboard
                        if (playerStats.isGrinding())
                            board.add(formatSpacing(Utils.translate("&aGrinding")));
                    }
                }
                // add ip
                board.add(Utils.translate("&7"));
                board.add(formatSpacing(Utils.translate("&6renatus.cc")));
            }
            playerStats.updateBoard(board); // update board lines
        }
    }
}