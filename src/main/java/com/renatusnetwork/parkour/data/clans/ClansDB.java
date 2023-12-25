package com.renatusnetwork.parkour.data.clans;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;

import java.util.*;

public class ClansDB {

    public static HashMap<String, Clan> loadClans()
    {
        HashMap<String, Clan> clans = new HashMap<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "clans",
                "*",
                ""
        );

        for (Map<String, String> result : results)
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

        Parkour.getPluginLogger().info("Clans Loaded: " + results.size());

        // load members
        loadMembers(clans);

        return clans;
    }

    private static void loadMembers(HashMap<String, Clan> clans)
    {
        List<Map<String, String>> memberResults = DatabaseQueries.getResults(
                "players", "uuid, name, clan",
                "WHERE clan IS NOT NULL");

        for (Map<String, String> memberResult : memberResults)
        {
            Clan clan = clans.get(memberResult.get("clan"));

            // add to clan if not null
            if (clan != null)
                clan.addMember(new ClanMember(memberResult.get("uuid"), memberResult.get("name")));
        }
    }

    public static void newClan(Clan clan)
    {
        insertClan(clan);

        PlayerStats owner = Parkour.getStatsManager().get(clan.getOwnerUUID());

        if (owner != null)
        {
            clan.addMember(new ClanMember(owner.getUUID(), owner.getPlayerName()));
            owner.setClan(clan);
            updatePlayerClan(owner);

            owner.sendMessage(Utils.translate("&7Successfully created your Clan &3" + clan.getTag()));
        }
    }

    public static void insertClan(Clan clan) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO clans (tag, owner_uuid) VALUES " +
                    "('" + clan.getTag() + "', " + clan.getOwnerUUID() + ")"
        );
    }

    public static void setClanXP(int clanXP, String tag) {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET " +
                "xp=? WHERE tag=?", clanXP, tag);
    }

    public static void setTotalXP(long totalXP, String tag) {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET " +
                "total_xp=? WHERE tag=?", totalXP, tag);
    }

    public static void setClanLevel(int clanLevel, String tag) {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET " +
                "level=? WHERE tag=?", clanLevel, tag);
    }

    public static void removeClan(String tag) {
        DatabaseQueries.runAsyncQuery("DELETE FROM clans WHERE tag=?", tag);
    }

    public static void resetClanMember(String playerName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE players SET clan=NULL WHERE name=?", playerName);
    }

    public static void updatePlayerClan(PlayerStats playerStats)
    {

        if (playerStats.inClan())
        {
            String clanTag = playerStats.getClan().getTag();
            String query = "UPDATE players SET clan=" + clanTag + " WHERE uuid=" + playerStats.getUUID();

            DatabaseQueries.runAsyncQuery(query);
        }
    }

    public static void updatePlayerClanID(String playerName, String tag)
    {
        DatabaseQueries.runAsyncQuery("UPDATE players SET clan=? WHERE name=?", tag, playerName);
    }

    public static void updateClanTag(Clan clan, String oldClanTag)
    {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET tag=? WHERE tag=?", clan.getTag(), oldClanTag);
    }

    public static void updateClanMaxLevel(Clan clan)
    {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET max_level=" + clan.getMaxLevel() + " WHERE tag='" + clan.getTag() + "'");
    }

    public static void updateClanMaxMembers(Clan clan)
    {
        DatabaseQueries.runAsyncQuery("UPDATE clans SET max_members=" + clan.getMaxMembers() + " WHERE tag='" + clan.getTag() + "'");
    }
}