package com.parkourcraft.Parkour.data.rewards;

import com.parkourcraft.Parkour.data.RewardManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Rewards_YAML {

    private static FileConfiguration rewardsConfig = FileManager.getFileConfig("rewards");

    private static void commit(String rewardName) {
        FileManager.save("levels");
        RewardManager.load(rewardName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(rewardsConfig.getKeys(false));
    }

    public static boolean exists(String rewardName) {
        return rewardsConfig.isSet(rewardName);
    }

    public static boolean isSet(String rewardName, String valuePath) {
        return rewardsConfig.isSet(rewardName + "." + valuePath);
    }

    public static String getTitle(String rewardName) {
        if (isSet(rewardName, "title"))
            return rewardsConfig.getString(rewardName + ".title");

        return rewardName;
    }

    public static List<String> getPermissions(String rewardName) {
        if (isSet(rewardName, "permissions"))
            return rewardsConfig.getStringList(rewardName + ".permissions");

        return new ArrayList<>();
    }

    public static List<String> getRequirements(String rewardName) {
        if (isSet(rewardName, "requirements"))
            return rewardsConfig.getStringList(rewardName + ".requirements");

        return new ArrayList<>();
    }

    public static int getPrice(String rewardName) {
        if (isSet(rewardName, "price"))
            return rewardsConfig.getInt(rewardName + ".price");

        return 0;
    }

    public static void create(String rewardName) {
        if (!exists(rewardName)) {
            rewardsConfig.set(rewardName + ".permissions", new ArrayList<>());
            rewardsConfig.set(rewardName + ".requirements", new ArrayList<>());

            commit(rewardName);
        }
    }

    public static void setTitle(String rewardName, String title) {
        if (exists(rewardName))
            rewardsConfig.set(rewardName + ".title", title);

        commit(rewardName);
    }

    public static void setPermissions(String rewardName, List<String> permissions) {
        if (exists(rewardName))
            rewardsConfig.set(rewardName + ".permissions", permissions);

        commit(rewardName);
    }

    public static void setRequirements(String rewardName, List<String> requirements) {
        if (exists(rewardName))
            rewardsConfig.set(rewardName + ".requirements", requirements);

        commit(rewardName);
    }

    public static void setPrice(String rewardName, int price) {
        if (exists(rewardName))
            rewardsConfig.set(rewardName + ".requirements", price);

        commit(rewardName);
    }

}
