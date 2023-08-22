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
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfiniteManager {

    private HashMap<String, Infinite> participants = new HashMap<>();
    private HashMap<InfiniteType, InfiniteLB> leaderboards = new HashMap<>(Parkour.getSettingsManager().max_infinite_leaderboard_size);
    private LinkedHashMap<Integer, InfiniteReward> rewards = new LinkedHashMap<>(); // linked so order stays

    public InfiniteManager()
    {
        startScheduler();
        initLeaderboards();
    }

    public void initLeaderboards()
    {
        leaderboards.put(InfiniteType.CLASSIC, new InfiniteLB(InfiniteType.CLASSIC));
        leaderboards.put(InfiniteType.SPEEDRUN, new InfiniteLB(InfiniteType.SPEEDRUN));
        leaderboards.put(InfiniteType.SPRINT, new InfiniteLB(InfiniteType.SPRINT));
        leaderboards.put(InfiniteType.TIMED, new InfiniteLB(InfiniteType.TIMED));
    }

    public void startScheduler() {

        new BukkitRunnable() {
            @Override
            public void run() {
                InfiniteRewardsYAML.loadRewards();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());

        // update leaderboards every 3 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboards();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 5, 20 * 180);
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
                    // end and reward player
                    infinite.end();
                    rewardPlayer(playerStats, infinite, event.getReward());

                    // load level info from region
                    Parkour.getLevelManager().regionLevelCheck(playerStats, player.getLocation());
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

                updateScore(player.getName(), playerStats.getInfiniteType(), score);
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
    public void updateScore(String playerName, InfiniteType type, int score)
    {
        PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

        if (playerStats != null)
            playerStats.setInfiniteScore(type, score);

        Parkour.getDatabaseManager().add(
                "UPDATE players SET infinite_" + type.toString().toLowerCase() + "_score=" + score + " WHERE player_name='" + playerName + "'"
        );
    }

    public Location generateNextBlockLocation(Location oldLocation, Player player)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        Vector playerDirection = player.getLocation().getDirection().setY(0).normalize(); // ignore y

        float min = settingsManager.infinite_distance_min;
        float bound = settingsManager.infinite_distance_bound;

        // need to be + 1 so it accounts for it
        float newY = (float) oldLocation.getY() + random.nextInt(settingsManager.infinite_generation_y_min, settingsManager.infinite_generation_y_max + 1);

        // adjust the values if they are beyond the limits
        if (newY > settingsManager.max_infinite_y)
            newY--;
        else if (newY < settingsManager.min_infinite_y)
            newY++;

        float minModifier = 1.0f;
        float maxModifier = 1.0f;
        float diff = 0.0f;
        boolean notZero = false;

        // if it is a +1
        if (newY > oldLocation.getY())
        {
            minModifier = settingsManager.infinite_generation_positive_y_min;
            maxModifier = settingsManager.infinite_generation_positive_y_max;
            diff = settingsManager.infinite_generation_positive_y_diff;
            notZero = true;
        }
        // if it is a -1
        else if (newY < oldLocation.getY())
        {
            minModifier = settingsManager.infinite_generation_negative_y_min;
            maxModifier = settingsManager.infinite_generation_negative_y_max;
            diff = settingsManager.infinite_generation_negative_y_diff;
            notZero = true;
        }

        min *= minModifier;
        bound *= maxModifier;

        // if it was modified and either the difference is less than th e min or the bound is <= min
        if (notZero && (bound <= min || Math.abs(bound - min) < diff))
            bound = min + diff;

        // get random distance modifier
        double distance = random.nextDouble(min, bound);

        // get random angle
        float angleBound = settingsManager.infinite_angle_bound;
        double randomAngle = random.nextDouble(-Math.PI / angleBound, Math.PI / angleBound); // Adjust the range as needed

        Location middle = Parkour.getLocationManager().get(Parkour.getSettingsManager().infinite_middle_loc);
        int radiusX = settingsManager.infinite_soft_border_radius_x;
        int radiusZ = settingsManager.infinite_soft_border_radius_z;

        // they are at the soft border!
        if (Math.abs(oldLocation.getX()) > (Math.abs(middle.getBlockX()) + radiusX) || Math.abs(oldLocation.getZ()) > (Math.abs(middle.getBlockZ()) + radiusZ))
        {
            float minAngle = settingsManager.infinite_soft_border_angle_min;
            float maxAngle = settingsManager.infinite_soft_border_angle_max;

            // get random angle based on the min/max
            if (random.nextBoolean())
                randomAngle = random.nextDouble(Math.PI / minAngle, Math.PI / maxAngle); // Adjust the range as needed
            else
                randomAngle = random.nextDouble(-Math.PI / maxAngle, -Math.PI / minAngle); // Adjust the range as needed, need to reverse by how division works
        }

        // rotate the vector by the random angle
        double rotatedX = playerDirection.getX() * Math.cos(randomAngle) - playerDirection.getZ() * Math.sin(randomAngle);
        double rotatedZ = playerDirection.getX() * Math.sin(randomAngle) + playerDirection.getZ() * Math.cos(randomAngle);

        double newX = oldLocation.getX() + (distance * rotatedX);
        double newZ = oldLocation.getZ() + (distance * rotatedZ);

        Location newLocation = new Location(oldLocation.getWorld(), newX, newY, newZ);

        return newLocation;
    }

    public boolean isLocationEmpty(Location location)
    {
        boolean empty = true;

        outer: for (Infinite infinite : participants.values())
            for (Block block : infinite.getBlocks())
            {
                Location blockLoc = block.getLocation();
                // if equal, it is not empty!
                if (blockLoc.getBlockX() == location.getBlockX() &&
                    blockLoc.getBlockY() == location.getBlockY() &&
                    blockLoc.getBlockZ() == location.getBlockZ())
                {
                    empty = false;
                    break outer;
                }
            }

        return empty;
    }

    public Location findStartingLocation()
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();
        LocationManager locationManager = Parkour.getLocationManager();
        Location middle = locationManager.get(Parkour.getSettingsManager().infinite_middle_loc);

        int radiusX = settingsManager.infinite_soft_border_radius_x;
        int radiusZ = settingsManager.infinite_soft_border_radius_x;
        int foundX = ThreadLocalRandom.current().nextInt(middle.getBlockX() - radiusX, middle.getBlockX() + radiusX + 1);
        int foundZ = ThreadLocalRandom.current().nextInt(middle.getBlockZ() - radiusZ, middle.getBlockZ() + radiusZ + 1);

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

    public void loadLeaderboards()
    {
        for (InfiniteLB lb : leaderboards.values())
            lb.loadLeaderboard();
    }

    public void loadLeaderboard(InfiniteType infiniteType)
    {
        leaderboards.get(infiniteType).loadLeaderboard();
    }

    public void shutdown() {
        for (Infinite infinite : participants.values())
            endPK(infinite.getPlayer());
    }

    public InfiniteLB getLeaderboard(InfiniteType type) {
        return leaderboards.get(type);
    }
}
