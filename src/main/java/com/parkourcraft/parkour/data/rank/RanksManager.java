package com.parkourcraft.parkour.data.rank;

import com.parkourcraft.parkour.Parkour;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RanksManager {

    private static List<Rank> rankList = new ArrayList<>();

    public RanksManager() {
        load();
    }

    public void add(String rankName) {
        // get from YAML
        String rankTitle = Ranks_YAML.getRankTitle(rankName);
        int rankId = Ranks_YAML.getRankId(rankName);
        double rankUpPrice = Ranks_YAML.getRankUpPrice(rankName);

        Rank rank = new Rank(rankName, rankTitle, rankId, rankUpPrice);
        rankList.add(rank);
    }

    public Rank get(int rankId) {
        for (Rank rank : rankList)
            if (rank.getRankId() == rankId)
                return rank;

        return null;
    }

    public Rank get(String rankName) {
        for (Rank rank : rankList)
            if (rank.getRankName().equalsIgnoreCase(rankName))
                return rank;

        return null;
    }

    public void load() {
        rankList = new ArrayList<>();

        for (String rankName : Ranks_YAML.getNames())
            load(rankName);

        Parkour.getPluginLogger().info("Ranks loaded: " + rankList.size());
    }

    public void load(String rankName) {

        boolean exists = exists(rankName);

        if (!Ranks_YAML.exists(rankName) && exists)
            remove(rankName);
        else {
            if (exists)
                remove(rankName);

            add(rankName);
        }
    }

    public void remove(String rankName) {
        for (Iterator<Rank> iterator = rankList.iterator(); iterator.hasNext();) {
            if (iterator.next().getRankName().equals(rankName)) {
                Ranks_YAML.remove(iterator.getClass().getName());
                iterator.remove();
            }
        }
    }

    public boolean exists(String rankName) {
        return (get(rankName) != null);
    }
}