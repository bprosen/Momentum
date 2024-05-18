package com.renatusnetwork.momentum.data.blackmarket;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;
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

        Momentum.getPluginLogger().info("Black Market items loaded: " + artifacts.size());
    }

    private void runScheduler()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Calendar bankCalendar = Momentum.getSettingsManager().black_market_reset_calendar;
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(new Date());

                // if bankCalendar is BEFORE current, run blackmarket!
                if (bankCalendar.get(Calendar.DAY_OF_WEEK) == currentCalendar.get(Calendar.DAY_OF_WEEK) &&
                    bankCalendar.get(Calendar.HOUR_OF_DAY) == currentCalendar.get(Calendar.HOUR_OF_DAY))
                {
                    start();
                }
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 60 * 60, 20 * 60 * 60); // check every hour
    }
    public boolean start()
    {
        if (!isRunning())
        {
            running = new BlackMarketEvent(artifacts.get(ThreadLocalRandom.current().nextInt(artifacts.size())));

            inPreperation = true;

            String prefix = Momentum.getSettingsManager().blackmarket_message_prefix;

            // random messages for prep stage
            List<String> randomDialogue = new ArrayList<String>()
            {{
                add(Utils.translate(prefix + " Never speak of the auction..."));
                add(Utils.translate(prefix + " The first rule, a cardinal decree, is to never speak of the auction beyond these veiled walls..."));
                add(Utils.translate(prefix + " To wield the artifacts of the auction is to embrace the enigma of balance. For each power granted by an artifact, an equivalent price is exacted."));
                add(Utils.translate(prefix + " The unspoken command that governs all; never assume you know all the rules of the auction."));
                add(Utils.translate(prefix + " Anticipation is in the air, the auction is a clandestine dance where seekers of the extraordinary come to collide with fate."));
                add(Utils.translate(prefix + " Among the shadows, one must tread with caution."));
                add(Utils.translate(prefix + " These artifacts hold the essence of untold realms."));
                add(Utils.translate(prefix + " Embrace the consequences of your choices; every step taken, a web of destiny is woven."));
                add(Utils.translate(prefix + " You may find answers to questions you never thought to ask."));
                add(Utils.translate(prefix + " You hold the power to shape your destiny, but the artifacts hold the power to shape you."));
                add(Utils.translate(prefix + " Safety is but an illusion in the dance of shadows."));
            }};

            // plays to all online
            Utils.playSound(Sound.ENTITY_WITHER_SPAWN);

            // begin timer before starting event
            new BukkitRunnable()
            {
                int timerCount = 5;

                @Override
                public void run()
                {
                    if (!isRunning())
                        cancel();
                    else if (timerCount == 0)
                    {
                        cancel();
                        inPreperation = false;

                        // only start if met the minimum
                        if (running.getPlayerCount() >= Momentum.getSettingsManager().blackmarket_min_player_count)
                            running.start();
                        else
                        {
                            // cancel because not enough players
                            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));
                            Bukkit.broadcastMessage(Utils.translate("&8&lBLACK MARKET"));
                            Bukkit.broadcastMessage(Utils.translate(""));
                            Bukkit.broadcastMessage(Utils.translate("&8The risk isn't worth it..."));
                            Bukkit.broadcastMessage(Utils.translate("&8There isn't enough competition."));
                            Bukkit.broadcastMessage(Utils.translate("&c(Not enough players, need " + Momentum.getSettingsManager().blackmarket_min_player_count + ")"));
                            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));

                            running = null; // event shouldnt continue if there arent enough players
                        }
                    }
                    else
                    {
                        HashMap<String, PlayerStats> stats = Momentum.getStatsManager().getPlayerStats();
                        String random = randomDialogue.get(ThreadLocalRandom.current().nextInt(randomDialogue.size()));

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
                                    player.sendMessage(Utils.translate("&8You have &c" + timerCount + " minute" + (timerCount > 1 ? "s..." : "...")));
                                    player.sendMessage(Utils.translate("&8&m-------------------------------"));
                                }
                                else
                                {
                                    // random message for players
                                    stat.getPlayer().sendMessage(Utils.translate(random));
                                }
                            }
                        }
                        randomDialogue.remove(random);
                        timerCount--;
                    }
                }
            }.runTaskTimer(Momentum.getPlugin(), 0, 20 * 60);
            return true;
        }
        else
        {
            Momentum.getPluginLogger().info("Tried to start a Black Market event with one in-progress");
            return false;
        }
    }

    public boolean end()
    {
        if (isRunning())
        {
            running.broadcastToPlayers(Utils.translate("&8&m-------------------------------"));
            running.broadcastToPlayers(Utils.translate("&8&lBLACK MARKET"));
            running.broadcastToPlayers(Utils.translate(""));

            if (running.hasHighestBidder())
            {
                running.broadcastToPlayers(Utils.translate("&c" + running.getHighestBidder().getPlayer().getDisplayName() +  " &8has earned the &c" + running.getBlackMarketItem().getTitle()));
                running.broadcastToPlayers(Utils.translate("&8for a staggering &6" + Utils.formatNumber(running.getHighestBid()) + " &eCoins&8."));
            }

            running.broadcastToPlayers(Utils.translate("&8Get out before the staff find you."));
            running.broadcastToPlayers(Utils.translate("&cUntil our next sale..."));
            running.broadcastToPlayers(Utils.translate("&8&m-------------------------------"));

            running.end(false);
            runEndingSchedulers(false);

            return true;
        }
        else
        {
            Momentum.getPluginLogger().info("Tried to end a Black Market event with none in-progress");
            return false;
        }
    }

    public boolean forceEnd()
    {
        if (isRunning())
        {
            running.end(true);
            runEndingSchedulers(true);
            return true;
        }
        else
        {
            Momentum.getPluginLogger().info("Tried to force end a Black Market event with none in-progress");
            return false;
        }
    }

    private void runEndingSchedulers(boolean forceEnded)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (isRunning()) { // prevent clashing "/blackmarket end" in case cmd executed multiple times before 10 seconds
                    for (PlayerStats playerStats : running.getPlayers()) {
                        Player player = playerStats.getPlayer();
                        playerStats.teleport(Momentum.getLocationManager().getSpawnLocation(), false); // teleport to spawn

                        if (running.hasHighestBidder()) {
                            String display = running.getHighestBidder().getPlayer().getDisplayName();
                            playerStats.sendTitle("&8&lBlack Market", "&c" + display + " &7won!", 10, 20, 20);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 1.0F, 1.0F);
                        }
                        playerStats.setBlackMarket(false);
                    }
                    running = null;
                }
            }
        }.runTaskLater(Momentum.getPlugin(), 10 * 20); // teleport them all 10 seconds later

        if (!forceEnded)
        {
            // ending schedulers, we only do these if the event ended normally
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    // run final jackpot 5 mins later
                    Momentum.getBankManager().startJackpot();

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            // LOAD NEW BANK ITEMS!
                            Momentum.getBankManager().resetBank();
                        }
                    }.runTaskLater(Momentum.getPlugin(), (20 * Momentum.getSettingsManager().jackpot_length) + 20 * 60 * 5); // jackpot length + 5 minutes
                }
            }.runTaskLater(Momentum.getPlugin(), 20 * 60 * 5); // 5 mins later
        }
    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        if (isRunning())
        {
            running.increaseBid(playerStats, bid);
            running.broadcastToPlayers(Utils.translate(
                    Momentum.getSettingsManager().blackmarket_message_prefix + " &c" + playerStats.getPlayer().getDisplayName() + " &8has increased the bid to &6" + Utils.formatNumber(bid) + " &eCoins"
            ));
            running.broadcastToPlayers(Utils.translate(Momentum.getSettingsManager().blackmarket_message_prefix + " &8The next bid starts at &6" + Utils.formatNumber(running.getNextMinimumBid()) + " &eCoins"));
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
                player.sendMessage(Utils.translate(Momentum.getSettingsManager().blackmarket_message_prefix + " &8You're too late... the risk is too high to take you in."));
                player.sendMessage(Utils.translate(Momentum.getSettingsManager().blackmarket_message_prefix + " &cCome early next time."));
            }
            else
            {
                running.addPlayer(playerStats);
                player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 1.0F, 1.0F); // play noise
                playerStats.sendTitle("&8&lBlack Market", "&7We will be starting soon...", 20, 100, 20); // send title
                playerStats.teleport(Momentum.getLocationManager().get(Momentum.getSettingsManager().blackmarket_tp_loc), true);
                playerStats.setBlackMarket(true);
            }
        }
    }

    public void playerLeft(PlayerStats playerStats, boolean disconnected)
    {
        Player player = playerStats.getPlayer();

        if (isRunning())
        {
            running.removePlayer(playerStats);
            playerStats.setBlackMarket(false);

            // remove highest bidder
            if (running.isHighestBidder(playerStats))
                running.highestBidderLeft();
        }

        if (!disconnected)
            player.sendMessage(Utils.translate(Momentum.getSettingsManager().blackmarket_message_prefix + " &cYou have missed the opportunity of a lifetime."));
    }

    public boolean isRunning()
    {
        return running != null;
    }

    public BlackMarketEvent getRunningEvent() { return running; }

    public void shutdown()
    {
        if (isRunning())
        {
            running.end(true);

            for (PlayerStats playerStats : running.getPlayers())
                playerStats.teleport(Momentum.getLocationManager().getSpawnLocation(), false); // teleport to spawn
        }
    }
}
