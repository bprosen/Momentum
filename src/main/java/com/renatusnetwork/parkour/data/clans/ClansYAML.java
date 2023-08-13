package com.renatusnetwork.parkour.data.clans;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

public class ClansYAML {

    private static FileConfiguration clansConfig = Parkour.getConfigManager().get("clans");

    private static void commit() {
        Parkour.getConfigManager().save("clans");
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
}
