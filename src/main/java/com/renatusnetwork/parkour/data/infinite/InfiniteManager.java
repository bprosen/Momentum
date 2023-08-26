package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.InfiniteEndEvent;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.infinite.gamemode.*;
import com.renatusnetwork.parkour.data.infinite.leaderboard.InfiniteLB;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteReward;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteRewards;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteRewardsYAML;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfiniteManager {

    private HashMap<String, Infinite> participants;
    private HashMap<InfiniteType, InfiniteLB> leaderboards;
    private HashMap<InfiniteType, InfiniteRewards> rewards;

    public InfiniteManager()
    {
        this.participants = new HashMap<>();

        loadAllRewards();
        startScheduler();
        initLeaderboards();
    }

    public void initLeaderboards()
    {
        this.leaderboards = new HashMap<>(Parkour.getSettingsManager().max_infinite_leaderboard_size);

        leaderboards.put(InfiniteType.CLASSIC, new InfiniteLB(InfiniteType.CLASSIC));
        leaderboards.put(InfiniteType.SPEEDRUN, new InfiniteLB(InfiniteType.SPEEDRUN));
        leaderboards.put(InfiniteType.SPRINT, new InfiniteLB(InfiniteType.SPRINT));
        leaderboards.put(InfiniteType.TIMED, new InfiniteLB(InfiniteType.TIMED));
    }

    public void startScheduler()
    {

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
            InfiniteType infiniteType = playerStats.getInfiniteType();

            if (score > playerStats.getBestInfiniteScore())
            {
                for (InfiniteReward reward : getApplicableRewards(infiniteType, playerStats.getBestInfiniteScore(), score))
                {
                    // loop through and run commands of applicable rewards
                    player.sendMessage(Utils.translate(" &7You received &d" + reward.getDisplay() + " &d(Score of " + Utils.formatNumber(reward.getScoreNeeded()) + ")"));

                    // run commands
                    for (String command : reward.getCommands())
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            }

            if (playerStats.hasModifier(ModifierTypes.INFINITE_BOOSTER))
            {
                Booster booster = (Booster) playerStats.getModifier(ModifierTypes.INFINITE_BOOSTER);
                coinReward *= booster.getMultiplier();
            }

            String rewardString = Utils.getCoinFormat(score, coinReward);
            String formattedType = StringUtils.capitalize(infiniteType.toString().toLowerCase());

            if (playerStats.getBestInfiniteScore() < score)
            {
                // if they are online
                if (player.isOnline())
                {
                    player.sendMessage(Utils.translate(
                            "&7You have beaten your previous &d" + formattedType + "&7 record of &d" +
                                    Utils.formatNumber(playerStats.getBestInfiniteScore()) + " &7with &d" + Utils.formatNumber(score) + "\n" +
                                    "&7Awarded " + rewardString + " &eCoins"
                    ));
                }

                updateScore(player.getName(), playerStats.getInfiniteType(), score);
            }
            else if (player.isOnline())
                player.sendMessage(Utils.translate(
                        "&7You failed &d" + formattedType + "&7 at &d" + Utils.formatNumber(score) + " &5(Best is " + Utils.formatNumber(playerStats.getBestInfiniteScore()) + ")\n" +
                                "&7Awarded &6" + rewardString + " &eCoins"
                ));
            // deposit reward from listener (default = score)
            Parkour.getStatsManager().addCoins(playerStats, coinReward);
        }
    }

    public InfiniteRewards getRewards(InfiniteType type) { return rewards.get(type); }

    public void loadAllRewards()
    {
        rewards = new HashMap<>();
        int sizeRewards = 0;

        for (InfiniteType type : InfiniteType.values())
        {
            loadRewards(type);
            sizeRewards += rewards.get(type).size();
        }

        Parkour.getPluginLogger().info("Infinite rewards loaded: " + sizeRewards);
    }

    public void loadRewards(InfiniteType type)
    {
        rewards.put(type, new InfiniteRewards(type));
    }

    public List<InfiniteReward> getApplicableRewards(InfiniteType type, int oldBestScore, int newBestScore)
    {
        List<InfiniteReward> tempRewards = new ArrayList<>();
        InfiniteRewards infiniteRewards = rewards.get(type);

        for (InfiniteReward reward : infiniteRewards.getRewards())
        {
            int score = reward.getScoreNeeded();
            // if score is greater than old and less than or = to new, then add
            if (score > oldBestScore && score <= newBestScore)
                tempRewards.add(reward);
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

    public Location generateNextBlockLocation(Location oldLocation, Player player, Infinite infinite)
    {
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ThreadLocalRandom random = ThreadLocalRandom.current();

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

        // if it was modified and either the difference is less than the min or the bound is <= min
        if (notZero && (bound <= min || Math.abs(bound - min) < diff))
            bound = min + diff;

        // get random distance modifier
        double distance = random.nextDouble(min, bound);
        double angle;
        Vector locationVector;

        if (infinite.isOutsideBorder())
        {
            // turn angle and return it
            angle = infinite.turnAngle();
            Location middle = Parkour.getLocationManager().get(Parkour.getSettingsManager().infinite_middle_loc);
            locationVector = oldLocation.toVector().subtract(middle.toVector()).setY(0).normalize();
        }
        else
        {
            // get random angle
            angle = random.nextDouble(-Math.PI / settingsManager.infinite_angle_bound, Math.PI / settingsManager.infinite_angle_bound); // Adjust the range as needed
            locationVector = player.getLocation().getDirection().setY(0).normalize(); // ignore y;
        }

        // rotate the vector by the set angle
        double rotatedX = locationVector.getX() * Math.cos(angle) - locationVector.getZ() * Math.sin(angle);
        double rotatedZ = locationVector.getX() * Math.sin(angle) + locationVector.getZ() * Math.cos(angle);

        // new x and z!
        double newX = oldLocation.getX() + (distance * rotatedX);
        double newZ = oldLocation.getZ() + (distance * rotatedZ);

        Location newLocation = new Location(oldLocation.getWorld(), newX, newY, newZ);

        // if loc is outside border, continue
        if (isOutsideBorder(newLocation))
        {
            // only enter border if they are outside of it!
            if (!infinite.isOutsideBorder())
                infinite.enterBorder(angle);
        }
        // if their new loc is not outside border but they were outside, then exit
        else if (infinite.isOutsideBorder())
            infinite.exitBorder();

        return newLocation;
    }

    public boolean isOutsideBorder(Location location)
    {
        int radiusX = Parkour.getSettingsManager().infinite_soft_border_radius_x;
        int radiusZ = Parkour.getSettingsManager().infinite_soft_border_radius_z;
        Location middle = Parkour.getLocationManager().get(Parkour.getSettingsManager().infinite_middle_loc);

        double middleX = middle.getX();
        double middleZ = middle.getZ();

        // conditions where they are outside the border
        return ((middleX + radiusX) < location.getX() ||
                (middleX - radiusX) > location.getX() ||
                (middleZ + radiusZ) < location.getZ() ||
                (middleZ - radiusZ) > location.getZ());
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

        // make it so they will be anywhere in the border - 10 (so they do not go straight out of border)
        int radiusX = settingsManager.infinite_soft_border_radius_x - 10;
        int radiusZ = settingsManager.infinite_soft_border_radius_z - 10;
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

    public InfiniteLB getLeaderboard(InfiniteType type) {
        return leaderboards.get(type);
    }

    public void shutdown() {
        for (Infinite infinite : participants.values())
            endPK(infinite.getPlayer());
    }
}
