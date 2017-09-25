package com.parkourcraft.Parkour.data.rewards;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.ChatColor;

import java.util.List;

public class RewardObject {

    private String name;
    private String title;
    private List<String> permissions;
    private List<String> requirements;
    private int price;

    public RewardObject(String rewardName) {
        name = rewardName;

        load();
    }

    private void load() {
        if (Rewards_YAML.exists(name)) {
            title = Rewards_YAML.getTitle(name);
            permissions = Rewards_YAML.getPermissions(name);
            requirements = Rewards_YAML.getRequirements(name);
            price = Rewards_YAML.getPrice(name);
        }
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public int getPrice() {
        return price;
    }

    public boolean hasRequirements(PlayerStats playerStats) {
        for (String levelRequirement : requirements)
            if (playerStats.getLevelCompletionsCount(levelRequirement) < 1)
                return false;

        return true;
    }

}
