package com.parkourcraft.Parkour.data.clans;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.ClansManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClansData {

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
        DatabaseManager.runQuery(
                "INSERT INTO clans " +
                "(clan_tag, owner_player_id)" +
                " VALUES " +
                "('" +
                clan.getTag() + "', " +
                clan.getOwnerID() +
                ")"
        );
    }

}
