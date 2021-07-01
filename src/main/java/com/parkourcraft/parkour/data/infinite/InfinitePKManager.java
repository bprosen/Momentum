package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.SettingsManager;
import com.parkourcraft.parkour.data.locations.LocationManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfinitePKManager {

    private HashMap<String, InfinitePK> participants = new HashMap<>();
    private LinkedHashSet<InfinitePKLBPosition> leaderboard = new LinkedHashSet<>(Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

    public InfinitePKManager() {
        startScheduler();
    }

    public void startScheduler() {
        // run a check every 2 seconds to stop them from continuing if they fall too far
        new BukkitRunnable() {
            @Override
            public void run() {

                for (InfinitePK infinitePK : participants.values())
                    if (infinitePK.getPlayer().getLocation().getBlockY() < (infinitePK.getCurrentBlockLoc().getBlockY() - 2))
                        endPK(infinitePK.getPlayer(), false);
            }
        }.runTaskTimer(Parkour.getPlugin(), 20 * 5, 20 * 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public void startPK(Player player) {

        // find loc and teleport player
        Location startingLoc = findStartingLocation();

        // create cache
        InfinitePK infinitePK = new InfinitePK(player);
        participants.put(player.getName(), infinitePK);
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

    public void endPK(Player player, boolean disconnected) {

        InfinitePK infinitePK = get(player.getName());
        if (infinitePK != null) {

            player.teleport(infinitePK.getOriginalLoc());
            int score = infinitePK.getScore();

            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (isBestScore(player.getName(), score)) {
                // if they disconnected
                if (!disconnected) {
                    player.sendMessage(Utils.translate("&7You have beaten your best &d(" +
                            Utils.formatNumber(playerStats.getInfinitePKScore()) + ")" +
                            " &5Infinite Parkour &7record with &d" + Utils.formatNumber(score)));
                }

                updateScore(player.getName(), score);

                // load leaderboard if they have a lb position
                if (scoreWillBeLB(score) || leaderboard.size() < Parkour.getSettingsManager().max_infinitepk_leaderboard_size) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            loadLeaderboard();
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
            } else if (!disconnected) {
                player.sendMessage(Utils.translate("&7You failed at &d" + Utils.formatNumber(score) + "&7! " +
                        "&7Your best is &d" + playerStats.getInfinitePKScore()));
            }
            // clear blocks and reset data
            infinitePK.getLastBlockLoc().getBlock().setType(Material.AIR);
            infinitePK.getPressutePlateLoc().getBlock().setType(Material.AIR);
            infinitePK.getCurrentBlockLoc().getBlock().setType(Material.AIR);
            playerStats.toggleInfinitePK();
            participants.remove(player.getName());
        }
    }

    public InfinitePK get(String playerName) {
        return participants.get(playerName);
    }

    public void remove(String playerName) {
        participants.remove(playerName);
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

        Parkour.getDatabaseManager().run(
                "UPDATE players SET infinitepk_score=" + score + " WHERE player_name='" + playerName + "'"
        );
    }

    public void doNextJump(Player player, boolean startingJump) {
        InfinitePK infinitePK = get(player.getName());

        if (infinitePK != null) {

            // go back if in any current blocks
            Location newLocation = findNextBlockSpawn(infinitePK.getCurrentBlockLoc(), infinitePK);
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

                newLocation.getWorld().spawnParticle(Particle.CLOUD, newLocation.getX(), newLocation.getY(), newLocation.getZ(), 15);
            }
        }
    }

    public Location findNextBlockSpawn(Location oldLocation, InfinitePK infinitePK) {

        LocationManager locationManager = Parkour.getLocationManager();
        SettingsManager settingsManager = Parkour.getSettingsManager();
        InfinitePKDirection directionType = infinitePK.getDirectionType();

        int xMin = 2, zMin = 2, yMin = -1;
        int xMax = 5, zMax = 5, yMax = 2;

        /*
         flip max/min if backwards/forwards and it is not already in negatives/positives (simple < check)
         xMin becomes xMax, etc
         */
        if (directionType == InfinitePKDirection.BACKWARDS && xMin > 0) {
            // make a copy so we do not access already changed values (xMin and zMin)
            int copyMinX = xMin;
            int copyMinZ = zMin;

            xMin = xMax * -1;
            xMax = copyMinX * -1;
            zMin = zMax * -1;
            zMax = copyMinZ * -1;
        }

        // get random increase
        int zIncrease = ThreadLocalRandom.current().nextInt(zMin, zMax);
        int xIncrease = ThreadLocalRandom.current().nextInt(xMin, xMax);

        // since random does not go to bound (6 becomes 5, need to make these real variables to mimic depending on direction)
        int realMaxX, realMaxZ;
        if (directionType == InfinitePKDirection.FORWARDS) {
            realMaxX = xMax++;
            realMaxZ = zMax++;
        } else {
            realMaxX = xMin--;
            realMaxZ = zMin--;
        }

        /*
            below is a section that handles certain cases of impossible jumps, like 4 + 1, 4 by 4, etc
         */
        if (xIncrease == realMaxX) {
            // remove 1 from y
            if (yMax > 1)
                yMax--;

            // adjust z in special case of both being max
            if (zIncrease != realMaxZ) {
                if (directionType == InfinitePKDirection.BACKWARDS && zIncrease < -2)
                    zIncrease += 2;
                else if (zIncrease > 2)
                    zIncrease -= 2;
            } else {
                if (directionType == InfinitePKDirection.BACKWARDS)
                    zIncrease += 3;
                else
                    zIncrease -= 3;
            }
        } else if (zIncrease == realMaxZ) {
            // remove 1 from y
            if (yMax > 1)
                yMax--;

            // adjust x in special case of both being max
            if (xIncrease != realMaxX) {
                if (directionType == InfinitePKDirection.BACKWARDS && xIncrease < -2)
                    xIncrease += 2;
                else if (xIncrease > 2)
                    xIncrease -= 2;
            } else {
                if (directionType == InfinitePKDirection.BACKWARDS)
                    zIncrease += 3;
                else
                    zIncrease -= 3;
            }

        }

        // if too high, reduce max by 1 if it is already above 0
        if (oldLocation.getBlockY() > Parkour.getSettingsManager().max_infinitepk_y && yMax > 1)
            yMax--;

        // if they are going to be below the min, set to 0 instead
        if (yMin < 0 && (oldLocation.getBlockY() - yMin <= Parkour.getSettingsManager().min_infinitepk_y))
            yMin = 0;

        int yIncrease = ThreadLocalRandom.current().nextInt(yMin, yMax);

        // if the block will be 1 above, need to remove/add 1 from max/min depending on direction if x and z increase is 3
        if (yIncrease > 0) {
            if (directionType == InfinitePKDirection.BACKWARDS) {
                if (xIncrease < -2 && zIncrease < -2) {
                    xIncrease++;
                    zIncrease++;
                }
            } else if (xIncrease > 2 && zIncrease > 2) {
                xIncrease--;
                zIncrease--;
            }
        }

        // run through maxes, and flip if they hit it
        if (((oldLocation.getX() + xIncrease) >= (locationManager.getLobbyLocation().getBlockX() + settingsManager.max_infinitepk_x)) ||
             (oldLocation.getZ() + zIncrease) >= (locationManager.getLobbyLocation().getBlockZ() + settingsManager.max_infinitepk_z)) {
            xIncrease *= -1;
            zIncrease *= -1;

            // flip enum
            infinitePK.flipDirectionType();
        }

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

    public void loadLeaderboard() {
        try {

            LinkedHashSet<InfinitePKLBPosition> leaderboard = getLeaderboard();
            leaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    "players",
                    "uuid, player_name, infinitepk_score",
                    " WHERE infinitepk_score > 0" +
                            " ORDER BY infinitepk_score DESC" +
                            " LIMIT " + Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

            outer: for (Map<String, String> scoreResult : scoreResults) {

                // quick loop to make sure there are no duplicates
                for (InfinitePKLBPosition infinitePK : leaderboard)
                    if (infinitePK.getName().equalsIgnoreCase(scoreResult.get("player_name")))
                        continue outer;

                leaderboard.add(
                        new InfinitePKLBPosition(
                                scoreResult.get("uuid"),
                                scoreResult.get("player_name"),
                                Integer.parseInt(scoreResult.get("infinitepk_score")),
                                leaderboard.size() + 1)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLocInCurrentBlocks(Location loc) {
        for (InfinitePK infinitePK : participants.values())
            if ((infinitePK.getCurrentBlockLoc().getBlockX() == loc.getBlockX() &&
                infinitePK.getCurrentBlockLoc().getBlockZ() == loc.getBlockZ()) ||
                loc.getBlock().getType() != Material.AIR)
                return true;
        return false;
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

    public boolean scoreWillBeLB(int score) {
        int lowestScore = 0;
        // gets lowest score
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (lowestScore == 0 || infinitePKLBPosition.getScore() < lowestScore)
                lowestScore = infinitePKLBPosition.getScore();

        if (lowestScore < score)
            return true;
        return false;
    }

    public void shutdown() {
        for (InfinitePK infinitePK : participants.values())
            endPK(infinitePK.getPlayer(), true);
    }

    public LinkedHashSet<InfinitePKLBPosition> getLeaderboard() {
        return leaderboard;
    }

    public HashMap<String, InfinitePK> getParticipants() {
        return participants;
    }
}
