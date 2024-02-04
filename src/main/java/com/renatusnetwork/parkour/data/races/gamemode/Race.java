package com.renatusnetwork.parkour.data.races.gamemode;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
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

    public void start() {
        getPlayer1().start();
        getPlayer2().start();
    }

    public void end(RacePlayer winner, RaceEndReason endReason)
    {
        RacePlayer loser = winner.getOpponent();
        end(winner, loser, endReason);
    }

    public void end(RacePlayer winner, RacePlayer loser, RaceEndReason endReason)
    {
        // get max timer and cancel right away
        maxTimer.cancel();

        PlayerStats winnerStats = winner.getPlayerStats();
        PlayerStats loserStats = loser.getPlayerStats();

        StatsManager statsManager = Parkour.getStatsManager();

        if (endReason == RaceEndReason.DISCONNECTED || endReason == RaceEndReason.COMPLETED)
        {
            // administer coins, and update data
            winner.win();
            loser.loss();

            if (endReason == RaceEndReason.COMPLETED)
                statsManager.runGGTimer();
            else
                winnerStats.sendMessage(Utils.translate("&cYour opponent disconnected in the race, giving a win and the bet money"));
        }
        else if (endReason == RaceEndReason.SHUTDOWN || endReason == RaceEndReason.OUT_OF_TIME)
        {
            // give back bet and do not touch stats
            if (hasBet())
            {
                statsManager.addCoins(winnerStats, getBet());
                statsManager.addCoins(loserStats, getBet());
            }

            winnerStats.endRace();
            loserStats.endRace();

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
                end(getPlayer1(), getPlayer2(), RaceEndReason.OUT_OF_TIME);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 60 * 10);
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
