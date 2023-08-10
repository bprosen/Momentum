package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

    public BlackMarketEvent(BlackMarketArtifact blackMarketArtifact)
    {
        bids = new LinkedHashMap<>();
        players = new ArrayList<>();
        canBid = false;
        this.blackMarketArtifact = blackMarketArtifact;
        this.nextMinimumBid = blackMarketArtifact.getStartingBid();
    }

    public void teleportToSpawn()
    {
        for (PlayerStats playerStats : players)
            playerStats.getPlayer().teleport(Parkour.getLocationManager().getLobbyLocation()); // teleport to spawn
    }

    public int getNextMinimumBid()
    {
        return nextMinimumBid;
    }

    public void beginBid()
    {
        canBid = true;
        startTimer();
    }

    public void stopBid()
    {
        canBid = false;
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

        for (Map.Entry<PlayerStats, Integer> entry : bids.entrySet())
        {
            if (highestEntry == null || highestEntry.getValue() < entry.getValue())
                highestEntry = entry;
        }

        if (highestEntry == null)
        {
            Parkour.getBlackMarketManager().forceEnd(); // force end
        }
        else
        {
            highestBidder = highestEntry.getKey();
            highestBid = highestEntry.getValue();
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
        this.nextMinimumBid = (int) (bid * blackMarketArtifact.getNextBidMultiplier());
        startTimer();
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
                        stopBid();
                        Parkour.getBlackMarketManager().end();
                        break;
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), 20, 20); // every second
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
}
