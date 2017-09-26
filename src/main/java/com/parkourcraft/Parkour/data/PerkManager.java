package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.data.perks.Perks_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;
import org.bukkit.entity.Player;

import java.util.*;

public class PerkManager {

    private static List<Perk> perks = new ArrayList<>();
    private static Map<String, Integer> perkIDCache = new HashMap<>();

    public static Perk get(String perkName) {
        for (Perk perk : perks)
            if (perk.getName().equals(perkName))
                return perk;

        return null;
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

        syncIDCache();
    }

    public static void syncPermissions(Player player) {
        PlayerStats playerStats = StatsManager.get(player);

        for (Perk perk : perks) {
            boolean hasRequirements = perk.hasRequirements(playerStats);

            for (String permission : perk.getPermissions())
                player.addAttachment(Parkour.getPlugin(), permission, hasRequirements);
        }
    }

    public static void bought(PlayerStats playerStats, Perk perk) {
        if (perk.getID() > 0) {
            Long date = System.currentTimeMillis();

            playerStats.addPerk(perk.getID(), date);
            DatabaseManager.addUpdateQuery(
                    "INSERT INTO ledger (player_id, perk_id, date)"
                    + " VALUES "
                    + "(" + playerStats.getPlayerID()
                    + ", " + perk.getID()
                    + ", FROM_UNIXTIME(" + (date / 1000) + "))"
            );
        }
    }

    public static void loadIDs() {
        List<Map<String, String>> perkResults = DatabaseQueries.getResults(
                "perks",
                "*",
                ""
        );

        if (perkResults.size() > 0) {
            perkIDCache = new HashMap<>();

            for (Map<String, String> perkResult : perkResults) {
                perkIDCache.put(
                        perkResult.get("perk_name"),
                        Integer.parseInt(perkResult.get("perk_id"))
                );
            }
        }
    }

    private static void syncIDCache() {
        for (String perkName : perkIDCache.keySet()) {
            Perk perk = get(perkName);

            if (perk != null)
                perk.setID(perkIDCache.get(perkName));
        }
    }

    public static void syncIDs() {
        syncIDCache();

        List<String> insertQueries = new ArrayList<>();

        for (Perk perk : perks)
            if (perk.getID() == -1) {
                String query = "INSERT INTO perks " +
                        "(perk_name)" +
                        " VALUES " +
                        "('" + perk.getName() + "')";

                insertQueries.add(query);
            }


        if (insertQueries.size() > 0) {
            String finalQuery = "";
            for (String sql : insertQueries)
                finalQuery = finalQuery + sql + "; ";

            DatabaseManager.runQuery(finalQuery);
            loadIDs();
            syncIDs();
        }
    }

}
