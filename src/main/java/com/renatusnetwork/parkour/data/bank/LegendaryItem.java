package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class LegendaryItem extends BankItem
{
    public LegendaryItem(BankItemType type, String displayName)
    {
        super(type, displayName);
        setMinimumNextBidRate(Parkour.getSettingsManager().legendaryNextBidMinimum);
    }

    @Override
    public void broadcastNewBid(PlayerStats playerStats, int bidAmount)
    {

        Bukkit.broadcastMessage(Utils.translate("&d&m-----------------------------------------------------------\n"));
        Bukkit.broadcastMessage(Utils.translate(" &d&lNEW &4&lLEGENDARY &d&lBANK BID"));
        Bukkit.broadcastMessage(Utils.translate(
                " &d" + playerStats.getPlayer().getDisplayName() + " &7put &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + getDisplayName()
        ));
        Bukkit.broadcastMessage(Utils.translate(" " + getDisplayName() + " &7total is now &6" + Utils.formatNumber(getCurrentTotal()) + "&eCoins &7in the &d&lBank"));
        Bukkit.broadcastMessage(Utils.translate(" \n&7Next bid starts at &6" + Utils.formatNumber(getMinimumNextBid()) + " &eCoins"));
        Bukkit.broadcastMessage(Utils.translate(" &7Type &c/bank bid " + ChatColor.stripColor(getDisplayName()) + " &c(at least " + Utils.formatNumber(getNextBidMinimum())
                + ") &7to overtake &c" + playerStats.getPlayer().getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate("\n&d&m----------------------------------------------------------"));
    }
}
