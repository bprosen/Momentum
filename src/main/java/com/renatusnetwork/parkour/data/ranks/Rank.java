package com.renatusnetwork.parkour.data.ranks;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;

public class Rank {

    private String rankName;
    private String rankTitle;
    private Level rankupLevel;

    public Rank(String rankName, String rankTitle) {
        this.rankName = rankName;
        this.rankTitle = rankTitle;
        this.rankupLevel = Parkour.getLevelManager().get(RanksYAML.getRankUpLevel(rankName));
    }

    public Level getRankupLevel() { return rankupLevel; }

    public String getRankName() {
        return rankName;
    }

    public String getRankTitle() { return rankTitle; }
}
