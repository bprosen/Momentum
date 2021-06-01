package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.SettingsManager;
import com.parkourcraft.parkour.data.locations.LocationManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfinitePKManager {

    private Set<InfinitePK> participants = new HashSet<>();
    private Set<InfinitePKLBPosition> leaderboard = new HashSet<>(Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

    public InfinitePKManager() {
        InfinitePKDB.loadLeaderboard();
        startScheduler();
    }

    public void startScheduler() {
        // run a check every 3 seconds to stop them from continuing if they fall too far
        new BukkitRunnable() {
            @Override
            public void run() {
                for (InfinitePK infinitePK : participants)
                    if (infinitePK.getPlayer().getLocation().getBlockY() < (infinitePK.getCurrentBlockLoc().getBlockY() - 2))
                        endPK(infinitePK.getPlayer());
            }
        }.runTaskTimer(Parkour.getPlugin(), 20 * 5, 20 * 3);
    }

    public void startPK(Player player) {

        // find loc and teleport player
        Location startingLoc = findStartingLocation();

        // create cache
        InfinitePK infinitePK = new InfinitePK(player);
        participants.add(infinitePK);
        infinitePK.setCurrentBlockLoc(startingLoc);

        // prepare block and teleport
        startingLoc.getBlock().setType(Material.QUARTZ_BLOCK);
        startingLoc.setPitch(player.getLocation().getPitch());
        startingLoc.setYaw(player.getLocation().getYaw());

        // set current loc after teleport
        player.teleport(startingLoc.clone().add(0.5, 1, 0.5));
        // immediately get new loc
        doNextJump(player, true);

        Parkour.getStatsManager().get(player).toggleInfinitePK();
    }

    public void endPK(Player player) {

        InfinitePK infinitePK = get(player.getUniqueId().toString());
        if (infinitePK != null) {

            player.teleport(infinitePK.getOriginalLoc());
            int score = infinitePK.getScore();

            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (isBestScore(player.getName(), score)) {
                player.sendMessage(Utils.translate("&7You have beaten your best &d(" +
                                   Utils.formatNumber(playerStats.getInfinitePKScore()) + ")" +
                                   " &5Infinite Parkour &7record with &d" + Utils.formatNumber(score)));

                updateScore(player.getName(), score);
            } else {
                player.sendMessage(Utils.translate("&7You failed at &d" + Utils.formatNumber(score) + "&7! " +
                                                        "&7Your best is &d" + playerStats.getInfinitePKScore()));
            }

            participants.remove(infinitePK);
            playerStats.toggleInfinitePK();
        }
    }

    public InfinitePK get(String UUID) {
        for (InfinitePK participant : participants)
            if (participant.getUUID().equalsIgnoreCase(UUID))
                return participant;

        return null;
    }

    public void add(InfinitePK infinitePK) {
        participants.add(infinitePK);
    }

    public void remove(String UUID) {
        InfinitePK infinitePK = get(UUID);

        if (infinitePK != null)
            participants.remove(infinitePK);
    }

    public boolean isBestScore(String playerName, int score) {

        boolean isBest = false;

        // it will first check if they have a score (exist), then it will check if their gotten score is better
        if (InfinitePKDB.hasScore(playerName)) {
            int currentBestScore = InfinitePKDB.getScoreFromName(playerName);

            if (score > currentBestScore)
                isBest = true;
        }
        return isBest;
    }

    // method to update their score in all 3 possible placed
    public void updateScore(String playerName, int score) {
        PlayerStats playerStats = Parkour.getStatsManager().getByNameIgnoreCase(playerName);

        if (playerStats != null)
            playerStats.setInfinitePKScore(score);

        if (isLBPosition(playerName))
            getLeaderboardPosition(playerName).setScore(score);

        Parkour.getDatabaseManager().add(
                "UPDATE players SET infinitepk_score=" + score + " WHERE player_name='" + playerName + "'"
        );
    }

    public void doNextJump(Player player, boolean startingJump) {
        InfinitePK infinitePK = get(player.getUniqueId().toString());

        if (infinitePK != null) {

            // go back if in any current blocks
            Location newLocation = findNextBlockSpawn(infinitePK.getCurrentBlockLoc());
            if (isLocInCurrentBlocks(newLocation)) {
                doNextJump(player, startingJump);
            } else {

                infinitePK.addScore();

                // remove old loc and update to new loc if not null, otherwise it is starting location, just remove current block
                if (infinitePK.getLastBlockLoc() != null) {
                    infinitePK.getLastBlockLoc().clone().add(0, 1, 0).getBlock().setType(Material.AIR);
                    infinitePK.getLastBlockLoc().getBlock().setType(Material.AIR);
                } else if (!startingJump) {
                    infinitePK.getCurrentBlockLoc().getBlock().setType(Material.AIR);
                }

                infinitePK.updateBlockLoc(newLocation);

                // then set new
                infinitePK.getCurrentBlockLoc().getBlock().setType(Material.QUARTZ_BLOCK);
                infinitePK.getPressutePlateLoc().getBlock().setType(Material.IRON_PLATE);

                newLocation.getWorld().spawnParticle(Particle.CLOUD, newLocation.getX(), newLocation.getY(), newLocation.getZ(), 50);
            }
        }
    }

    public Location findNextBlockSpawn(Location oldLocation) {

        LocationManager locationManager = Parkour.getLocationManager();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        // this is really -2 to 0, but due to rounding it has to be -3 to 1
        int yMax = 2;
        if (oldLocation.getBlockY() > Parkour.getSettingsManager().max_infinitepk_y)
            yMax--;

        int yIncrease = ThreadLocalRandom.current().nextInt(-1, yMax);

        int xMax = 4, zMax = 4;

        // if the block will be 1 above, need to remove 1 from max
        if (yIncrease > 0) {
            xMax--;
            zMax--;
        }

        int zIncrease = ThreadLocalRandom.current().nextInt(1, zMax + 1);
        int xIncrease = ThreadLocalRandom.current().nextInt(1, xMax + 1);

        // if they are both at max, remove one at random (50 50 chance)
        if (xIncrease == xMax && zIncrease == zMax) {
            if (ThreadLocalRandom.current().nextInt(0, 101) > 50)
                xIncrease--;
            else
                zIncrease--;
        }

        // run through maxes, and flip if they hit it
        if ((oldLocation.getX() + xIncrease) >= (locationManager.getLobbyLocation().getBlockX() + settingsManager.max_infinitepk_x))
            xIncrease *= -1;

        if ((oldLocation.getZ() + zIncrease) >= (locationManager.getLobbyLocation().getBlockZ() + settingsManager.max_infinitepk_z))
            zIncrease *= -1;

        Location newLocation = new Location(oldLocation.getWorld(),
                                         oldLocation.getX() + xIncrease,
                                         oldLocation.getY() + yIncrease,
                                         oldLocation.getZ() + zIncrease);

        return newLocation;
    }

    public Location findStartingLocation() {

        SettingsManager settingsManager = Parkour.getSettingsManager();
        LocationManager locationManager = Parkour.getLocationManager();

        int minX = locationManager.getLobbyLocation().getBlockX() - settingsManager.max_infinitepk_x;
        int maxX = locationManager.getLobbyLocation().getBlockX() + settingsManager.max_infinitepk_x;
        int minZ = locationManager.getLobbyLocation().getBlockZ() - settingsManager.max_infinitepk_z;
        int maxZ = locationManager.getLobbyLocation().getBlockZ() + settingsManager.max_infinitepk_z;

        int foundX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int foundZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        Location foundLoc = new Location(
                locationManager.getLobbyLocation().getWorld(),
                foundX, settingsManager.infinitepk_starting_y, foundZ
        );

        // check if the location will be in the way, if so, go again
        if (isLocInCurrentBlocks(foundLoc))
            findStartingLocation();
        else
            return foundLoc;

        return null;
    }

    public boolean isLocInCurrentBlocks(Location loc) {
        for (InfinitePK infinitePK : participants)
            if (infinitePK.getCurrentBlockLoc().getBlockX() == loc.getBlockX() &&
                infinitePK.getCurrentBlockLoc().getBlockZ() == loc.getBlockZ())
                return true;

        return false;
    }

    public InfinitePKLBPosition getLeaderboardPosition(int position) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getPosition() == position)
                return infinitePKLBPosition;

        return null;
    }

    public InfinitePKLBPosition getLeaderboardPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return infinitePKLBPosition;

        return null;
    }

    public boolean isLBPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return true;

        return false;
    }

    public Set<InfinitePKLBPosition> getLeaderboard() {
        return leaderboard;
    }

    public Set<InfinitePK> getParticipants() {
        return participants;
    }
}
