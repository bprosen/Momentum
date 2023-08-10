package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BlackMarketManager
{
    private BlackMarketEvent running;
    private boolean inPreperation;
    private ArrayList<BlackMarketArtifact> artifacts;

    public BlackMarketManager()
    {
        inPreperation = false;
        artifacts = new ArrayList<>();
        running = null;

        load();
        runScheduler();
    }

    public void load()
    {
        // clear first
        artifacts.clear();

        for (String name : BlackMarketYAML.getItemNames())
            artifacts.add(new BlackMarketArtifact(name));
    }

    private void runScheduler()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Calendar bankCalendar = Parkour.getSettingsManager().black_market_reset_calendar;
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(new Date());

                // if bankCalendar is BEFORE current, run blackmarket!
                if (bankCalendar.get(Calendar.DAY_OF_WEEK) == currentCalendar.get(Calendar.DAY_OF_WEEK) &&
                    bankCalendar.get(Calendar.HOUR_OF_DAY) == currentCalendar.get(Calendar.HOUR_OF_DAY))
                {
                    start();
                }
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 60 * 60, 20 * 60 * 60); // check every hour
    }
    public void start()
    {
        if (!isRunning())
        {
            running = new BlackMarketEvent(artifacts.get(ThreadLocalRandom.current().nextInt(artifacts.size())));

            inPreperation = true;

            // begin timer before starting event
            new BukkitRunnable()
            {
                int timerCount = 5;

                @Override
                public void run()
                {
                    if (timerCount == 0)
                    {
                        cancel();
                        inPreperation = false;

                        // only start if met the minimum
                        if (running.getPlayerCount() >= Parkour.getSettingsManager().blackmarket_min_player_count)
                            running.start();
                        else
                        {
                            // cancel because not enough players
                            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));
                            Bukkit.broadcastMessage(Utils.translate("&8&lBLACK MARKET"));
                            Bukkit.broadcastMessage(Utils.translate(""));
                            Bukkit.broadcastMessage(Utils.translate("&8The risk isn't worth it..."));
                            Bukkit.broadcastMessage(Utils.translate("&8There isn't enough competition."));
                            Bukkit.broadcastMessage(Utils.translate("&c(Not enough players, need " + Parkour.getSettingsManager().blackmarket_min_player_count + ")"));
                            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));
                        }
                    }
                    else
                    {
                        HashMap<String, PlayerStats> stats = Parkour.getStatsManager().getPlayerStats();

                        synchronized (stats)
                        {
                            for (PlayerStats stat : stats.values())
                            {
                                if (!running.inEvent(stat))
                                {
                                    Player player = stat.getPlayer();

                                    // reminder for players to join
                                    player.sendMessage(Utils.translate("&8&m-------------------------------"));
                                    player.sendMessage(Utils.translate("&8&lBLACK MARKET"));
                                    player.sendMessage(Utils.translate(""));
                                    player.sendMessage(Utils.translate("&8Come to the hollow depths of &c/spawn"));
                                    player.sendMessage(Utils.translate("&8for a risky trade of illegal wares."));
                                    player.sendMessage(Utils.translate("&8You have &c" + timerCount + " minutes..."));
                                    player.sendMessage(Utils.translate("&8&m-------------------------------"));
                                }
                            }
                        }

                        timerCount--;
                    }
                }
            }.runTaskTimer(Parkour.getPlugin(), 0, 20 * 60);
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to start a Black Market event with one in-progress");
        }
    }

    public void end()
    {

        if (isRunning())
        {
            running.broadcastToPlayers(Utils.translate("&8&m-------------------------------"));
            running.broadcastToPlayers(Utils.translate("&8&lBLACK MARKET"));
            running.broadcastToPlayers(Utils.translate(""));
            running.broadcastToPlayers(Utils.translate("&c" + running.getHighestBidder().getPlayer().getDisplayName() +  " &8has earned the &c" + running.getBlackMarketItem().getTitle()));
            running.broadcastToPlayers(Utils.translate("&8for a staggering &6" + Utils.formatNumber(running.getHighestBid()) + " &eCoins&8."));
            running.broadcastToPlayers(Utils.translate("&8Get out before the staff find you."));
            running.broadcastToPlayers(Utils.translate("&cUntil our next sale..."));
            running.broadcastToPlayers(Utils.translate("&8&m-------------------------------"));

            running.end();

            // TODO: reward here
            running = null;

            runEndingSchedulers();
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to end a Black Market event with none in-progress");
        }
    }

    public void forceEnd()
    {
        if (!isRunning())
        {
            running.end();

            // TODO: reward here
            running = null;
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to force end a Black Market event with none in-progress");
        }
    }

    private void runEndingSchedulers()
    {
        // ending schedulers
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                // run final jackpot 5 mins later
                Parkour.getBankManager().startJackpot();

                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        // LOAD NEW BANK ITEMS!
                        Parkour.getBankManager().resetItems();
                        Parkour.getBankManager().broadcastReset();
                    }
                }.runTaskLater(Parkour.getPlugin(), (20 * Parkour.getSettingsManager().jackpot_length) + 20 * 60 * 5); // jackpot length + 5 minutes
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 60 * 5); // 5 mins later
    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        if (isRunning())
        {
            running.increaseBid(playerStats, bid);
            running.broadcastToPlayers(Utils.translate(
                    "&c" + playerStats.getPlayer().getDisplayName() + " &8has increased the bid to &6" + Utils.formatNumber(bid) + " &eCoins"
            ));
            running.broadcastToPlayers(Utils.translate("&8The next bid starts at &6" + Utils.formatNumber(running.getNextMinimumBid()) + " &eCoins"));
        }
    }

    public void playerJoined(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();

        if (isRunning())
        {
            // if the event is still waiting for players
            if (!inPreperation)
            {
                player.sendMessage(Utils.translate("&8You're too late... the risk is too high to take you in."));
                player.sendMessage(Utils.translate("&cCome early next time."));
            }
            else
                running.addPlayer(playerStats);
        }
        else
            player.sendMessage(Utils.translate("&cThere is nothing to see here..."));
    }

    public void playerLeft(PlayerStats playerStats, boolean disconnected)
    {
        Player player = playerStats.getPlayer();

        if (isRunning())
        {
            running.removePlayer(playerStats);

            // remove highest bidder
            if (running.isHighestBidder(playerStats))
                running.highestBidderLeft();
        }

        if (!disconnected)
            player.sendMessage(Utils.translate("&cYou have missed the opportunity of a lifetime."));
    }

    public boolean isInEvent(PlayerStats playerStats)
    {
        return isRunning() && running.inEvent(playerStats);
    }

    public boolean isRunning()
    {
        return running != null;
    }

    public BlackMarketEvent getRunningEvent() { return running; }

}
