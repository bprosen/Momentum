package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BlackMarketEvent
{
    private ArrayList<PlayerStats> players;
    private boolean canBid;
    private LinkedHashMap<PlayerStats, Integer> bids;
    private PlayerStats highestBidder;
    private int highestBid;
    private int nextMinimumBid;
    private BlackMarketArtifact blackMarketArtifact;
    private BukkitTask taskTimer;
    private Item itemEntity;

    public BlackMarketEvent(BlackMarketArtifact blackMarketArtifact)
    {
        this.bids = new LinkedHashMap<>();
        this.players = new ArrayList<>();
        this.canBid = false;
        this.blackMarketArtifact = blackMarketArtifact;
        this.nextMinimumBid = blackMarketArtifact.getStartingBid();
    }

    public void start()
    {
        broadcastToPlayers(Utils.translate("&7Welcome, welcome, seekers of the forbidden. Step into the shadows, for tonight, the secrets of the underworld await you."));
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                broadcastToPlayers("&7The auction, a spectacle like no other. Hidden from the prying eyes of the staff, it unfolds here where no laws dare to penetrate. There, coveted artifacts, mystical relics, and powerful items change hands in a dance of secrecy.");
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {

                        broadcastToPlayers("&7These.. items.. They hold secrets veiled in ancient whispers, bestowing upon the buyer unique characteristics and buffs of untold origin.");
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                showItem(); // show item
                                broadcastToPlayers("&7Behold, " + getBlackMarketItem().getTitle() + "! An artifact that " + getBlackMarketItem().getDescription() + ".");

                                new BukkitRunnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        canBid = true; // enable bidding
                                        startTimer();
                                        broadcastToPlayers("&7Start your bids now. Do &c/bid (at least " + Utils.formatNumber(getNextMinimumBid()) + ")");
                                    }
                                }.runTaskLater(Parkour.getPlugin(), 20 * 10);
                            }
                        }.runTaskLater(Parkour.getPlugin(), 20 * 10);
                    }
                }.runTaskLater(Parkour.getPlugin(), 20 * 10);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 10);
    }

    public void end()
    {
        if (itemEntity != null)
        {
            itemEntity.remove();
            itemEntity = null;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (PlayerStats playerStats : players)
                    playerStats.getPlayer().teleport(Parkour.getLocationManager().getLobbyLocation()); // teleport to spawn
            }
        }.runTaskLater(Parkour.getPlugin(), 10 * 20); // teleport them all 10 seconds later
    }

    public int getNextMinimumBid()
    {
        return nextMinimumBid;
    }

    public boolean isBiddingAllowed()
    {
        return canBid;
    }

    public BlackMarketArtifact getBlackMarketItem()
    {
        return blackMarketArtifact;
    }

    public PlayerStats getHighestBidder()
    {
        return highestBidder;
    }

    public int getHighestBid() { return highestBid; }

    public boolean isHighestBidder(PlayerStats playerStats)
    {
        return highestBidder.equals(playerStats);
    }

    public void highestBidderLeft()
    {
        bids.remove(highestBidder);

        Map.Entry<PlayerStats, Integer> highestEntry = null;

        // find the next highest entry
        for (Map.Entry<PlayerStats, Integer> entry : bids.entrySet())
            if (highestEntry == null || highestEntry.getValue() < entry.getValue())
                highestEntry = entry;

        if (highestEntry == null)
            Parkour.getBlackMarketManager().forceEnd(); // force end
        else
        {
            highestBidder = highestEntry.getKey();
            highestBid = highestEntry.getValue();
            calcNextMinimumBid(highestBid); // change min bid

            // broadcast change to players
            broadcastToPlayers("&8The highest bidder missed out on the opportunity of a lifetime...");
            broadcastToPlayers("&c" + highestBidder.getPlayer().getDisplayName() + " &8is the new highest bidder for &6" + Utils.formatNumber(highestBid) + " &eCoins");
            broadcastToPlayers("&8Bid at least &6" + Utils.formatNumber(highestBid) + " &eCoins &8to overtake");
        }
    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        if (bids.containsKey(playerStats))
            bids.replace(playerStats, bid);
        else
            bids.put(playerStats, bid);

        this.highestBidder = playerStats;
        this.highestBid = bid;
        calcNextMinimumBid(bid);
        startTimer();
    }

    private void calcNextMinimumBid(int previous)
    {
        this.nextMinimumBid = (int) (previous * blackMarketArtifact.getNextBidMultiplier());
    }

    public void addPlayer(PlayerStats playerStats)
    {
        players.add(playerStats);
    }

    public void removePlayer(PlayerStats playerStats)
    {
        players.remove(playerStats);
    }

    public void broadcastToPlayers(String message)
    {
        for (PlayerStats player : players)
            player.getPlayer().sendMessage(message);
    }

    public boolean inEvent(PlayerStats playerStats)
    {
        return players.contains(playerStats);
    }

    public int getPlayerCount()
    {
        return players.size();
    }

    private void startTimer()
    {
        // restart if found
        if (taskTimer != null)
            taskTimer.cancel();

        int timer = Parkour.getSettingsManager().seconds_before_ending_from_no_bids;
        taskTimer = new BukkitRunnable()
        {
            int seconds = 0;
            @Override
            public void run()
            {
                seconds++;
                int secondsLeft = timer - seconds;

                switch (secondsLeft)
                {
                    case 10:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                    case 1:
                        broadcastToPlayers("&8There are &c" + secondsLeft + " &8seconds left to increase the bid to &6" + Utils.formatNumber(nextMinimumBid) + " &eCoins");
                        break;
                    case 0:
                        cancel();
                        canBid = false;
                        Parkour.getBlackMarketManager().end();
                        break;
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), 20, 20); // every second
    }

    public void showItem()
    {
        Location itemSpawn = Parkour.getSettingsManager().black_market_item_spawn;
        itemEntity = itemSpawn.getWorld().dropItem(itemSpawn.clone().add(0, 1, 0), getBlackMarketItem().getItemStack());

        // show item
        itemEntity.setVelocity(new Vector(0, .2, 0));
        itemEntity.setCustomName(Utils.translate(getBlackMarketItem().getTitle()));
        itemEntity.setCustomNameVisible(true);
        itemEntity.setPickupDelay(Integer.MAX_VALUE);
    }
}
