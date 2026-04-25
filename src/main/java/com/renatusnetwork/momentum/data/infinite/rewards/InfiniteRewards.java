package com.renatusnetwork.momentum.data.infinite.rewards;

import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;

import java.util.*;

public class InfiniteRewards {

    private InfiniteType type;
    private LinkedHashMap<Integer, InfiniteReward> rewards; // linked so order stays

    public InfiniteRewards(InfiniteType type) {
        this.type = type;
        load();
    }

    private void load() {
        rewards = new LinkedHashMap<>();

        // load into rewards cache
        for (InfiniteReward reward : InfiniteRewardsYAML.getRewards(type)) {
            rewards.put(reward.getScoreNeeded(), reward);
        }
    }

    public InfiniteReward getReward(int scoreNeeded) {
        return rewards.get(scoreNeeded);
    }

    public List<InfiniteReward> getRewards() {
        List<InfiniteReward> orderedRewards = new ArrayList<>();

        // need to retain order!
        for (int key : rewards.keySet()) {
            orderedRewards.add(rewards.get(key));
        }

        return orderedRewards;
    }

    public int size() {
        return rewards.size();
    }
}
