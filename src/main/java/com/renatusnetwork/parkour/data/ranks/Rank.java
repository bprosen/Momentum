package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;

public class Rank {

    private String rankName;
    private int rankId;
    private String rankTitle;
    private Level rankupLevel;

    public Rank(String rankName, String rankTitle, int rankId) {
        this.rankName = rankName;
        this.rankTitle = rankTitle;
        this.rankId = rankId;
        this.rankupLevel = Parkour.getLevelManager().get(RanksYAML.getRankUpLevel(rankName));
    }

    public Level getRankupLevel() { return rankupLevel; }

    public String getRankName() {
        return rankName;
    }

    public int getRankId() {
        return rankId;
    }

    public String getRankTitle() { return rankTitle; }
}
