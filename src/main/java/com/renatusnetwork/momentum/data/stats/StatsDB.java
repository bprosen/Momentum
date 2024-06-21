package com.renatusnetwork.momentum.data.stats;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.items.BankItemType;
import com.renatusnetwork.momentum.data.clans.Clan;
import com.renatusnetwork.momentum.data.clans.ClansManager;
import com.renatusnetwork.momentum.data.elo.ELOTiersManager;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.menus.LevelSortingType;
import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class StatsDB
{

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
            ClansManager clansManager = Momentum.getClansManager();

            String nameInDB = playerResult.get("name");
            Clan clan = clansManager.get(playerResult.get("clan"));

            // update player names
            if (!nameInDB.equals(playerStats.getName()))
            {
                updateName(playerStats.getUUID(), playerStats.getName());

                if (clan != null)
                    // update name in cache
                    Momentum.getClansManager().updatePlayerNameInClan(clan, nameInDB, playerStats.getName());

                // update in db
                Momentum.getPlotsManager().updatePlayerNameInPlot(nameInDB, playerStats.getName());
            }

            playerStats.setCoins(Integer.parseInt(playerResult.get("coins")));
            playerStats.setSpectatable(Integer.parseInt(playerResult.get("spectatable")) == 1);

            String eloTier = playerResult.get("elo_tier");
            ELOTiersManager eloTiersManager = Momentum.getELOTiersManager();

            // set tier or translate if null
            if (eloTier != null)
                playerStats.setELOTier(eloTiersManager.get(eloTier));
            else
                playerStats.setELOTier(eloTiersManager.get(Momentum.getSettingsManager().default_elo_tier));

            String elo = playerResult.get("elo");
            if (elo != null)
                playerStats.setELO(Integer.parseInt(elo));
            else
                playerStats.setELO(Momentum.getSettingsManager().default_elo);

            playerStats.loadELOToXPBar();

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
            Rank rank = Momentum.getRanksManager().get(rankName);
            if (rank != null)
                playerStats.setRank(rank);
            else
                playerStats.setRank(Momentum.getRanksManager().get(Momentum.getSettingsManager().default_rank));

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
                }.runTask(Momentum.getPlugin());

            // get total count of how many levels they've rated
            playerStats.setRatedLevelsCount(getRatedLevelsCount(playerStats));
            playerStats.setTotalLevelCompletions(getTotalCompletions(playerStats.getUUID()));
            playerStats.setPrestiges(Integer.parseInt(playerResult.get("prestiges")));

            // set multiplier percentage
            if (playerStats.hasPrestiges())
            {
                float defaultMultiplier = Momentum.getSettingsManager().prestige_multiplier_per_prestige;

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
            playerStats.setAttemptingMastery(Integer.parseInt(playerResult.get("attempting_mastery")) == 1);
            playerStats.setRaceWins(Integer.parseInt(playerResult.get("race_wins")));
            playerStats.setRaceLosses(Integer.parseInt(playerResult.get("race_losses")));
            playerStats.setAutoSave(Integer.parseInt(playerResult.get("auto_save")) == 1);

            // we do a math.max since we can't divide by 0... so if they have never lost we divide by 1 not zero
            playerStats.calcRaceWinRate();

            String sortLevelsType = playerResult.get("menu_sort_levels_type");

            // only set it if its non null
            if (sortLevelsType != null)
                playerStats.setLevelSortingType(LevelSortingType.valueOf(sortLevelsType));
            else
                // default is config based otherwise
                playerStats.setLevelSortingType(Momentum.getSettingsManager().default_level_sorting_type);

            String infiniteBlock = playerResult.get("infinite_block");

            // only set it if its non null
            if (infiniteBlock != null)
                playerStats.setInfiniteBlock(Material.matchMaterial(infiniteBlock));
            else
                // default is quartz block otherwise
                playerStats.setInfiniteBlock(Momentum.getSettingsManager().infinite_default_block);

            String infiniteType = playerResult.get("infinite_type");
            if (infiniteType != null)
                playerStats.setInfiniteType(InfiniteType.valueOf(infiniteType.toUpperCase()));
            else
                playerStats.setInfiniteType(Momentum.getSettingsManager().infinite_default_type);

            playerStats.setBankBids(getBankBids(playerStats, Momentum.getBankManager().getCurrentWeek()));
        }
    }

    public static int getRatedLevelsCount(PlayerStats playerStats)
    {

        Map<String, String> result = DatabaseQueries.getResult(
                        DatabaseManager.LEVEL_RATINGS_TABLE, "COUNT(*) AS count",
                        "WHERE uuid=?", playerStats.getUUID());

        return Integer.parseInt(result.get("count"));
    }

    public static int getTotalPlayers()
    {
        Map<String, String> result = DatabaseQueries.getResult(DatabaseManager.PLAYERS_TABLE, "COUNT(*) AS total", "");

        return Integer.parseInt(result.get("total"));
    }

    public static int getTotalCompletions(String uuid)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.LEVEL_COMPLETIONS_TABLE,
                "COUNT(*) AS total_completions",
                "WHERE uuid=?", uuid
        );

        return Integer.parseInt(playerResult.get("total_completions"));
    }

    // this will update the player name all across the database
    public static void updateName(String uuid, String name)
    {
        // update in stats
        DatabaseQueries.runAsyncQuery("UPDATE "  + DatabaseManager.PLAYERS_TABLE + " SET name=? WHERE uuid=?", name, uuid);
    }

    public static void updateEventWins(String uuid, int eventWins)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET event_wins=? WHERE uuid=?", eventWins, uuid);
    }

    public static void updateELO(String uuid, int elo)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET elo=? WHERE uuid=?", elo, uuid);
    }

    public static void updateMenuSortLevelsType(String uuid, LevelSortingType type)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET menu_sort_levels_type=? WHERE uuid=?", type.name(), uuid);
    }

    public static void updateSpectatable(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
            "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET spectatable=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateFailMode(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET fail_mode=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateRaceLosses(String uuid, int losses)
    {
        DatabaseQueries.runAsyncQuery("UPDATE players SET race_losses=? WHERE uuid=?", losses, uuid);
    }

    public static void updateRaceWins(String uuid, int wins)
    {
        DatabaseQueries.runAsyncQuery("UPDATE players SET race_wins=? WHERE uuid=?", wins, uuid);
    }

    public static void updateNightVision(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
            "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET night_vision=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateAutoSave(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET auto_save=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateGrinding(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET grinding=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateAttemptingRankup(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET attempting_rankup=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateAttemptingMastery(String uuid, boolean value)
    {
        int valueInt = value ? 1 : 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET attempting_mastery=? WHERE uuid=?", valueInt, uuid
        );
    }

    public static void updateCoins(String uuid, int coins, boolean async)
    {
        if (coins < 0)
            coins = 0;

        if (async)
            DatabaseQueries.runAsyncQuery(
                    "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=? WHERE uuid=?",
                    coins, uuid
            );
        else
            DatabaseQueries.runQuery(
                    "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=? WHERE uuid=?",
                    coins, uuid
            );
    }

    public static void updateCoinsName(String playerName, int coins)
    {
        if (coins < 0)
            coins = 0;

        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET coins=? WHERE name=?",
                    coins, playerName
        );
    }

    public static void updateInfiniteType(String uuid, InfiniteType newType)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_type=? WHERE uuid=?",
                    newType.toString().toLowerCase(), uuid
        );
    }

    public static void updateRank(String uuid, String rank)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET rank_name=? WHERE uuid=?", rank, uuid);
    }

    public static int getCoinsFromName(String playerName)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE name=?", playerName
        );

        return !playerResult.isEmpty() ? Integer.parseInt(playerResult.get("coins")) : 0;
    }

    public static int getCoinsFromUUID(String UUID)
    {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "coins",
                " WHERE uuid=?", UUID
        );

        return !playerResult.isEmpty() ? Integer.parseInt(playerResult.get("coins")) : 0;
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

    public static String getUUIDByName(String playerName) {
        Map<String, String> playerResult = DatabaseQueries.getResult(
                DatabaseManager.PLAYERS_TABLE,
                "uuid",
                " WHERE name=?", playerName
        );

        return playerResult.get("uuid");
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

    public static void loadFavoriteLevels(PlayerStats playerStats)
    {
        if (playerStats != null)
        {
            ArrayList<Level> favoriteLevels = new ArrayList<>();
            List<Map<String, String>> purchasesResults = DatabaseQueries.getResults(
                    DatabaseManager.FAVORITE_LEVELS,
                    "level_name",
                    "WHERE uuid=?", playerStats.getUUID()
            );

            for (Map<String, String> boughtResult : purchasesResults)
                favoriteLevels.add(Momentum.getLevelManager().get(boughtResult.get("level_name")));

            playerStats.setFavoriteLevels(favoriteLevels);
        }
    }

    public static void addFavoriteLevel(String uuid, String boughtLevel)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.FAVORITE_LEVELS + " (uuid, level_name)" +
                        " VALUES " +
                        "(?,?)",
                uuid, boughtLevel);
    }

    public static void removeFavoriteLevel(String uuid, String boughtLevel)
    {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.FAVORITE_LEVELS + " WHERE uuid=? AND level_name=?",
                uuid, boughtLevel);
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

    public static void updatePlayerClan(String uuid, String tag)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET clan=? WHERE uuid=?", tag, uuid
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

            modifiers.put(ModifierType.valueOf(playerResult.get("type").toUpperCase()), Momentum.getModifiersManager().getModifier(modifierName));
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
                "DELETE FROM " + DatabaseManager.PLAYER_MODIFIERS_TABLE + " WHERE uuid IN (SELECT uuid FROM " + DatabaseManager.PLAYERS_TABLE + " " +
                    "WHERE name=?) AND modifier_name=?",
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
                    Momentum.getPerkManager().get(perkResult.get("perk_name"))
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

    public static void resetInfiniteBlock(String uuid)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_block=NULL WHERE uuid=?", uuid);
    }

    public static void updateInfiniteScore(String uuid, InfiniteType type, int score)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET infinite_" + type.name().toLowerCase() + "_score=? WHERE uuid=?", score, uuid);
    }

    public static void updateELOTier(String uuid, String eloTier)
    {
        DatabaseQueries.runAsyncQuery("UPDATE " + DatabaseManager.PLAYERS_TABLE + " SET elo_tier=? WHERE uuid=?", eloTier, uuid);
    }

    public static HashMap<BankItemType, BankBid> getBankBids(PlayerStats playerStats, int week)
    {
        List<Map<String, String>> bankBidsResults = DatabaseQueries.getResults(
                DatabaseManager.BANK_BIDS,
                "*",
                "WHERE uuid=? AND week=?", playerStats.getUUID(), week
        );

        HashMap<BankItemType, BankBid> bids = new HashMap<>();

        for (Map<String, String> result : bankBidsResults)
        {
            bids.put(
                    BankItemType.valueOf(result.get("bank_item_type")),
                    new BankBid(
                            Integer.parseInt(result.get("total_bid")),
                            Long.parseLong(result.get("last_bid_date"))
                    )
            );
        }

        return bids;
    }

    public static void updateBankBid(String uuid, int week, BankItemType type, int totalBid, long lastBidDateMillis)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.BANK_BIDS + " SET total_bid=?,last_bid_date=? WHERE uuid=? AND week=? AND bank_item_type=?",
                totalBid, lastBidDateMillis, uuid, week, type.name());
    }

    public static void insertBankBid(String uuid, int week, BankItemType type, int totalBid, long lastBidDateMillis)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.BANK_BIDS + " (week,uuid,bank_item_type,total_bid,last_bid_date) VALUES(?,?,?,?,?)",
                week, uuid, type.name(), totalBid, lastBidDateMillis
        );
    }

    public static void loadObtainedCommandSigns(PlayerStats playerStats) {
        List<Map<String, String>> results = DatabaseQueries.getResults(
                DatabaseManager.COMMAND_SIGNS + " a",
                "a.*",
                "INNER JOIN " + DatabaseManager.OBTAINED_COMMAND_SIGNS + " b " +
                        "ON a.world = b.world AND a.x = b.x AND a.y = b.y AND a.z = b.z " +
                        "WHERE b.uuid = ?",
                playerStats.getUUID()
        );

        for (Map<String, String> result : results) {
            playerStats.obtainCommandSign(Bukkit.getWorld(result.get("world")), Double.parseDouble(result.get("x")), Double.parseDouble(result.get("y")), Double.parseDouble(result.get("z")));
        }
    }

    public static void insertCommandSign(String command, String world, double x, double y, double z) {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.COMMAND_SIGNS + " VALUES (?, ?, ?, ?)",
                command, world, x, y, z
        );
    }

    public static void deleteCommandSign(String world, double x, double y, double z) {
        DatabaseQueries.runAsyncQuery(
                "DELETE FROM " + DatabaseManager.COMMAND_SIGNS + " WHERE world = ? AND x = ? AND y = ? AND z = ?",
                world, x, y, z
        );
    }

    public static boolean hasCommandSign(String world, double x, double y, double z) {
        Map<String, String> result = DatabaseQueries.getResult(
                DatabaseManager.COMMAND_SIGNS,
                "*",
                "WHERE world = ? AND x = ? AND y = ? AND z = ?",
                world, x, y, z
        );

        return !result.isEmpty();
    }

    public static String getSignCommand(String world, double x, double y, double z) {
        Map<String, String> result = DatabaseQueries.getResult(
                DatabaseManager.COMMAND_SIGNS,
                "command",
                "WHERE world = ? AND x = ? AND y = ? AND z = ?",
                world, x, y, z
        );

        return result.get("command");
    }
}