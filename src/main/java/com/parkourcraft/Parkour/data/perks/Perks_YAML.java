package com.parkourcraft.Parkour.data.perks;

import com.parkourcraft.Parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Perks_YAML {

    private static FileConfiguration perksConfig = Parkour.configs.get("perks");

    private static void commit(String perkName) {
        Parkour.configs.save("perks");
        Parkour.perks.load(perkName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(perksConfig.getKeys(false));
    }

    public static boolean exists(String perkName) {
        return perksConfig.isSet(perkName);
    }

    public static boolean isSet(String perkName, String valuePath) {
        return perksConfig.isSet(perkName + "." + valuePath);
    }

    public static String getTitle(String perkName) {
        if (isSet(perkName, "title"))
            return perksConfig.getString(perkName + ".title");

        return perkName;
    }

    public static List<String> getPermissions(String perkName) {
        if (isSet(perkName, "permissions"))
            return perksConfig.getStringList(perkName + ".permissions");

        return new ArrayList<>();
    }

    public static List<String> getRequirements(String perkName) {
        if (isSet(perkName, "requirements"))
            return perksConfig.getStringList(perkName + ".requirements");

        return new ArrayList<>();
    }

    public static List<String> getRequiredPermissions(String perkName) {
        if (isSet(perkName, "required_permissions"))
            return perksConfig.getStringList(perkName + ".required_permissions");

        return new ArrayList<>();
    }

    public static int getPrice(String perkName) {
        if (isSet(perkName, "price"))
            return perksConfig.getInt(perkName + ".price");

        return 0;
    }

    public static void create(String perkName) {
        if (!exists(perkName)) {
            perksConfig.set(perkName + ".permissions", new ArrayList<>());
            perksConfig.set(perkName + ".requirements", new ArrayList<>());

            commit(perkName);
        }
    }

    public static void setTitle(String perkName, String title) {
        if (exists(perkName))
            perksConfig.set(perkName + ".title", title);

        commit(perkName);
    }

    public static void setPermissions(String perkName, List<String> permissions) {
        if (exists(perkName))
            perksConfig.set(perkName + ".permissions", permissions);

        commit(perkName);
    }

    public static void setRequirements(String perkName, List<String> requirements) {
        if (exists(perkName))
            perksConfig.set(perkName + ".requirements", requirements);

        commit(perkName);
    }

    public static void setRequiredPermissions(String perkName, List<String> permissions) {
        if (exists(perkName))
            perksConfig.set(perkName + ".required_permissions", permissions);

        commit(perkName);
    }

    public static void setPrice(String perkName, int price) {
        if (exists(perkName))
            perksConfig.set(perkName + ".price", price);

        commit(perkName);
    }

}
