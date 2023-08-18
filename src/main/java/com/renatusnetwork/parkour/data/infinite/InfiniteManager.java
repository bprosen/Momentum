package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.InfiniteEndEvent;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.infinite.types.*;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfiniteManager {

    private HashMap<String, Infinite> participants = new HashMap<>();
    private HashMap<Integer, InfiniteLBPosition> leaderboard = new HashMap<>(Parkour.getSettingsManager().max_infinite_leaderboard_size);
    private LinkedHashMap<Integer, InfiniteReward> rewards = new LinkedHashMap<>(); // linked so order stays

    public InfiniteManager() {
        startScheduler();
    }

    public void startScheduler() {

        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
                InfiniteRewardsYAML.loadRewards();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public void startPK(PlayerStats playerStats, InfiniteType type, boolean fromPortal) {

        /*
            We force run this in sync for a few reasons:
            1. The packet listener executes this in async which cant be done as this creates blocks.
            2. Someone with bad ping can sometimes make this method run many times causing glitched blocks in the air
               if they have bad ping.
         */
        new BukkitRunnable() {
            @Override
            public void run() {

                // if type is null or from portal, prevent getting in via spectator
                if (type == null || playerStats.isInInfinite() || (fromPortal && playerStats.isSpectating()))
                    return;

                Player player = playerStats.getPlayer();

                Infinite infinite = null;
                // create cache
                switch (type)
                {
                    case CLASSIC:
                        infinite = new Classic(playerStats);
                        break;
                    case SPEEDRUN:
                        infinite = new Speedrun(playerStats);
                        break;
                    case TIMED:
                        infinite = new Timed(playerStats);
                        break;
                    case SPRINT:
                        infinite = new Sprint(playerStats);
                        break;
                }
                participants.put(player.getName(), infinite);

                // if they are at spawn prior to teleport, change original loc to setting
                if (fromPortal)
                    infinite.setOriginalLoc(Parkour.getLocationManager().get(Parkour.getSettingsManager().infinite_respawn_loc));

                infinite.start(); // begin!

            }
        }.runTask(Parkour.getPlugin());
    }

    public void endPK(Player player)
    {
        if (player != null)
        {
            Infinite infinite = get(player.getName());
            if (infinite != null)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                InfiniteEndEvent event = new InfiniteEndEvent(playerStats, infinite.getScore());
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    // run in sync because of packet listener running in async, need to remove blocks in sync
                    new BukkitRunnable() {
                        @Override
                        public void run()
                        {
                            // end and reward player
                            infinite.end();
                            rewardPlayer(playerStats, infinite, event.getReward());
                        }
                    }.runTask(Parkour.getPlugin());

                    participants.remove(player.getName());
                }
            }
        }
    }

    public void changeType(PlayerStats playerStats, InfiniteType newType)
    {
        playerStats.setInfiniteType(newType);
        StatsDB.updateInfiniteType(playerStats, newType);
    }

    private void rewardPlayer(PlayerStats playerStats, Infinite infinite, int coinReward)
    {
        if (infinite != null)
        {
            Player player = playerStats.getPlayer();
            int score = infinite.getScore();

            if (score > playerStats.getBestInfiniteScore())
            {
                // run in sync for safety
                for (InfiniteReward reward : getApplicableRewards(playerStats.getBestInfiniteScore(), score))
                {
                    // loop through and run commands of applicable rewards
                    player.sendMessage(Utils.translate(" &7You received &d" + reward.getName() + " &d(Score of " + Utils.formatNumber(reward.getScoreNeeded()) + ")"));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.getCommand().replace("%player%", player.getName()));
                }
            }

            if (playerStats.hasModifier(ModifierTypes.INFINITE_BOOSTER))
            {
                Booster booster = (Booster) playerStats.getModifier(ModifierTypes.INFINITE_BOOSTER);
                coinReward *= booster.getMultiplier();
            }

            String rewardString = Utils.getCoinFormat(score, coinReward);

            if (playerStats.getBestInfiniteScore() < score)
            {
                // if they are online
                if (player.isOnline())
                {
                    player.sendMessage(Utils.translate(
                            "&7You have beaten your previous record of &d" +
                                    Utils.formatNumber(playerStats.getBestInfiniteScore()) + " &7with &d" + Utils.formatNumber(score) + "\n" +
                                    "&7Awarded " + rewardString + " &eCoins"
                    ));
                }

                updateScore(player.getName(), score);

                // load leaderboard if they have a lb position
                if (scoreWillBeLB(score) || leaderboard.size() < Parkour.getSettingsManager().max_infinite_leaderboard_size)
                {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            loadLeaderboard();
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
            }
            else if (player.isOnline())
                player.sendMessage(Utils.translate(
                        "&7You failed at &d" + Utils.formatNumber(score) + " &5(Best is " + Utils.formatNumber(playerStats.getBestInfiniteScore()) + ")\n" +
                                "&7Awarded &6" + rewardString + " &eCoins"
                ));
            // deposit reward from listener (default = score)
            Parkour.getStatsManager().addCoins(playerStats, coinReward);
        }
    }

    public LinkedHashMap<Integer, InfiniteReward> getRewards() { return rewards; }

    public InfiniteReward getReward(int score) { return rewards.get(score); }

    public void addReward(InfiniteReward infiniteReward) { rewards.put(infiniteReward.getScoreNeeded(), infiniteReward); }

    public void clearRewards() { rewards.clear(); }

    public List<InfiniteReward> getApplicableRewards(int oldBestScore, int newBestScore) {
        List<InfiniteReward> tempRewards = new ArrayList<>();

        for (Map.Entry<Integer, InfiniteReward> entry : rewards.entrySet()) {
            int score = entry.getKey();
            // if score is greater than old and less than or = to new, then add
            if (score > oldBestScore && score <= newBestScore)
                tempRewards.add(entry.getValue());
        }
        return tempRewards;
    }

    public Infinite get(String playerName) {
        return participants.get(playerName);
    }

    public void remove(String playerName) {
        participants.remove(playerName);
    }

    // method to update their score in all 3 possible placed
    public void updateScore(String playerName, int score) {
        PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

        if (playerStats != null)
            playerStats.setInfiniteScore(score);

        if (isLBPosition(playerName))
            getLeaderboardPosition(playerName).setScore(score);

        Parkour.getDatabaseManager().run(
                "UPDATE players SET infinitepk_score=" + score + " WHERE player_name='" + playerName + "'"
        );
    }

    public Location generateNextBlockLocation(Location oldLocation, Player player) {

        LocationManager locationManager = Parkour.getLocationManager();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vector facing = player.getLocation().getDirection().setY(0);

        Location newLocation = oldLocation.clone().add(facing.multiply(random.nextInt(3, 6)));

        return newLocation;
    }

    public boolean isLocationEmpty(Location location)
    {
        boolean empty = true;

        for (Infinite infinite : participants.values())
            for (Block block : infinite.getBlocks())
            {
                Location blockLoc = block.getLocation();
                // if equal, it is not empty!
                if (blockLoc.getBlockX() == location.getBlockX() &&
                    blockLoc.getBlockY() == location.getBlockY() &&
                    blockLoc.getBlockZ() == location.getBlockZ())
                {
                    empty = false;
                    break;
                }
            }

        return empty;
    }

    public Location findStartingLocation()
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();
        LocationManager locationManager = Parkour.getLocationManager();
        Location middle = locationManager.get(Parkour.getSettingsManager().infinite_middle_loc);

        // make the box 6 less so it will never flip on the first one
        int minX = middle.getBlockX() - (settingsManager.max_infinite_x - 6);
        int maxX = middle.getBlockX() + (settingsManager.max_infinite_x - 6);
        int minZ = middle.getBlockZ() - (settingsManager.max_infinite_z - 6);
        int maxZ = middle.getBlockZ() + (settingsManager.max_infinite_z - 6);

        int foundX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int foundZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        Location foundLoc = new Location(
                middle.getWorld(), foundX, settingsManager.infinite_starting_y, foundZ
        );

        // check if the location will be in the way, if so, go again
        if (!isLocationEmpty(foundLoc))
            findStartingLocation();
        else
            return foundLoc;

        return null;
    }

    public void loadLeaderboard() {
        try {

            HashMap<Integer, InfiniteLBPosition> leaderboard = getLeaderboard();
            leaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    "players",
                    "uuid, player_name, infinitepk_score",
                    " WHERE infinitepk_score > 0" +
                            " ORDER BY infinitepk_score DESC" +
                            " LIMIT " + Parkour.getSettingsManager().max_infinite_leaderboard_size);

            int lbPos = 1;
            for (Map<String, String> scoreResult : scoreResults) {
                leaderboard.put(lbPos,
                        new InfiniteLBPosition(
                                scoreResult.get("uuid"),
                                scoreResult.get("player_name"),
                                Integer.parseInt(scoreResult.get("infinitepk_score")))
                );
                lbPos++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InfiniteLBPosition getLeaderboardPosition(String playerName) {
        for (InfiniteLBPosition infiniteLBPosition : leaderboard.values())
            if (infiniteLBPosition.getName().equalsIgnoreCase(playerName))
                return infiniteLBPosition;

        return null;
    }

    public boolean isLBPosition(String playerName) {
        for (InfiniteLBPosition infiniteLBPosition : leaderboard.values())
            if (infiniteLBPosition.getName().equalsIgnoreCase(playerName))
                return true;

        return false;
    }

    public boolean scoreWillBeLB(int score) {
        int lowestScore = 0;
        // gets lowest score
        for (InfiniteLBPosition infiniteLBPosition : leaderboard.values())
            if (lowestScore == 0 || infiniteLBPosition.getScore() < lowestScore)
                lowestScore = infiniteLBPosition.getScore();

        return lowestScore <= score;
    }

    public void shutdown() {
        for (Infinite infinitePK : participants.values())
            endPK(infinitePK.getPlayer());
    }

    public HashMap<Integer, InfiniteLBPosition> getLeaderboard() {
        return leaderboard;
    }

    public HashMap<String, Infinite> getParticipants() {
        return participants;
    }
}
