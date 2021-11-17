package com.renatusnetwork.parkour.data.races;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;

public class RaceRequest {

    private PlayerStats player1;
    private PlayerStats player2;
    private Level selectedLevel = null;
    private double bet = -1.0;

    public RaceRequest(PlayerStats player1, PlayerStats player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public PlayerStats getPlayer1() { return player1; }

    public PlayerStats getPlayer2() { return player2; }

    public void setSelectedLevel(Level selectedLevel) { this.selectedLevel = selectedLevel; }

    public void setBet(double bet) { this.bet = bet; }

    public boolean hasBet() { return bet > 0.0; }

    public double getBet() { return bet; }

    public Level getSelectedLevel() { return selectedLevel; }

    public boolean randomLevel() { return selectedLevel == null; }
}
