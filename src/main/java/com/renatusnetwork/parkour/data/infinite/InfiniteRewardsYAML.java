package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

public class InfiniteRewardsYAML {

    private static FileConfiguration rewardsFile = Parkour.getConfigManager().get("rewards");

    public static void loadRewards() {

        // load before so new config
        Parkour.getConfigManager().load("rewards");

        // get int keys
        for (String key : rewardsFile.getConfigurationSection("infinite").getKeys(false))
            // if it is an int and .command and .name is set, add it
            if (Utils.isInteger(key) &&
                rewardsFile.isSet("infinite." + key + ".command") &&
                rewardsFile.isSet("infinite." + key + ".name")) {

                int scoreNeeded = Integer.parseInt(key);
                String command = rewardsFile.getString("infinite." + key + ".command");
                String name = rewardsFile.getString("infinite." + key + ".name");

                // make new object and add
                Parkour.getInfiniteManager().addReward(new InfiniteReward(scoreNeeded, command, name));
            }
    }
}

