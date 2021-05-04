package com.parkourcraft.parkour.data.rank;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RanksYAML {

    private static FileConfiguration ranksConfig = Parkour.getConfigManager().get("ranks");

    private static void commit() {
        Parkour.getConfigManager().save("ranks");
    }

    public static List<String> getNames() {
        return new ArrayList<>(ranksConfig.getKeys(false));
    }

    public static boolean exists(String rankName) {
        return ranksConfig.isSet(rankName);
    }

    public static boolean isSection(String rankName) {
        if (ranksConfig.isConfigurationSection(rankName))
            return true;
        return false;
    }
    public static boolean isSet(String rankName, String valuePath) {
        return ranksConfig.isSet(rankName + "." + valuePath);
    }

    public static String getRankTitle(String rankName) {
        if (isSection(rankName)) {
            String rankTitle = ranksConfig.getString(rankName + ".title");
            return rankTitle;
        }
        return null;
    }

    public static int getRankId(String rankName) {
        if (isSection(rankName)) {
            int rankId = ranksConfig.getInt(rankName + ".id");
            return rankId;
        }
        return 0;
    }

    public static double getRankUpPrice(String rankName) {
        if (isSection(rankName)) {
            double rankId = ranksConfig.getDouble(rankName + ".rankup-price");
            return rankId;
        }
        return 0.0;
    }

    public static String getRankUpLevel(String rankName, String levelType) {
        if (isSet(rankName, levelType)) {
            String rankUpLevel = ranksConfig.getString(rankName + "." + levelType);
            return rankUpLevel;
        }
        return null;
    }

    public static void create(String rankName) {
        if (!exists(rankName)) {
            ranksConfig.set(rankName + ".title", rankName);
            commit();
        }
    }

    public static void setRankTitle(String rankName, String rankTitle) {
        if (exists(rankName)) {
            ranksConfig.set(rankName + ".title", rankTitle);
            commit();
        }
    }

    public static void setRankID(String rankName, int rankId) {
        if (exists(rankName)) {
            ranksConfig.set(rankName + ".id", rankId);
            commit();
        }
    }

    public static void setRankUpPrice(String rankName, double rankUpPrice) {
        if (exists(rankName)) {
            ranksConfig.set(rankName + ".rankup-price", rankUpPrice);
            commit();
        }
    }

    public static boolean isSingleLevelRankup(String rankName) {
        if (isSet(rankName, "single-level-rankup"))
            return ranksConfig.getBoolean(rankName + ".single-level-rankup");
        return false;
    }

    public static void remove(String rankName) {
        if (isSection(rankName)) {
            ranksConfig.set(rankName, null);
            commit();
        }
    }
}
