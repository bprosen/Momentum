package com.renatusnetwork.momentum.data.clans;

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.configuration.file.FileConfiguration;

public class ClansYAML {

    private static FileConfiguration clansConfig = Momentum.getConfigManager().get("clans");

    private static void commit() {
        Momentum.getConfigManager().save("ranks");
    }

    public static boolean isSection(String rankName) {
        if (clansConfig.isConfigurationSection(rankName))
            return true;
        return false;
    }

    public static boolean isSet(String rankName, String valuePath) {
        return clansConfig.isSet(rankName + "." + valuePath);
    }

    public static int getLevelUpPrice(Clan clan) {
        if (isSection("clans." + clan.getLevel()))
            return clansConfig.getInt("clans." + clan.getLevel() + ".xp-needed");
        return 0;
    }

    public static int getLevelUpPrice(int level) {
        if (isSection("clans." + level))
            return clansConfig.getInt("clans." + level + ".xp-needed");
        return 0;
    }

    public static int getMaxLevel() {
        for (int i = 1;; i++) {
            if (!isSection("clans." + i))
                return i;
        }
    }
}
