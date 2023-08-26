package com.renatusnetwork.parkour.data.infinite.rewards;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfiniteRewardsYAML {

    private static FileConfiguration rewardsFile = Parkour.getConfigManager().get("rewards");

    public static List<InfiniteReward> getRewards(InfiniteType type)
    {
        String typeString = type.toString().toLowerCase();
        ConfigurationSection section = rewardsFile.getConfigurationSection("infinite." + typeString);

        List<InfiniteReward> rewards = new ArrayList<>();

        // get int keys
        for (String key : section.getKeys(false))
            // if it is an int and .commands and .display is set, add it
            if (Utils.isInteger(key) && section.isSet(key + ".commands") && section.isSet(key + ".display"))
            {
                int scoreNeeded = Integer.parseInt(key);
                List<String> commands = section.getStringList(key + ".commands");
                String display = section.getString(key + ".display");

                // make new object and add
                rewards.add(new InfiniteReward(type, scoreNeeded, commands, display));
            }
        return rewards;
    }
}

