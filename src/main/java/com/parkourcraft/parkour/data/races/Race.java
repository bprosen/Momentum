package com.parkourcraft.parkour.data.races;

import com.parkourcraft.parkour.data.levels.LevelObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Race {

    private Player player1;
    private Player player2;
    private boolean bet = false;
    private LevelObject level;
    private double betAmount;
    private Location originalPlayer1Loc = null;
    private Location originalPlayer2Loc = null;

    public Race(Player player1, Player player2, LevelObject level, boolean doingBet, double betAmount) {
        this.player1 = player1;
        this.player2 = player2;
        this.level = level;
        this.originalPlayer1Loc = player1.getLocation();
        this.originalPlayer2Loc = player2.getLocation();

        if (doingBet) {
            bet = true;
            this.betAmount = betAmount;
        }
    }

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

    public boolean isPlayer1(Player player) {
        if (player.getName().equalsIgnoreCase(player1.getName()))
            return true;
        return false;
    }

    public boolean isPlayer2(Player player) {
        if (player.getName().equalsIgnoreCase(player2.getName()))
            return true;
        return false;
    }

    public boolean hasBet() {
        return bet;
    }

    public LevelObject getRaceLevel() {
        if (level != null)
            return level;
        return null;
    }

    public double getBet() {
        if (bet)
            return betAmount;
        return 0.0;
    }

    public Player getOpponent(Player player) {
        // if player is player 1, then opponent is player2, and vice versa
        if (player1.getName().equalsIgnoreCase(player.getName()))
            return player2;
        else if (player2.getName().equalsIgnoreCase(player.getName()))
            return player1;

        return null;
    }
}
