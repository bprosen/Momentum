package com.renatusnetwork.momentum.data.placeholders;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.infinite.rewards.InfiniteReward;
import com.renatusnetwork.momentum.data.infinite.rewards.InfiniteRewards;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.entity.Player;

public class RewardsPlaceholders {

    public static final String REWARDS_PREFIX = "rewards";

    public static String processPlaceholder(Player player, String placeholder) {
        String[] split = placeholder.split("_");
        String rewardType = split[0];

        // future proofing!
        if (rewardType.equals("infinite")) {
            /*
                momentum_rewards_infinite_classic_(score)_score <--- needed for strikethrough, seems redundant at first
                momentum_rewards_infinite_classic_(score)_display
             */
            if (split.length == 4) {
                String infiniteType = split[1];
                String score = split[2];
                String value = split[3].toLowerCase();

                if (Utils.isInteger(score)) {
                    int scoreInt = Integer.parseInt(score);

                    try {
                        InfiniteRewards rewards = Momentum.getInfiniteManager().getRewards(InfiniteType.valueOf(infiniteType.toUpperCase()));

                        // make sure not null
                        if (rewards != null) {
                            // get from score
                            InfiniteReward reward = rewards.getReward(scoreInt);

                            if (reward != null) {
                                switch (value) {
                                    case "score":
                                        String scoreString = Utils.formatNumber(scoreInt);

                                        PlayerStats playerStats = Momentum.getStatsManager().get(player);

                                        // add strikethrough if they have it!
                                        if (playerStats != null && reward.hasReward(playerStats)) {
                                            scoreString = "&m" + scoreString;
                                        }

                                        return scoreString;
                                    case "display":
                                        return Utils.translate(reward.getDisplay());
                                }
                            }
                        }
                    } catch (IllegalArgumentException exception) {
                        return "Invalid infinite type";
                    }
                }
            }
        }
        return "";
    }
}
