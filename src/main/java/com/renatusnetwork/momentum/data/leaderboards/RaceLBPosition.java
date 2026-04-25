<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/data/leaderboards/RaceLBPosition.java
package com.renatusnetwork.momentum.data.leaderboards;
========
package com.renatusnetwork.momentum.data.races;
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/data/races/RaceLBPosition.java

public class RaceLBPosition {

    private String playerName;
    private int wins;
    private float winRate;

    public RaceLBPosition(String playerName, int wins, float winRate) {
        this.playerName = playerName;
        this.wins = wins;
        this.winRate = winRate;
    }

    public String getName() {
        return playerName;
    }

    public int getWins() {
        return wins;
    }

    public float getWinRate() {
        return winRate;
    }
}
