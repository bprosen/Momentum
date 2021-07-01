package com.parkourcraft.parkour.data.ranks;

public class Rank {

    private String rankName;
    private int rankId;
    private double rankUpPrice;
    private String rankTitle;

    public Rank(String rankName, String ranktitle, int rankId, double rankUpPrice) {
        this.rankName = rankName;
        this.rankTitle = ranktitle;
        this.rankId = rankId;
        this.rankUpPrice = rankUpPrice;
    }

    public String getRankName() {
        return rankName;
    }

    public int getRankId() {
        return rankId;
    }

    public double getRankUpPrice() {
        return rankUpPrice;
    }

    public String getRankTitle() { return rankTitle; }
}
