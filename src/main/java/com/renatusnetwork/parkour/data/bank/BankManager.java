package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.types.BankItem;
import com.renatusnetwork.parkour.data.bank.types.BankItemType;
import com.renatusnetwork.parkour.data.bank.types.BrilliantItem;
import com.renatusnetwork.parkour.data.bank.types.RadiantItem;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;

import java.util.HashMap;

public class BankManager
{
    private HashMap<BankItemType, BankItem> items;

    public BankManager()
    {
        items = new HashMap<>();

        // get random nums
        int radiantNum = BankYAML.chooseBankItem(BankItemType.RADIANT);
        int brilliantNum = BankYAML.chooseBankItem(BankItemType.BRILLIANT);
        int legendaryNum = BankYAML.chooseBankItem(BankItemType.LEGENDARY);

        // add into map as polymorphic
        items.put(BankItemType.RADIANT,
                new RadiantItem(BankItemType.RADIANT, BankYAML.getTitle(BankItemType.RADIANT, radiantNum)));
        items.put(BankItemType.BRILLIANT,
                new BrilliantItem(BankItemType.BRILLIANT, BankYAML.getTitle(BankItemType.BRILLIANT, brilliantNum)));
        items.put(BankItemType.LEGENDARY,
                new RadiantItem(BankItemType.LEGENDARY, BankYAML.getTitle(BankItemType.LEGENDARY, legendaryNum)));
    }

    public BankItem getItem(BankItemType type)
    {
        return items.get(type);
    }
    public boolean isType(String typeName)
    {
        boolean result = false;

        // try catch to determine
        try
        {
            BankItemType type = BankItemType.valueOf(typeName);
            result = true;
        }
        catch (IllegalArgumentException ignored)
        {}

        return result;
    }

    public void bid(PlayerStats playerStats, int bidAmount, BankItemType type)
    {
        BankItem bankItem = items.get(type);

        // make sure they do not bid on themselves
        if (!bankItem.hasCurrentHolder() || !bankItem.getCurrentHolder().equalsIgnoreCase(playerStats.getPlayerName()))
        {
            int amountToRemove = bidAmount;

            // bid amount - their prev bid to adjust
            if (bankItem.hasBid(playerStats.getPlayerName()))
                amountToRemove -= bankItem.getBid(playerStats.getPlayerName());

            if (playerStats.getCoins() >= bankItem.getNextBidMinimum())
            {
                Parkour.getStatsManager().removeCoins(playerStats, amountToRemove); // remove coins
                bankItem.setCurrentHolder(playerStats.getPlayerName()); // update current holder
                bankItem.addBid(playerStats, bidAmount); // update in cache
                bankItem.broadcastNewBid(playerStats, bidAmount); // broadcast bid
                BankDB.updateBid(playerStats, type, bidAmount); // update in db
            }
            else
            {
                playerStats.getPlayer().sendMessage(Utils.translate("&cYou do not have enough coins to raise the bid to &6" + Utils.formatNumber(bidAmount) + " &eCoins"));
            }
        }
        else
        {
            playerStats.getPlayer().sendMessage(Utils.translate("&cYou cannot bid on yourself"));
        }
    }
}
