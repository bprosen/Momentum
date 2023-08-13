package com.renatusnetwork.parkour.data.blackmarket;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.sk89q.commandbook.locations.TeleportSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

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

        Parkour.getPluginLogger().info("Black Market items loaded: " + artifacts.size());
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

            String prefix = Parkour.getSettingsManager().blackmarket_message_prefix;

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
                                    player.sendMessage(Utils.translate("&8You have &c" + timerCount + " minutes..."));
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

            if (running.hasHighestBidder())
            {
                running.broadcastToPlayers(Utils.translate("&c" + running.getHighestBidder().getPlayer().getDisplayName() +  " &8has earned the &c" + running.getBlackMarketItem().getTitle()));
                running.broadcastToPlayers(Utils.translate("&8for a staggering &6" + Utils.formatNumber(running.getHighestBid()) + " &eCoins&8."));
            }

            running.broadcastToPlayers(Utils.translate("&8Get out before the staff find you."));
            running.broadcastToPlayers(Utils.translate("&cUntil our next sale..."));
            running.broadcastToPlayers(Utils.translate("&8&m-------------------------------"));

            running.end();
            runEndingSchedulers(false);
            running = null;
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to end a Black Market event with none in-progress");
        }
    }

    public void forceEnd()
    {
        if (isRunning())
        {
            running.end();
            runEndingSchedulers(true);
            running = null;
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to force end a Black Market event with none in-progress");
        }
    }

    private void runEndingSchedulers(boolean forceEnded)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (PlayerStats playerStats : running.getPlayers())
                {
                    playerStats.getPlayer().teleport(Parkour.getLocationManager().getLobbyLocation()); // teleport to spawn

                    if (running.hasHighestBidder())
                    {
                        String display = running.getHighestBidder().getPlayer().getDisplayName();
                        TitleAPI.sendTitle(playerStats.getPlayer(), 0, 20, 20,
                                Utils.translate("&8&lBlack Market"), Utils.translate("&c" + display + " &7won!"));
                    }
                    playerStats.setBlackMarket(false);
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 10 * 20); // teleport them all 10 seconds later

        if (!forceEnded)
        {
            // ending schedulers, we only do these if the event ended normally
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
    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        if (isRunning())
        {
            running.increaseBid(playerStats, bid);
            running.broadcastToPlayers(Utils.translate(
                    Parkour.getSettingsManager().blackmarket_message_prefix + " &c" + playerStats.getPlayer().getDisplayName() + " &8has increased the bid to &6" + Utils.formatNumber(bid) + " &eCoins"
            ));
            running.broadcastToPlayers(Utils.translate(Parkour.getSettingsManager().blackmarket_message_prefix + " &8The next bid starts at &6" + Utils.formatNumber(running.getNextMinimumBid()) + " &eCoins"));
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
                player.sendMessage(Utils.translate(Parkour.getSettingsManager().blackmarket_message_prefix + " &8You're too late... the risk is too high to take you in."));
                player.sendMessage(Utils.translate(Parkour.getSettingsManager().blackmarket_message_prefix + " &cCome early next time."));
            }
            else
            {
                running.addPlayer(playerStats);
                playerStats.getPlayer().teleport(Parkour.getLocationManager().get(Parkour.getSettingsManager().blackmarket_tp_loc));
                playerStats.setBlackMarket(true);
            }
        }
        else
            player.sendMessage(Utils.translate(Parkour.getSettingsManager().blackmarket_message_prefix + " &cThere is nothing to see here..."));
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
            player.sendMessage(Utils.translate(Parkour.getSettingsManager().blackmarket_message_prefix + " &cYou have missed the opportunity of a lifetime."));
    }

    public boolean isRunning()
    {
        return running != null;
    }

    public BlackMarketEvent getRunningEvent() { return running; }

}
