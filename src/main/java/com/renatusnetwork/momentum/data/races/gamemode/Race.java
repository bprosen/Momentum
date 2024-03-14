package com.renatusnetwork.momentum.data.races.gamemode;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Race
{
    private RacePlayer[] players;
    private Level level;
    private int bet;
    private BukkitTask maxTimer;

    public Race(PlayerStats playerStats1, PlayerStats playerStats2, Level level, int bet) {
        initPlayers(playerStats1, playerStats2);
        this.level = level;
        this.bet = bet;
        startTimer();
    }

    public void start()
    {
        getPlayer1().start();
        getPlayer2().start();
    }

    public void end(RacePlayer winner, RaceEndReason endReason)
    {
        // get max timer and cancel right away
        maxTimer.cancel();

        RacePlayer loser = winner.getOpponent();
        PlayerStats winnerStats = winner.getPlayerStats();
        PlayerStats loserStats = loser.getPlayerStats();

        winner.resetPracAndCP();
        loser.resetPracAndCP();

        winner.showPlayersIfDisabled();
        loser.showPlayersIfDisabled();

        StatsManager statsManager = Momentum.getStatsManager();

        if (endReason == RaceEndReason.FORFEIT || endReason == RaceEndReason.WON)
        {
            // administer coins, and update data
            winner.win();
            winner.sendEndTitle(winnerStats);
            loser.loss();
            loser.sendEndTitle(winnerStats);

            String broadcast =
                    "&4" + winnerStats.getDisplayName() + "&a (▲" + Utils.formatNumber(winnerStats.getELO()) +
                    ")&7 beat &4" + loserStats.getDisplayName() + "&c (▼" + Utils.formatNumber(loserStats.getELO()) +
                    ")&7 on &c" + level.getTitle();

            if (hasBet())
                broadcast += "&7 for &6" + Utils.formatNumber(bet) + " &eCoins";

            Bukkit.broadcastMessage(Utils.translate(broadcast));
            
            if (endReason == RaceEndReason.FORFEIT)
            {
                String message = "&cYour opponent forfeit the race, giving a win";

                if (hasBet())
                    message += " &cand the &6" + Utils.formatNumber(bet) + " &eCoins &cbet";

                winnerStats.sendMessage(Utils.translate(message));
            }
        }
        else if (endReason == RaceEndReason.SHUTDOWN || endReason == RaceEndReason.OUT_OF_TIME)
        {
            // give back bet and do not touch stats
            if (hasBet())
            {
                boolean isOutOfTime = endReason == RaceEndReason.OUT_OF_TIME;

                statsManager.addCoins(winnerStats, getBet(), isOutOfTime);
                statsManager.addCoins(loserStats, getBet(), isOutOfTime);
            }

            winnerStats.resetRace();
            loserStats.resetRace();

            if (endReason == RaceEndReason.OUT_OF_TIME)
            {
                winnerStats.sendMessage(Utils.translate("&cYou ran out of time to complete the race"));
                loserStats.sendMessage(Utils.translate("&cYou ran out of time to complete the race"));
            }
        }
        winner.resetLevelAndTeleport();
        loser.resetLevelAndTeleport();
    }

    private void initPlayers(PlayerStats playerStats1, PlayerStats playerStats2)
    {
        RacePlayer racePlayer1 = new RacePlayer(playerStats1, this, playerStats1.getPlayer().getLocation());
        RacePlayer racePlayer2 = new RacePlayer(playerStats2, this, playerStats2.getPlayer().getLocation());

        this.players = new RacePlayer[]{racePlayer1, racePlayer2};

        racePlayer1.setOpponent(racePlayer2);
        racePlayer2.setOpponent(racePlayer1);

        playerStats1.setRace(racePlayer1);
        playerStats2.setRace(racePlayer2);
    }

    private RacePlayer getPlayer1()
    {
        return players[0];
    }

    private RacePlayer getPlayer2()
    {
        return players[1];
    }

    private void startTimer()
    {
        maxTimer = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                end(getPlayer1(), RaceEndReason.OUT_OF_TIME);
            }
        }.runTaskLater(Momentum.getPlugin(), 20 * 60 * Momentum.getSettingsManager().max_race_time);
    }

    public boolean hasBet() {
        return bet > 0;
    }

    public Level getLevel()
    {
        return level;
    }

    public int getBet()
    {
        return bet;
    }
}
