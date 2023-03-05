package com.renatusnetwork.parkour.data.ranks;

public class Rank {

    private String rankName;
    private int rankId;
    private double rankUpPrice;
    private String rankTitle;
    private String shortRankTitle;

    public Rank(String rankName, String rankTitle, String shortRankTitle, int rankId, double rankUpPrice) {
        this.rankName = rankName;
        this.rankTitle = rankTitle;
        this.shortRankTitle = shortRankTitle;
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

    public String getShortRankTitle() { return shortRankTitle; }
}
