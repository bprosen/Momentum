package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.clans.ClansManager;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class StatsDB {

    /*
     * Player Stats Section
     */
    public static void loadPlayerInformation(PlayerStats playerStats)
    {

        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "*",
                "WHERE uuid=?", playerStats.getUUID()
        );

        if (playerResult.isEmpty())
        {
            DatabaseQueries.runQuery(
                    "INSERT INTO " + DatabaseManager.PLAYERS_TABLE + " (uuid, name) VALUES (?,?)",
                    playerStats.getUUID(), playerStats.getName());
        }
        else
        {
            ClansManager clansManager = Parkour.getClansManager();

            String nameInDB = playerResult.get("name");
            Clan clan = clansManager.get(playerResult.get("clan"));

            // update player names
            if (!nameInDB.equals(playerStats.getName()))
            {
                updatePlayerName(playerStats);

                if (clan != null)
                    // update name in cache
                    Parkour.getClansManager().updatePlayerNameInClan(clan, nameInDB, playerStats.getName());

                // update in db
                Parkour.getPlotsManager().updatePlayerNameInPlot(nameInDB, playerStats.getName());
            }

            playerStats.setCoins(Double.parseDouble(playerResult.get("coins")));
            playerStats.setSpectatable(Integer.parseInt(playerResult.get("spectatable")) == 1);

            if (clan != null)
            {
                playerStats.setClan(clan);

                // notify members of joining
                String memberRole = "Member";
                if (clan.isOwner(playerStats.getName()))
                    memberRole = "Owner";

                // Bukkit#getPlayer() is async safe :)
                clansManager.sendMessageToMembers(playerStats.getClan(),
                        "&eClan " + memberRole + " &6" + playerStats.getName() + " &7joined",
                        playerStats.getName());
            }

            String rankName = playerResult.get("rank_name");
            Rank rank = Parkour.getRanksManager().get(rankName);
            if (rank != null)
                playerStats.setRank(rank);
            else
                playerStats.setRank(Parkour.getRanksManager().get(Parkour.getSettingsManager().default_rank));

            for (InfiniteType type : InfiniteType.values())
            {
                String typeString = "infinite_" + type.toString().toLowerCase() + "_score";
                String scoreString = playerResult.get(typeString);

                // set score
                if (scoreString != null)
                    playerStats.setInfiniteScore(type, Integer.parseInt(scoreString));
            }

            playerStats.setNightVision(Integer.parseInt(playerResult.get("night_vision")) == 1);

            // need to give them night vision
            if (playerStats.hasNightVision())
                // run sync potion add
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerStats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
                    }
                }.runTask(Parkour.getPlugin());

            // get total count of how many levels they've rated
            playerStats.setRatedLevelsCount(getRatedLevelsCount(playerStats));

            // set multiplier percentage
            if (playerStats.hasPrestiges())
            {
                float defaultMultiplier = Parkour.getSettingsManager().prestige_multiplier_per_prestige;

                float prestigeMultiplier = defaultMultiplier * playerStats.getPrestiges();

                if (prestigeMultiplier >= defaultMultiplier)
                    prestigeMultiplier = defaultMultiplier;

                prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                playerStats.setPrestigeMultiplier(prestigeMultiplier);
            }

            // lots of settings here
            playerStats.setGrinding(Integer.parseInt(playerResult.get("grinding")) == 1);
            playerStats.setEventWins(Integer.parseInt(playerResult.get("event_wins")));
            playerStats.setFailMode(Integer.parseInt(playerResult.get("fail_mode")) == 1);
            playerStats.setAttemptingRankup(Integer.parseInt(playerResult.get("attempting_rankup")) == 1);
            playerStats.setRaceWins(Integer.parseInt(playerResult.get("race_wins")));
            playerStats.setRaceLosses(Integer.parseInt(playerResult.get("race_losses")));
            playerStats.setPrestiges(Integer.parseInt(playerResult.get("prestiges")));

            // we do a math.max since we can't divide by 0... so if they have never lost we divide by 1 not zero
            playerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal(
                    (double) playerStats.getRaceWins() / Math.max(1, playerStats.getRaceLosses())
            )));

            String infiniteBlock = playerResult.get("infinite_block");

            // only set it if its non null
            if (infiniteBlock != null)
                playerStats.setInfiniteBlock(Material.matchMaterial(infiniteBlock));
            else
                // default is quartz block otherwise
                playerStats.setInfiniteBlock(Parkour.getSettingsManager().infinite_default_block);

            String infiniteType = playerResult.get("infinite_type");
            if (infiniteType != null)
                playerStats.setInfiniteType(InfiniteType.valueOf(infiniteType.toUpperCase()));
            else
                playerStats.setInfiniteType(Parkour.getSettingsManager().infinite_default_type);
        }
    }

    public static int getRatedLevelsCount(PlayerStats playerStats)
    {

        Map<String, String> result = DatabaseQueries.getResult(
                        DatabaseManager.LEVEL_RATINGS_TABLE, "COUNT(*) AS count",
                        "WHERE uuid=?", playerStats.getUUID());

        if (!result.isEmpty())
            return Integer.parseInt(result.get("count"));
        else
            return 0;
    }

    public static int getTotalPlayers()
    {
        List<Map<String, String>> results = DatabaseQueries.getResults(DatabaseManager.PLAYERS_TABLE, "COUNT(*) AS total", "");

        return Integer.parseInt(results.get(0).get("total"));
    }

    // this will update the player name all across the database
    private static void updatePlayerName(PlayerStats playerStats) {
        // update in stats
        DatabaseQueries.runAsyncQuery("UPDATE "  + DatabaseManager.PLAYERS_TABLE + " SET " +
                "name='" + playerStats.getName() + "' WHERE uuid=" + playerStats.getUUID());
    }

    public static void updatePlayerSpectatable(PlayerStats playerStats)
    {
        int spectatable = playerStats.isSpectatable() ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
            "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                "spectatable=? WHERE uuid=?", spectatable, playerStats.getUUID()
        );
    }

    public static void updatePlayerNightVision(PlayerStats playerStats)
    {
        int vision = playerStats.hasNightVision() ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
            "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                "night_vision=? WHERE uuid=?", vision, playerStats.getUUID()
        );
    }

    public static void updatePlayerGrinding(PlayerStats playerStats)
    {
        int grinding = playerStats.isGrinding() ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET " +
                    "grinding=? WHERE uuid=?", grinding, playerStats.getUUID()
        );
    }

    public static void updateCoins(String uuid, double coins)
    {
        if (coins < 0)
            coins = 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=? WHERE uuid=?",
                coins, uuid
        );
    }

    public static void updateCoinsName(String playerName, double coins)
    {
        if (coins < 0)
            coins = 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=? WHERE name=?",
                    coins, playerName
        );
    }

    public static void updateInfiniteType(PlayerStats playerStats, InfiniteType newType)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_type=? WHERE uuid=?",
                    newType.toString().toLowerCase(), playerStats.getUUID()
        );
    }

    public static void updateRank(String uuid, String rank)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET rank_name=? WHERE uuid=?", rank, uuid);
    }

    public static double getCoinsFromName(String playerName)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE name=?", playerName
        );

        if (!playerResult.isEmpty())
            return Double.parseDouble(playerResult.get("coins"));
        else
            return 0.0;
    }

    public static double getCoinsFromUUID(String UUID)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE uuid=?", UUID
        );

        if (!playerResult.isEmpty())
            return Double.parseDouble(playerResult.get("coins"));
        else
            return 0.0;
    }

    public static boolean isPlayerInDatabase(String playerName)
    {

        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "uuid",
                " WHERE name=?", playerName
        );

        return !playerResult.isEmpty();
    }

    public static void loadBoughtLevels(PlayerStats playerStats)
    {
        if (playerStats != null)
        {
            HashSet<String> boughtLevels = new HashSet<>();

            List<Map<String, String>> purchasesResults = DatabaseQueries.getResults(
                    DatabaseManager.LEVEL_PURCHASES_TABLE,
                    "level_name",
                    "WHERE uuid=?", playerStats.getUUID()
            );

            for (Map<String, String> boughtResult : purchasesResults)
                boughtLevels.add(boughtResult.get("level_name"));

            playerStats.setBoughtLevels(boughtLevels);
        }
    }

    public static void addBoughtLevel(String uuid, String boughtLevel)
    {
        DatabaseQueries.runAsyncQuery(
            "INSERT INTO " + DatabaseManager.LEVEL_PURCHASES_TABLE + " (uuid, level_name)" +
                " VALUES " +
                "(?,?)",
                uuid, boughtLevel);
    }

    public static void removeBoughtLevel(String uuid, String boughtLevel)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_PURCHASES_TABLE + " WHERE uuid=? AND level_name=?",
                uuid, boughtLevel);
    }

    public static void removeBoughtLevelByName(String playerName, String boughtLevel)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.LEVEL_PURCHASES_TABLE +
                    " lp JOIN " + DatabaseManager.PLAYERS_TABLE +
                    " p ON p.uuid=lp.uuid WHERE p.name=? AND lp.level_name=?",
                    playerName, boughtLevel);
    }

    public static boolean hasBoughtLevel(String uuid, String boughtLevel)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.LEVEL_PURCHASES_TABLE,
                "uuid",
                " WHERE uuid=? AND level_name=?", uuid, boughtLevel
        );

        return !playerResult.isEmpty();
    }

    public static void resetPlayerClan(String playerName)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET clan=NULL WHERE name=?", playerName);
    }

    public static void updatePlayerClan(PlayerStats playerStats, String tag)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET clan=? WHERE uuid=?", tag, playerStats.getUUID()
        );
    }

    public static void loadModifiers(PlayerStats playerStats)
    {
        HashMap<ModifierType, Modifier> modifiers = new HashMap<>();

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                DatabaseManager.PLAYER_MODIFIERS_TABLE + " pm",
                "m.type AS type, pm.modifier_name AS modifier_name",
                "JOIN " + DatabaseManager.MODIFIERS_TABLE + " m ON m.name=pm.modifier_name WHERE uuid=?",
                playerStats.getUUID()
        );

        for (Map<String, String> playerResult : playerResults)
        {
            String modifierName = playerResult.get("modifier_name");

            modifiers.put(ModifierType.valueOf(playerResult.get("type").toUpperCase()), Parkour.getModifiersManager().getModifier(modifierName));
        }

        playerStats.setModifiers(modifiers);
    }

    public static void addModifier(String uuid, String modifierName)
    {
        DatabaseQueries.runAsyncQuery(
            "INSERT INTO " + DatabaseManager.PLAYER_MODIFIERS_TABLE + " (uuid, modifier_name) VALUES(?,?)",
                uuid, modifierName
        );
    }

    public static void removeModifier(String uuid, String modifierName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PLAYER_MODIFIERS_TABLE + " WHERE uuid=? AND modifier_name=?",
                    uuid, modifierName
        );
    }

    public static void removeModifierName(String playerName, String modifierName)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.PLAYER_MODIFIERS_TABLE + " pm JOIN " + DatabaseManager.PLAYERS_TABLE + " " +
                    "p ON p.uuid=pm.uuid WHERE p.name=? AND modifier_name=?",
                    playerName, modifierName
        );
    }

    public static void loadBoughtPerks(PlayerStats playerStats)
    {
        List<Map<String, String>> perksResults = DatabaseQueries.getResults(
                DatabaseManager.PERKS_BOUGHT_TABLE,
                "perk_name",
                "WHERE uuid=?", playerStats.getUUID());

        for (Map<String, String> perkResult : perksResults)
            playerStats.addPerk(
                    Parkour.getPerkManager().get(perkResult.get("perk_name"))
            );
    }

    public static void addBoughtPerk(String playerUUID, String perkName)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.PERKS_BOUGHT_TABLE + " (uuid, perk_name) VALUES (?,?)", playerUUID, perkName
        );
    }

    public static void updateInfiniteBlock(String uuid, String material)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_block=? WHERE uuid=?", material, uuid);
    }
}