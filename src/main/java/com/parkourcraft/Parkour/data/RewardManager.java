package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.rewards.RewardObject;
import com.parkourcraft.Parkour.data.rewards.Rewards_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RewardManager {

    private static List<RewardObject> rewards = new ArrayList<>();

    public static RewardObject get(String rewardName) {
        for (RewardObject rewardObject : rewards)
            if (rewardObject.getName().equals(rewardName))
                return rewardObject;

        return null;
    }

    public static boolean exists(String rewardName) {
        if (get(rewardName) != null)
            return true;

        return false;
    }

    public static void remove(String rewardName) {
        for (Iterator<RewardObject> iterator = rewards.iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(rewardName))
                iterator.remove();
    }

    public static void create(String rewardName) {
        if (!exists(rewardName))
            Rewards_YAML.create(rewardName);
    }

    public static void load(String rewardName) {
        if (!Rewards_YAML.exists(rewardName)
                && exists(rewardName))
            remove(rewardName);
        else
            rewards.add(new RewardObject(rewardName));
    }

    public static void loadAll() {
        for (String rewardName : Rewards_YAML.getNames())
            load(rewardName);
    }

    public static void syncPermissions(Player player) {
        PlayerStats playerStats = StatsManager.get(player);

        for (RewardObject rewardObject : rewards) {
            boolean hasRequirements = rewardObject.hasRequirements(playerStats);

            for (String permission : rewardObject.getPermissions())
                player.addAttachment(Parkour.getPlugin(), permission, hasRequirements);
        }
    }

}
