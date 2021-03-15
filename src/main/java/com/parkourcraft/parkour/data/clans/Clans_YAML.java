package com.parkourcraft.parkour.data.clans;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

public class Clans_YAML {

    private static FileConfiguration clansConfig = Parkour.getConfigManager().get("clans");

    private static void commit() {
        Parkour.getConfigManager().save("ranks");
    }

    public static boolean isSection(String rankName) {
        if (clansConfig.isConfigurationSection(rankName))
            return true;
        return false;
    }

    public static boolean isSet(String rankName, String valuePath) {
        return clansConfig.isSet(rankName + "." + valuePath);
    }

    public static long getLevelUpPrice(Clan clan) {
        if (isSection("clans." + clan.getLevel()))
            return clansConfig.getLong("clans." + clan.getLevel() + ".xp-needed");
        return 0;
    }
}
