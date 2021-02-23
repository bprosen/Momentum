package com.parkourcraft.parkour.data.races;

import org.bukkit.entity.Player;

public class Race {

    private Player player1;
    private Player player2;

    public Race(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}
