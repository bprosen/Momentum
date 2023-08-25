package com.renatusnetwork.parkour.data.infinite.rewards;

import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;

import java.util.Collection;
import java.util.HashMap;

public class InfiniteRewards
{
    private InfiniteType type;
    private HashMap<Integer, InfiniteReward> rewards;

    public InfiniteRewards(InfiniteType type)
    {
        this.type = type;
        load();
    }

    private void load()
    {
        rewards = new HashMap<>();

        // load into rewards cache
        for (InfiniteReward reward : InfiniteRewardsYAML.getRewards(type))
            rewards.put(reward.getScoreNeeded(), reward);
    }

    public InfiniteReward getReward(int scoreNeeded)
    {
        return rewards.get(scoreNeeded);
    }

    public Collection<InfiniteReward> getRewards()
    {
        return rewards.values();
    }
}
