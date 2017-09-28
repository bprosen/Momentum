package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.data.perks.Perks_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DataQueries;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PerkManager {

    private static List<Perk> perks = new ArrayList<>();

    public static Perk get(String perkName) {
        for (Perk perk : perks)
            if (perk.getName().equals(perkName))
                return perk;

        return null;
    }

    public static List<Perk> getPerks() {
        return perks;
    }

    public static boolean exists(String perkName) {
        if (get(perkName) != null)
            return true;

        return false;
    }

    public static void remove(String perkName) {
        for (Iterator<Perk> iterator = perks.iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(perkName))
                iterator.remove();
    }

    public static void create(String perkName) {
        if (!exists(perkName))
            Perks_YAML.create(perkName);
    }

    public static void load(String perkName) {
        boolean exists = exists(perkName);

        if (!Perks_YAML.exists(perkName)
                && exists)
            remove(perkName);
        else {
            if (exists)
                remove(perkName);

            perks.add(new Perk(perkName));
        }
    }

    public static void loadAll() {
        for (String perkName : Perks_YAML.getNames())
            load(perkName);
    }

    public static void syncPermissions(Player player) {
        PlayerStats playerStats = StatsManager.get(player);

        for (Perk perk : perks) {
            boolean hasRequirements = perk.hasRequirements(playerStats);

            for (String permission : perk.getPermissions())
                player.addAttachment(Parkour.getPlugin(), permission, hasRequirements);
        }
    }

    public static void syncPermissions() {
        for (Player player : Bukkit.getOnlinePlayers())
            syncPermissions(player);
    }

    public static void bought(PlayerStats playerStats, Perk perk) {
        if (perk.getID() > 0) {
            Long date = System.currentTimeMillis();

            playerStats.addPerk(perk.getName(), date);
            DataQueries.insertPerk(playerStats, perk, date);
        }
    }

}
