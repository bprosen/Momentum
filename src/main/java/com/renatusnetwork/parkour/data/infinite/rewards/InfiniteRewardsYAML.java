package com.renatusnetwork.parkour.data.infinite.rewards;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfiniteRewardsYAML {

    private static FileConfiguration rewardsFile = Parkour.getConfigManager().get("rewards");

    public static Set<InfiniteReward> getRewards(InfiniteType type)
    {
        // load before so new config
        Parkour.getConfigManager().load("rewards");

        String typeString = type.toString().toLowerCase();
        ConfigurationSection section = rewardsFile.getConfigurationSection("infinite." + typeString);

        Set<InfiniteReward> rewards = new HashSet<>();

        // get int keys
        for (String key : section.getKeys(false))
            // if it is an int and .command and .name is set, add it
            if (Utils.isInteger(key) &&
                rewardsFile.isSet("infinite." + key + ".command") &&
                rewardsFile.isSet("infinite." + key + ".name"))
            {

                int scoreNeeded = Integer.parseInt(key);
                List<String> commands = rewardsFile.getStringList("infinite." + key + ".commands");
                String display = rewardsFile.getString("infinite." + key + ".display");

                // make new object and add
                rewards.add(new InfiniteReward(scoreNeeded, commands, display));
            }
        return rewards;
    }
}

