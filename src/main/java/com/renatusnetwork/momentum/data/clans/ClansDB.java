package com.renatusnetwork.momentum.data.clans;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;

import java.util.*;

public class ClansDB {

    public static HashMap<String, Clan> getClans() {
        HashMap<String, Clan> clans = new HashMap<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.CLANS_TABLE,
                "*",
                ""
        );

        for (Map<String, String> result : results) {
            clans.put(result.get("tag"),
                      new Clan(
                              result.get("tag"),
                              result.get("owner_uuid"),
                              Integer.parseInt(result.get("level")),
                              Integer.parseInt(result.get("xp")),
                              Long.parseLong(result.get("total_xp")),
                              Integer.parseInt(result.get("max_level")),
                              Integer.parseInt(result.get("max_members"))
                      )
            );
        }

        Momentum.getPluginLogger().info("Clans Loaded: " + results.size());

        // load members
        loadMembers(clans);

        return clans;
    }

    private static void loadMembers(HashMap<String, Clan> clans) {
        List<Map<String, String>> memberResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYERS_TABLE, "uuid, name, clan",
                "WHERE clan IS NOT NULL");

        for (Map<String, String> memberResult : memberResults) {
            Clan clan = clans.get(memberResult.get("clan"));

            // add to clan if not null
            if (clan != null) {
                clan.addMember(new ClanMember(memberResult.get("uuid"), memberResult.get("name")));
            }
        }
    }

    public static void insert(Clan clan) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.CLANS_TABLE + " (tag, owner_uuid) VALUES " +
                "(?,?)",
                clan.getTag(), clan.getOwnerUUID()
        );
    }

    public static void updateXP(String tag, int clanXP) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET " +
                "xp=? WHERE tag=?", clanXP, tag);
    }

    public static void updateTotalXP(String tag, long totalXP) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET " +
                "total_xp=? WHERE tag=?", totalXP, tag);
    }

    public static void updateLevel(String tag, int clanLevel) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET " +
                "level=? WHERE tag=?", clanLevel, tag);
    }

    public static void remove(String tag) {
        DatabaseQueries.runAsyncQuery("DELETE FROM " + DatabaseManager.CLANS_TABLE + " WHERE tag=?", tag);
    }

    public static void updateTag(String oldTag, String newTag) {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.CLANS_TABLE + " SET tag=? WHERE tag=?", newTag, oldTag);
    }

    public static void updateMaxLevel(String tag, int newMax) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET max_level=? WHERE tag=?",
                newMax, tag
        );
    }

    public static void updateMaxMembers(String tag, int newMax) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET max_members=? WHERE tag=?",
                newMax, tag
        );
    }

    public static void updateOwner(String tag, String newOwnerUUID) {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.CLANS_TABLE + " SET owner_uuid=? WHERE tag=?", newOwnerUUID, tag
        );
    }
}