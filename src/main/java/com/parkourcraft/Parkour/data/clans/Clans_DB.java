package com.parkourcraft.Parkour.data.clans;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Clans_DB {

    public static void loadClans(List<Clan> clans) {
        clans = new ArrayList<>();

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "clans",
                "clan_id, clan_tag, owner_player_id",
                ""
        );

        for (Map<String, String> result : results)
            clans.add(
                    new Clan(
                        Integer.parseInt(result.get("clan_id")),
                            result.get("clan_tag"),
                        Integer.parseInt(result.get("owner_player_id"))
                    )
            );

        Parkour.getPluginLogger().info("Clans Loaded: " + results.size());
    }

    public static void loadMembers(ClansManager clansManager) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "players",
                "player_id, uuid, player_name, clan_id",
                "WHERE clan_id > 0"
        );

        for (Map<String, String> result : results) {
            Clan clan = clansManager.get(Integer.parseInt(result.get("clan_id")));

            if (clan != null)
                clan.addMember(new ClanMember(
                        Integer.parseInt(result.get("player_id")),
                        result.get("uuid"),
                        result.get("player_name")
                ));
        }

        Parkour.getPluginLogger().info("Clan Members Loaded: " + results.size());
    }

    public static void newClan(Clan clan) {
        insertClan(clan);

        List<Map<String, String>> results = DatabaseQueries.getResults(
                "clans",
                "clan_id",
                "WHERE clan_tag='" + clan.getTag() + "'"
        );

        for (Map<String, String> result : results)
            clan.setID(Integer.parseInt(result.get("clan_id")));
    }

    private static void insertClan(Clan clan) {
        Parkour.database.run(
                "INSERT INTO clans " +
                "(clan_tag, owner_player_id)" +
                " VALUES " +
                "('" +
                clan.getTag() + "', " +
                clan.getOwnerID() +
                ")"
        );
    }

    public static void updatePlayerClanID(PlayerStats playerStats) {
        String query = "UPDATE players SET " +
                "clan_id=" + playerStats.getClanID() +
                " WHERE player_id=" + playerStats.getPlayerID()
                ;

        Parkour.database.add(query);
    }

    public static void updateClanTag(Clan clan) {
        String query = "UPDATE clans SET " +
                "clan_tag='" + clan.getTag() + "' " +
                "WHERE clan_id=" + clan.getID()
                ;

        Parkour.database.add(query);
    }

    public static void updateClanOwnerID(Clan clan) {
        String query = "UPDATE clans SET " +
                "owner_player_id=" + clan.getOwnerID() +
                "WHERE clan_id=" + clan.getID()
                ;

        Parkour.database.add(query);
    }


}
