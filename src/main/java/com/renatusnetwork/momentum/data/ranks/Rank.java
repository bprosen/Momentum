package com.renatusnetwork.momentum.data.ranks;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;

public class Rank {

    private String name;
    private String title;
    private Level rankupLevel;
    private String nextRank;

    public Rank(String name) {
        this.name = name;
    }

    public Rank(String name, String title, String rankupLevel, String nextRank) {
        this.name = name;
        this.title = title;
        this.rankupLevel = Momentum.getLevelManager().get(rankupLevel);
        this.nextRank = nextRank;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRankupLevel(Level level) {
        this.rankupLevel = level;
    }

    public void setNextRank(String nextRank) {
        this.nextRank = nextRank;
    }

    public String getNextRank() {
        return nextRank;
    }

    public Level getRankupLevel() {
        return rankupLevel;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean equals(Rank other) {
        return this.name.equalsIgnoreCase(other.getName());
    }

    public boolean isMaxRank() {
        return nextRank == null;
    }
}
