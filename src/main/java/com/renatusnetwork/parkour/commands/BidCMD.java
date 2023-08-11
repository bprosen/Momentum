package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketEvent;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BidCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            // either /bid or /bid (amount)
            if (a.length < 2)
            {
                BlackMarketManager blackMarketManager = Parkour.getBlackMarketManager();

                // if the event is actually running
                if (blackMarketManager.isRunning())
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    BlackMarketEvent runningEvent = blackMarketManager.getRunningEvent();

                    // if the player is in the event
                    if (runningEvent.inEvent(playerStats))
                    {
                        // make sure they dont try to bid early
                        if (runningEvent.isBiddingAllowed())
                        {
                            int amount = runningEvent.getNextMinimumBid();

                            // in the case of /bid (amount)
                            if (a.length == 1)
                            {
                                if (Utils.isInteger(a[0]))
                                    amount = Integer.parseInt(a[0]);
                                else
                                {
                                    player.sendMessage(Utils.translate("&4" + a[0] + " &cis not an integer"));
                                    return false; // return
                                }
                            }

                            // if the player actually has the amount to bid
                            if (playerStats.getCoins() >= amount)
                            {
                                // if the amount meets the minimum
                                if (amount >= runningEvent.getNextMinimumBid())
                                {
                                    // if they are not already the highest bidder
                                    if (!(runningEvent.hasHighestBidder() && runningEvent.getHighestBidder().equals(playerStats)))
                                    {
                                        // increase bid!
                                        blackMarketManager.increaseBid(playerStats, amount);
                                    }
                                    else
                                    {
                                        player.sendMessage(Utils.translate("&cYou cannot outbid yourself"));
                                    }
                                }
                                else
                                {
                                    player.sendMessage(Utils.translate("&cThat is not the minimum, it must be at least &6" + Utils.formatNumber(runningEvent.getNextMinimumBid()) + " &eCoins"));
                                }
                            }
                            else
                            {
                                player.sendMessage(Utils.translate("&cYou do not have &6" + Utils.formatNumber(amount) + " &eCoins"));
                            }
                        }
                        else
                        {
                            player.sendMessage(Utils.translate("&cBidding is not allowed yet"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&cYou are not in the event"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&cWhat are you trying to bid for?"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cInvalid usage, do &4/bid (amount) &cor &4/bid &c(bids the minimum"));
            }
        }
        return false;
    }
}
