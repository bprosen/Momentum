package com.renatusnetwork.parkour.data.blackmarket;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
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
                            beginEvent();
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
                        // reminder for players to join
                        Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));
                        Bukkit.broadcastMessage(Utils.translate("&8&lBLACK MARKET"));
                        Bukkit.broadcastMessage(Utils.translate(""));
                        Bukkit.broadcastMessage(Utils.translate("&8Come to the hollow depths of &c/spawn"));
                        Bukkit.broadcastMessage(Utils.translate("&8for a risky trade of illegal wares."));
                        Bukkit.broadcastMessage(Utils.translate("&8You have &c" + timerCount + " minutes..."));
                        Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));

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
            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));
            Bukkit.broadcastMessage(Utils.translate("&8&lBLACK MARKET"));
            Bukkit.broadcastMessage(Utils.translate(""));
            Bukkit.broadcastMessage(Utils.translate("&c" + running.getHighestBidder().getPlayer().getDisplayName() +  " &8has earned the &c" + running.getBlackMarketItem().getDisplayName()));
            Bukkit.broadcastMessage(Utils.translate("&8for a staggering &6" + Utils.formatNumber(running.getHighestBid()) + " &eCoins&8."));
            Bukkit.broadcastMessage(Utils.translate("&8Get out before the staff find you."));
            Bukkit.broadcastMessage(Utils.translate("&cUntil our next sale..."));
            Bukkit.broadcastMessage(Utils.translate("&8&m-------------------------------"));

            // TODO: reward here
            running = null;
        }
        else
        {
            Parkour.getPluginLogger().info("Tried to end a Black Market event with none in-progress");
        }
    }

    private void beginEvent()
    {

    }

    public void increaseBid(PlayerStats playerStats, int bid)
    {
        if (isRunning())
        {
            running.increaseBid(playerStats, bid);
            running.broadcastToPlayers(Utils.translate(
                    "&c" + playerStats.getPlayer().getDisplayName() + " &8has increased the bid to &6" + Utils.formatNumber(bid) + " &eCoins"
            ));
        }
    }

    public void playerJoined(PlayerStats playerStats)
    {
        if (isRunning())
        {
            // if the event is still waiting for players
            if (!inPreperation)
            {
                Player player = playerStats.getPlayer();

                player.sendMessage(Utils.translate("&8You're too late... the risk is too high to take you in."));
                player.sendMessage(Utils.translate("&cCome early next time."));
            }
            else
                running.addPlayer(playerStats);
        }
    }

    public void playerLeft(PlayerStats playerStats)
    {
        if (isRunning())
        {
            running.removePlayer(playerStats);

            // remove highest bidder
            if (running.isHighestBidder(playerStats))
                running.highestBidderLeft();
        }
    }

    public boolean isRunning()
    {
        return running != null;
    }
}
