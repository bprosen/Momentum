package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BrilliantItem extends BankItem
{
    public BrilliantItem(BankItemType type, String displayName)
    {
        super(type, Parkour.getSettingsManager().brilliantMinimumBid, displayName);
        setMinimumNextBidRate(Parkour.getSettingsManager().brilliantNextBidMinimum);
    }

    @Override
    public void broadcastNewBid(PlayerStats playerStats, int bidAmount)
    {
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &d&lNEW &e&lBRILLIANT &d&lBANK BID"));
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate(
                " &d" + playerStats.getPlayer().getDisplayName() + " &7put &6" + Utils.formatNumber(bidAmount) + " &eCoins &7for " + getDisplayName()
        ));
        Bukkit.broadcastMessage(Utils.translate("   " + getDisplayName() + " &7total is now &6" + Utils.formatNumber(getCurrentTotal()) + " &eCoins &7in the &d&lBank"));
        Bukkit.broadcastMessage(Utils.translate("   &7Bid &6" + Utils.formatNumber(getMinimumNextBid()) + " &eCoins &7at &c/spawn &7to overtake " + playerStats.getPlayer().getDisplayName()));
        Bukkit.broadcastMessage(Utils.translate("&d&m----------------------------------------"));
    }
}
