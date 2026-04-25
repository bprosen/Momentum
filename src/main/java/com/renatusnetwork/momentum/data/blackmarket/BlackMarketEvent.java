package com.renatusnetwork.momentum.data.blackmarket;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlackMarketEvent {

    private HashSet<PlayerStats> players;
    private boolean canBid;
    private LinkedHashMap<PlayerStats, Integer> bids;
    private PlayerStats highestBidder;
    private int highestBid;
    private int nextMinimumBid;
    private BlackMarketArtifact blackMarketArtifact;
    private BukkitTask taskTimer;
    private Item itemEntity;
    private BukkitTask particleTask;

    public BlackMarketEvent(BlackMarketArtifact blackMarketArtifact) {
        this.bids = new LinkedHashMap<>();
        this.players = new HashSet<>();
        this.blackMarketArtifact = blackMarketArtifact;
        this.nextMinimumBid = blackMarketArtifact.getStartingBid();

        doParticlesAtPortal();
    }

    private void doParticlesAtPortal() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                Location location = Momentum.getLocationManager().get(SettingsManager.BLACK_MARKET_PORTAL_NAME);
                location.getWorld().spawnParticle(Particle.PORTAL, location, 10);
            }
        }.runTaskTimer(Momentum.getPlugin(), 20, 20); // every .5 seconds
    }

    private void endParticlesAtPortal() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    public void start() {
        String prefix = Momentum.getSettingsManager().blackmarket_message_prefix;

        BlackMarketManager blackMarketManager = Momentum.getBlackMarketManager();

        endParticlesAtPortal();
        playSound(Sound.ENTITY_ELDER_GUARDIAN_CURSE);
        broadcastToPlayers(Utils.translate(prefix + " &7Welcome, welcome, seekers of the forbidden. Step into the shadows, for tonight, the secrets of the underworld await you."));
        // need to check if it is still running at each stage
        new BukkitRunnable() {
            @Override
            public void run() {
                if (blackMarketManager.isRunning()) {
                    broadcastToPlayers(Utils.translate(prefix + " &7The auction, a spectacle like no other. Hidden from the prying eyes of the staff, it unfolds here where no laws dare to penetrate. There, coveted artifacts, mystical relics, and powerful items change hands in a dance of secrecy."));
                    new BukkitRunnable() {
                        @Override
                        public void run() {

                            if (blackMarketManager.isRunning()) {
                                broadcastToPlayers(Utils.translate(prefix + " &7These.. items.. They hold secrets veiled in ancient whispers, bestowing upon the buyer unique characteristics and buffs of untold origin."));
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (blackMarketManager.isRunning()) {
                                            showItem(); // show item
                                            playSound(Sound.ENTITY_ENDERDRAGON_AMBIENT);
                                            broadcastToPlayers(Utils.translate(prefix + " &7Behold, " + getBlackMarketItem().getTitle() + "&7! An artifact that " + getBlackMarketItem().getDescription() + "."));

                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    if (blackMarketManager.isRunning()) {
                                                        canBid = true; // enable bidding
                                                        startTimer();
                                                        broadcastToPlayers(Utils.translate(prefix + " &7Start your bids now. Do &c/bid (at least " + Utils.formatNumber(getNextMinimumBid()) + ")"));
                                                    }
                                                }
                                            }.runTaskLater(Momentum.getPlugin(), 20 * 10);
                                        }
                                    }
                                }.runTaskLater(Momentum.getPlugin(), 20 * 10);
                            }
                        }
                    }.runTaskLater(Momentum.getPlugin(), 20 * 10);
                }
            }
        }.runTaskLater(Momentum.getPlugin(), 20 * 10);
    }

    public void end(boolean forceEnded) {
        if (itemEntity != null) {
            itemEntity.remove();
            itemEntity = null;
        }

        // cancel if not null
        if (taskTimer != null) {
            taskTimer.cancel();
        }

        endParticlesAtPortal();

        // coin removal, reward and message processing
        if (!forceEnded && hasHighestBidder()) {
            Momentum.getStatsManager().removeCoins(highestBidder, highestBid);
            Player player = highestBidder.getPlayer();

            for (String command : blackMarketArtifact.getRewardCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", highestBidder.getName())); // send command
            }

            for (String message : blackMarketArtifact.getWinnerMessages()) {
                player.sendMessage(Utils.translate(message)); // send msgs
            }

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    public int getNextMinimumBid() {
        return nextMinimumBid;
    }

    public boolean isBiddingAllowed() {
        return canBid;
    }

    public BlackMarketArtifact getBlackMarketItem() {
        return blackMarketArtifact;
    }

    public PlayerStats getHighestBidder() {
        return highestBidder;
    }

    public int getHighestBid() {
        return highestBid;
    }

    public boolean hasHighestBidder() {
        return highestBidder != null;
    }

    public boolean isHighestBidder(PlayerStats playerStats) {
        return highestBidder != null && highestBidder.equals(playerStats);
    }

    public void highestBidderLeft() {
        bids.remove(highestBidder);

        Map.Entry<PlayerStats, Integer> highestEntry = null;

        // find the next highest entry
        for (Map.Entry<PlayerStats, Integer> entry : bids.entrySet()) {
            if (highestEntry == null || highestEntry.getValue() < entry.getValue()) {
                highestEntry = entry;
            }
        }

        if (highestEntry == null) {
            Momentum.getBlackMarketManager().forceEnd(); // force end
        } else {
            highestBidder = highestEntry.getKey();
            highestBid = highestEntry.getValue();

            calcNextMinimumBid(highestBid); // change min bid

            String prefix = Momentum.getSettingsManager().blackmarket_message_prefix;

            // broadcast change to players
            broadcastToPlayers(Utils.translate(prefix + " &8The highest bidder missed out on the opportunity of a lifetime..."));
            broadcastToPlayers(Utils.translate(prefix + " &c" + highestBidder.getPlayer().getDisplayName() + " &8is the new highest bidder for &6" + Utils.formatNumber(highestBid) + " &eCoins"));
            broadcastToPlayers(Utils.translate(prefix + " &8Bid at least &6" + Utils.formatNumber(nextMinimumBid) + " &eCoins &8to overtake"));
        }
    }

    public void increaseBid(PlayerStats playerStats, int bid) {
        if (bids.containsKey(playerStats)) {
            bids.replace(playerStats, bid);
        } else {
            bids.put(playerStats, bid);
        }

        this.highestBidder = playerStats;
        this.highestBid = bid;
        calcNextMinimumBid(bid);
        startTimer();
    }

    private void calcNextMinimumBid(int previous) {
        this.nextMinimumBid = (int) (previous * blackMarketArtifact.getNextBidMultiplier());
    }

    public void addPlayer(PlayerStats playerStats) {
        players.add(playerStats);
    }

    public void removePlayer(PlayerStats playerStats) {
        players.remove(playerStats);
    }

    public void broadcastToPlayers(String message) {
        for (PlayerStats player : players) {
            player.getPlayer().sendMessage(message);
        }
    }

    public boolean inEvent(PlayerStats playerStats) {
        return players.contains(playerStats);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public HashSet<PlayerStats> getPlayers() {
        return players;
    }

    private void startTimer() {
        // restart if found
        if (taskTimer != null) {
            taskTimer.cancel();
        }

        int timer = Momentum.getSettingsManager().seconds_before_ending_from_no_bids;
        taskTimer = new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                seconds++;
                int secondsLeft = timer - seconds;

                switch (secondsLeft) {
                    case 10:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 1:
                        broadcastToPlayers(Utils.translate(Momentum.getSettingsManager().blackmarket_message_prefix + " &8There are &c" + secondsLeft + " &8seconds left to increase the bid to &6" + Utils.formatNumber(nextMinimumBid) + " &eCoins"));
                        playSound(Sound.BLOCK_STONE_BUTTON_CLICK_ON);
                        break;
                    case 0:
                        cancel();
                        canBid = false;
                        Momentum.getBlackMarketManager().end();
                        break;
                }
            }
        }.runTaskTimer(Momentum.getPlugin(), 20, 20); // every second
    }

    public void showItem() {
        Location itemSpawn = Momentum.getLocationManager().get(Momentum.getSettingsManager().blackmarket_item_spawn_loc);
        itemEntity = itemSpawn.getWorld().dropItem(itemSpawn.clone().add(0, 1, 0), getBlackMarketItem().getItemStack());

        // show item
        itemEntity.setVelocity(new Vector(0, .2, 0));
        itemEntity.setCustomName(Utils.translate(getBlackMarketItem().getTitle()));
        itemEntity.setCustomNameVisible(true);
        itemEntity.setPickupDelay(Integer.MAX_VALUE);
        Utils.spawnFirework(itemSpawn, Color.BLACK, Color.GRAY, false);
    }

    public void playSound(Sound sound) {
        for (PlayerStats playerStats : players) {
            playerStats.getPlayer().playSound(playerStats.getPlayer().getLocation(), sound, 1.0F, 1.0F);
        }
    }
}
