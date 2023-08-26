package com.renatusnetwork.parkour.data.placeholders;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteReward;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteRewards;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.entity.Player;

public class RewardsPlaceholders
{
    public static final String REWARDS_PREFIX = "rewards";

    public static String processPlaceholder(Player player, String placeholder)
    {
        String[] split = placeholder.split("_");
        String rewardType = split[0];

        // future proofing!
        if (rewardType.equals("infinite"))
        {
            /*
                rn-parkour_rewards_infinite_classic_(score)_score <--- needed for strikethrough, seems redundant at first
                rn-parkour_rewards_infinite_classic_(score)_display
             */
            if (split.length == 4)
            {
                String infiniteType = split[1];
                String score = split[2];
                String value = split[3].toLowerCase();

                if (Utils.isInteger(score))
                {
                    int scoreInt = Integer.parseInt(score);

                    try
                    {
                        InfiniteRewards rewards = Parkour.getInfiniteManager().getRewards(InfiniteType.valueOf(infiniteType.toUpperCase()));

                        // make sure not null
                        if (rewards != null)
                        {
                            // get from score
                            InfiniteReward reward = rewards.getReward(scoreInt);

                            if (reward != null)
                            {
                                switch (value)
                                {
                                    case "score":
                                        String scoreString = Utils.formatNumber(scoreInt);

                                        PlayerStats playerStats = Parkour.getStatsManager().get(player);

                                        // add strikethrough if they have it!
                                        if (playerStats != null && reward.hasReward(playerStats))
                                            scoreString = "&m" + scoreString;

                                        return scoreString;
                                    case "display":
                                        return Utils.translate(reward.getDisplay());
                                }
                            }
                        }
                    }
                    catch (IllegalArgumentException exception)
                    {
                        return "Invalid infinite type";
                    }
                }
            }
        }
        return "";
    }
}
