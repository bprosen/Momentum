package com.renatusnetwork.parkour.data.infinite;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.InfiniteEndEvent;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class InfinitePKManager {

    private HashMap<String, InfinitePK> participants = new HashMap<>();
    private HashMap<Integer, InfinitePKLBPosition> leaderboard = new HashMap<>(Parkour.getSettingsManager().max_infinitepk_leaderboard_size);
    private LinkedHashMap<Integer, InfinitePKReward> rewards = new LinkedHashMap<>(); // linked so order stays

    public InfinitePKManager() {
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

    public void startPK(PlayerStats playerStats, boolean fromPortal) {

        /*
            We force run this in sync for a few reasons:
            1. The packet listener executes this in async which cant be done as this creates blocks.
            2. Someone with bad ping can sometimes make this method run many times causing glitched blocks in the air
               if they have bad ping.
         */
        new BukkitRunnable() {
            @Override
            public void run() {

                // if from portal, prevent getting in via spectator
                if (playerStats.isInInfinitePK() || (fromPortal && playerStats.isSpectating()))
                    return;

                Player player = playerStats.getPlayer();

                // find loc
                Location startingLoc = findStartingLocation();
                startingLoc.setPitch(Parkour.getSettingsManager().infinitepk_starting_pitch);
                startingLoc.setYaw(Parkour.getSettingsManager().infinitepk_starting_yaw);

                // create cache
                InfinitePK infinitePK = new InfinitePK(player);
                participants.put(player.getName(), infinitePK);

                infinitePK.setCurrentBlockLoc(startingLoc);

                Location respawnLoc = Parkour.getLocationManager().get(Parkour.getSettingsManager().infinitepk_respawn_loc);

                // if they are at spawn prior to teleport, change original loc to setting
                if (fromPortal && respawnLoc != null)
                    infinitePK.setOriginalLoc(respawnLoc);

                // prepare block and teleport
                startingLoc.getBlock().setType(playerStats.getInfiniteBlock());
                playerStats.clearPotionEffects();
                player.teleport(startingLoc.clone().add(0.5, 1, 0.5));
                // set to true
                playerStats.setInfinitePK(true);

                // immediately get new loc
                doNextJump(playerStats, true);

            }
        }.runTask(Parkour.getPlugin());
    }

    public void endPK(Player player, boolean disconnected)
    {

        InfinitePK infinitePK = get(player.getName());
        if (infinitePK != null)
        {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            InfiniteEndEvent event = new InfiniteEndEvent(playerStats, infinitePK.getScore());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled())
            {
                // run in sync because of packet listener running in async, need to remove blocks in sync
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // tp in sync
                        player.teleport(infinitePK.getOriginalLoc());
                        // clear blocks and reset data
                        infinitePK.getLastBlockLoc().getBlock().setType(Material.AIR);
                        infinitePK.getPressutePlateLoc().getBlock().setType(Material.AIR);
                        infinitePK.getCurrentBlockLoc().getBlock().setType(Material.AIR);
                    }
                }.runTask(Parkour.getPlugin());

                int score = infinitePK.getScore();

                boolean doRewardsMsg = false;
                List<InfinitePKReward> rewards = null;

                if (score > playerStats.getInfinitePKScore())
                {
                    rewards = getApplicableRewards(playerStats.getInfinitePKScore(), score);

                    // dispatch command if not null and they havent gotten this reward yet
                    if (!rewards.isEmpty()) {
                        doRewardsMsg = true;

                        // run in sync for safety
                        for (InfinitePKReward reward : rewards) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // loop through and run commands of applicable rewards
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.getCommand().replace("%player%", player.getName()));
                                }
                            }.runTask(Parkour.getPlugin());
                        }
                    }
                }

                int coinReward = event.getReward();

                if (playerStats.hasModifier(ModifierTypes.INFINITE_BOOSTER))
                {
                    Booster booster = (Booster) playerStats.getModifier(ModifierTypes.INFINITE_BOOSTER);
                    coinReward *= booster.getMultiplier();
                }

                String rewardString = Utils.getCoinFormat(score, coinReward);

                if (isBestScore(player.getName(), score))
                {
                    // if they disconnected
                    if (!disconnected) {
                        player.sendMessage(Utils.translate(
                                "&7You have beaten your previous record of &d" +
                                        Utils.formatNumber(playerStats.getInfinitePKScore()) + " &7with &d" + Utils.formatNumber(score) + "\n" +
                                        "&7Awarded " + rewardString + " &eCoins"
                        ));

                        if (doRewardsMsg) {
                            // we can safely assume since it will only be true if its not empty
                            for (InfinitePKReward reward : rewards)
                                player.sendMessage(Utils.translate(
                                        " &7You received &d" + reward.getName() + " &d(Score of " + Utils.formatNumber(reward.getScoreNeeded()) + ")"));
                        }
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
                    player.sendMessage(Utils.translate(
                            "&7You failed at &d" + Utils.formatNumber(score) + " &5(Best is " + Utils.formatNumber(playerStats.getInfinitePKScore()) + ")\n" +
                                    "&7Awarded &6" + rewardString + " &eCoins"
                    ));
                }
                // deposit reward from listener (default = score)
                Parkour.getStatsManager().addCoins(playerStats, coinReward);

                playerStats.setInfinitePK(false);
                participants.remove(player.getName());
            }
        }
    }

    public LinkedHashMap<Integer, InfinitePKReward> getRewards() { return rewards; }

    public InfinitePKReward getReward(int score) { return rewards.get(score); }

    public void addReward(InfinitePKReward infinitePKReward) { rewards.put(infinitePKReward.getScoreNeeded(), infinitePKReward); }

    public void clearRewards() { rewards.clear(); }

    public List<InfinitePKReward> getApplicableRewards(int oldBestScore, int newBestScore) {
        List<InfinitePKReward> tempRewards = new ArrayList<>();

        for (Map.Entry<Integer, InfinitePKReward> entry : rewards.entrySet()) {
            int score = entry.getKey();
            // if score is greater than old and less than or = to new, then add
            if (score > oldBestScore && score <= newBestScore)
                tempRewards.add(entry.getValue());
        }
        return tempRewards;
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
        PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

        if (playerStats != null)
            playerStats.setInfinitePKScore(score);

        if (isLBPosition(playerName))
            getLeaderboardPosition(playerName).setScore(score);

        Parkour.getDatabaseManager().run(
                "UPDATE players SET infinitepk_score=" + score + " WHERE player_name='" + playerName + "'"
        );
    }

    public void doNextJump(PlayerStats playerStats, boolean startingJump)
    {
        Player player = playerStats.getPlayer();
        InfinitePK infinitePK = get(player.getName());

        if (infinitePK != null) {

            // go back if in any current blocks
            Location newLocation = findNextBlockSpawn(infinitePK.getCurrentBlockLoc(), infinitePK);
            if (isLocInCurrentBlocks(newLocation))
                doNextJump(playerStats, startingJump);
            else
            {
                // so they dont start at score of 1
                if (!startingJump)
                    infinitePK.addScore();

                // remove old loc and update to new loc if not null, otherwise it is starting location, just remove current block
                if (infinitePK.getLastBlockLoc() != null) {
                    infinitePK.getLastBlockLoc().clone().add(0, 1, 0).getBlock().setType(Material.AIR);
                    infinitePK.getLastBlockLoc().getBlock().setType(Material.AIR);
                }
                else if (!startingJump)
                    infinitePK.getCurrentBlockLoc().getBlock().setType(Material.AIR);

                infinitePK.updateBlockLoc(newLocation);

                // then set new
                infinitePK.getCurrentBlockLoc().getBlock().setType(playerStats.getInfiniteBlock());
                infinitePK.getPressutePlateLoc().getBlock().setType(Material.IRON_PLATE);

                if (!startingJump)
                    newLocation.getWorld().spawnParticle(Particle.CLOUD, newLocation.getX(), newLocation.getY(), newLocation.getZ(), 15);
            }
        }
    }

    public Location findNextBlockSpawn(Location oldLocation, InfinitePK infinitePK) {

        LocationManager locationManager = Parkour.getLocationManager();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        Player player = infinitePK.getPlayer();
        Vector facing = player.getLocation().getDirection().setY(0);

        Location newLocation = oldLocation.clone().add(facing.multiply(random.nextInt(3, 6)));

        return newLocation;
    }

    public Location findStartingLocation() {

        SettingsManager settingsManager = Parkour.getSettingsManager();
        LocationManager locationManager = Parkour.getLocationManager();
        Location middle = locationManager.get(Parkour.getSettingsManager().infinitepk_middle_loc);

        // make the box 6 less so it will never flip on the first one
        int minX = middle.getBlockX() - (settingsManager.max_infinitepk_x - 6);
        int maxX = middle.getBlockX() + (settingsManager.max_infinitepk_x - 6);
        int minZ = middle.getBlockZ() - (settingsManager.max_infinitepk_z - 6);
        int maxZ = middle.getBlockZ() + (settingsManager.max_infinitepk_z - 6);

        int foundX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int foundZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        Location foundLoc = new Location(
                middle.getWorld(), foundX, settingsManager.infinitepk_starting_y, foundZ
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

            HashMap<Integer, InfinitePKLBPosition> leaderboard = getLeaderboard();
            leaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    "players",
                    "uuid, player_name, infinitepk_score",
                    " WHERE infinitepk_score > 0" +
                            " ORDER BY infinitepk_score DESC" +
                            " LIMIT " + Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

            int lbPos = 1;
            for (Map<String, String> scoreResult : scoreResults) {
                leaderboard.put(lbPos,
                        new InfinitePKLBPosition(
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

    public boolean isLocInCurrentBlocks(Location loc) {
        for (InfinitePK infinitePK : participants.values())
            if ((infinitePK.getCurrentBlockLoc().getBlockX() == loc.getBlockX() &&
                infinitePK.getCurrentBlockLoc().getBlockZ() == loc.getBlockZ()) ||
                loc.getBlock().getType() != Material.AIR)
                return true;
        return false;
    }

    public InfinitePKLBPosition getLeaderboardPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard.values())
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return infinitePKLBPosition;

        return null;
    }

    public boolean isLBPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard.values())
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return true;

        return false;
    }

    public boolean scoreWillBeLB(int score) {
        int lowestScore = 0;
        // gets lowest score
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard.values())
            if (lowestScore == 0 || infinitePKLBPosition.getScore() < lowestScore)
                lowestScore = infinitePKLBPosition.getScore();

        return lowestScore <= score;
    }

    public void shutdown() {
        for (InfinitePK infinitePK : participants.values())
            endPK(infinitePK.getPlayer(), true);
    }

    public HashMap<Integer, InfinitePKLBPosition> getLeaderboard() {
        return leaderboard;
    }

    public HashMap<String, InfinitePK> getParticipants() {
        return participants;
    }
}
