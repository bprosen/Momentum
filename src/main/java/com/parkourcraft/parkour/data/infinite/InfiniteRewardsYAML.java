package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

public class InfiniteRewardsYAML {

    private static FileConfiguration rewardsFile = Parkour.getConfigManager().get("rewards");

    public static void loadRewards() {

        // get int keys
        for (String key : rewardsFile.getConfigurationSection("infinitepk").getKeys(false))
            // if it is an int and .command and .name is set, add it
            if (Utils.isInteger(key) &&
                rewardsFile.isSet("infinitepk." + key + ".command") &&
                rewardsFile.isSet("infinitepk." + key + ".name")) {

                int scoreNeeded = Integer.parseInt(key);
                String command = rewardsFile.getString("infinitepk." + key + ".command");
                String name = rewardsFile.getString("infinitepk." + key + ".name");

                // make new object and add
                Parkour.getInfinitePKManager().addReward(new InfinitePKReward(scoreNeeded, command, name));
            }
    }
}

