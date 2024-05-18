package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.BankManager;
import com.renatusnetwork.momentum.data.bank.items.Jackpot;
import com.renatusnetwork.momentum.data.blackmarket.BlackMarketEvent;
import com.renatusnetwork.momentum.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.events.types.AscentEvent;
import com.renatusnetwork.momentum.data.infinite.gamemode.Infinite;
import com.renatusnetwork.momentum.data.infinite.gamemode.Sprint;
import com.renatusnetwork.momentum.data.infinite.gamemode.Timed;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.levels.LevelManager;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.TimeUtils;
import com.renatusnetwork.momentum.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Scoreboard {

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
        int boardWidth = 18;
        int padding = boardWidth - ChatColor.stripColor(input).length();

        return padding > 0 ? getSpaces(padding / 2) + input : input;
    }

    public static void displayScoreboards()
    {
        HashMap<String, PlayerStats> stats = Momentum.getStatsManager().getPlayerStats();

        // ensure thread safety
        synchronized (stats)
        {
            for (PlayerStats playerStats : stats.values())
                displayScoreboard(playerStats);
        }
    }

    private static void displayScoreboard(PlayerStats playerStats)
    {
        if (playerStats != null && playerStats.isLoaded() && playerStats.hasBoard())
        {
            List<String> board = new ArrayList<>();
            BlackMarketManager blackMarketManager = Momentum.getBlackMarketManager();

            if (playerStats.isInBlackMarket() && blackMarketManager.isRunning())
            {
                String timeUntilStart = TimeUtils.formatTimeWithSeconds(blackMarketManager.getTimeBeforeStart());
                playerStats.getBoard().updateTitle(Utils.translate("&8&lBlack Market"/* + (blackMarketManager.isInPreparation() && playerStats.isInBlackMarket() ? " &7in " + timeUntilStart : "")*/));

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

                // display time left until blackmarket starts for those waiting inside
                if (blackMarketManager.isInPreparation() && playerStats.isInBlackMarket()) {
                    board.add(Utils.translate("  &7&oStarts in " + TimeUtils.formatTimeWithSeconds(blackMarketManager.getTimeBeforeStart())));
                    board.add(Utils.translate("&7"));
                }

                board.add(formatSpacing(Utils.translate("&crenatus.cc")));
            }
            else
            {
                playerStats.getBoard().updateTitle(Utils.translate("&c&lParkour"));

                Level level = playerStats.getLevel();
                EventManager eventManager = Momentum.getEventManager();

                board.add(Utils.translate("&7"));

                String coinBalance = Utils.translate("  &e&lCoins &6" + Utils.formatNumber(playerStats.getCoins()));
                board.add(coinBalance);

                // if they have a rank, show it
                if (playerStats.hasRank())
                {
                    String rankString = Utils.translate("  &e&lRank &6" + playerStats.getRank().getTitle());
                    board.add(rankString);
                }

                if (playerStats.hasELOTier())
                {
                    String eloString = Utils.translate("  &e&lELO &6" + playerStats.getELOTierTitleWithLB());
                    board.add(eloString);
                }

                // if they have a clan, show it
                if (playerStats.inClan())
                {
                    String clanString = Utils.translate("  &e&lClan &6" + playerStats.getClan().getTag());
                    board.add(clanString);
                }

                int fails = playerStats.getFails();
                if (!playerStats.isInInfinite() && !playerStats.isEventParticipant() &&
                    !playerStats.isSpectating() && playerStats.inLevel() && !playerStats.getLevel().isAscendance() &&
                    playerStats.inFailMode() && !playerStats.isInTutorial() && fails > 0)
                    board.add(Utils.translate("  &e&lFails &6" + fails));

                // spectator section of scoreboard
                if (playerStats.isSpectating())
                {
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&c&lSpectating")));
                    board.add(formatSpacing(Utils.translate("&c" + playerStats.getPlayerToSpectate().getDisplayName())));
                    board.add(formatSpacing(Utils.translate("&c/spectate &7to exit")));
                }
                // infinite parkour section of scoreboard
                else if (playerStats.isInInfinite())
                {
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&d" + StringUtils.capitalize(playerStats.getInfiniteType().toString().toLowerCase()) + " &5Infinite")));

                    // add best if they have one
                    int currentScore = Momentum.getInfiniteManager().get(playerStats.getName()).getScore();
                    int bestScore = playerStats.getBestInfiniteScore();
                    String currentScoreFormatted = Utils.formatNumber(currentScore);

                    Infinite infinite = Momentum.getInfiniteManager().get(playerStats.getName());

                    board.add(formatSpacing(Utils.translate(currentScore <= bestScore ? "&7" + currentScoreFormatted : "&d" + currentScoreFormatted)));

                    double timeLeft = 0.0;
                    if (infinite.isTimed())
                        timeLeft = ((Timed) infinite).getTimeLeft();
                    if (infinite.isSprint())
                        timeLeft = ((Sprint) infinite).getTimeLeft();

                    if (timeLeft > 0.0)
                        board.add(formatSpacing(Utils.translate("&7Time Left &d" + timeLeft)));
                }
                // practice section of scoreboard
                else if (playerStats.inPracticeMode())
                {
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&e/prac history")));
                    board.add(formatSpacing(Utils.translate("&e/unprac &7to exit")));
                    board.add("");

                    Location pracCPLocation = playerStats.getPracticeCheckpoint();

                    float facing = Utils.translateYawToFacing(pracCPLocation.getYaw());

                    board.add(formatSpacing(Utils.translate("&7x &6" + Utils.formatDecimal(pracCPLocation.getX(), false, 3, 3))));
                    board.add(formatSpacing(Utils.translate("&7z &6" + Utils.formatDecimal(pracCPLocation.getZ(), false, 3, 3))));
                    board.add(formatSpacing(Utils.translate("&7f &6" + Utils.formatDecimal(facing, false, 1, 1))));
                }
                // race section of scoreboard
                else if (playerStats.inRace())
                {

                    board.add(Utils.translate("&7"));
                    Player opponent = playerStats.getRace().getOpponent().getPlayerStats().getPlayer();
                    PlayerStats opponentStats = Momentum.getStatsManager().get(opponent);

                    board.add(formatSpacing(Utils.translate("&6&lRace")));
                    board.add(formatSpacing(Utils.translate("&c" + opponent.getDisplayName())));
                    // add timer
                    if (playerStats.getLevelStartTime() > 0)
                    {
                        long timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                        String timing = Utils.translate("&7" + TimeUtils.formatCompletionTimeTaken(timeElapsed, 1));
                        board.add(formatSpacing(timing));
                    }
                    board.add(Utils.translate("&7"));

                    // add wins, losses, winrate
                    board.add(Utils.translate("&aYour ELO &2" + Utils.formatNumber(playerStats.getELO())));
                    board.add(Utils.translate("&aTheir ELO &2" + Utils.formatNumber(opponentStats.getELO())));
                }
                // event section of scoreboard
                else if (playerStats.isEventParticipant())
                {
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&2&l" + eventManager.getRunningEvent().getFormattedName())));
                    board.add(formatSpacing(Utils.translate("&6" + eventManager.getParticipants().size() + " &e&lPlaying")));
                    board.add(Utils.translate("&7"));
                    board.add(formatSpacing(Utils.translate("&6&lTime Left")));
                    board.add(formatSpacing(Utils.translate("&7" + TimeUtils.formatTimeWithSeconds(eventManager.getTimeLeftMillis()))));

                    if (eventManager.isAscentEvent()) {
                        board.add(Utils.translate(""));
                        AscentEvent event = (AscentEvent) eventManager.getRunningEvent();
                        board.add(formatSpacing(Utils.translate("&e&lLevel &6" + event.getLevelID(playerStats.getPlayer()) + "/" + event.getLevelCount())));
                    }
                }
                // level section of scoreboard
                else if (level != null)
                {

                    board.add(Utils.translate("&7"));

                    // change the entire scoreboard if it is a rankup level
                    if (level.isRankUpLevel() && playerStats.isAttemptingRankup()) {
                        Rank rank = playerStats.getRank();

                        // null check their rank to avoid NPE and same with next rank
                        if (rank != null) {
                            Rank nextRank = Momentum.getRanksManager().getNextRank(rank);

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
                                long timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                                String timing = Utils.translate("&7" + TimeUtils.formatCompletionTimeTaken(timeElapsed, 1));
                                board.add(formatSpacing(timing));
                            }
                        }
                        else if (playerStats.isPreviewingLevel())
                        {
                            board.add(formatSpacing(Utils.translate("&c&lPreview")));
                            board.add(formatSpacing(level.getFormattedTitle()));
                        }
                        else
                        {
                            // normal scoreboard
                            String rewardString = Utils.translate("&6" + Utils.formatNumber(level.getReward()));
                            BankManager bankManager = Momentum.getBankManager();

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
                                board.add(formatSpacing(Utils.translate("&5&lMASTERY")));
                                newReward *= level.getMasteryMultiplier();
                            }
                            // add title and adjust rewardstring if it is a featured level
                            else if (level.isFeaturedLevel()) {
                                board.add(formatSpacing(Utils.translate("&c&lFEATURED")));
                                newReward *= Momentum.getSettingsManager().featured_level_reward_multiplier;
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

                                board.add(formatSpacing(Utils.translate("&a&lJACKPOT")));
                            }
                            // modifier section
                            else
                            {
                                if (playerStats.hasPrestiges() && level.hasReward())
                                    newReward *= playerStats.getPrestigeMultiplier();

                                LevelManager levelManager = Momentum.getLevelManager();

                                // set cooldown modifier last!
                                if (level.hasCooldown() && levelManager.inCooldownMap(playerStats.getName()) && levelManager.getLevelCooldown(playerStats.getName()).getModifier() != 1.00)
                                    newReward *= levelManager.getLevelCooldown(playerStats.getName()).getModifier();
                            }

                            if (newReward != level.getReward())
                                rewardString = Utils.translate("&c&m" + Utils.formatNumber(level.getReward()) + "&6 " + Utils.formatNumber(newReward));

                            board.add(formatSpacing(level.getFormattedTitle()));
                            board.add(formatSpacing(rewardString));

                            if (playerStats.getLevelStartTime() > 0) {
                                long timeElapsed = System.currentTimeMillis() - playerStats.getLevelStartTime();

                                String timing = Utils.translate("&7" + TimeUtils.formatCompletionTimeTaken(timeElapsed, 1));
                                board.add(formatSpacing(timing));
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