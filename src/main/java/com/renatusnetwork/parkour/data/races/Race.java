package com.renatusnetwork.parkour.data.races;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.RaceLevel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Race
{
    private Player player1;
    private Player player2;
    private boolean bet;
    private RaceLevel level;
    private double betAmount;
    private Location originalPlayer1Loc;
    private Location originalPlayer2Loc;
    private BukkitTask maxTimer;

    public Race(Player player1, Player player2, RaceLevel level, boolean doingBet, double betAmount) {
        this.player1 = player1;
        this.player2 = player2;
        this.level = level;
        this.originalPlayer1Loc = player1.getLocation();
        this.originalPlayer2Loc = player2.getLocation();

        if (doingBet)
        {
            this.bet = true;
            this.betAmount = betAmount;
        }

        Race race = this;

        maxTimer = new BukkitRunnable() {
            @Override
            public void run() {
                Parkour.getRaceManager().forceEndRace(race, false);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 60 * 10);
    }

    public BukkitTask getMaxTimer() { return maxTimer; }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Location getOriginalPlayer1Loc() {
        return originalPlayer1Loc;
    }

    public Location getOriginalPlayer2Loc() {
        return originalPlayer2Loc;
    }

    public boolean isPlayer1(Player player)
    {
        return player.getName().equalsIgnoreCase(player1.getName());
    }

    public boolean hasBet() {
        return bet;
    }

    public RaceLevel getLevel()
    {
        return level;
    }

    public double getBet()
    {
        return betAmount;
    }

    public Player getOpponent(Player player)
    {
        // if player is player 1, then opponent is player2, and vice versa
        if (player1.getName().equalsIgnoreCase(player.getName()))
            return player2;
        else if (player2.getName().equalsIgnoreCase(player.getName()))
            return player1;

        return null;
    }
}
