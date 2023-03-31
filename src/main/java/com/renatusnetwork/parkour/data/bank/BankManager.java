package com.renatusnetwork.parkour.data.bank;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;

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
        items.put(BankItemType.RADIANT, new RadiantItem(BankItemType.RADIANT, BankYAML.getTitle(BankItemType.RADIANT, radiantNum)));
        items.put(BankItemType.BRILLIANT, new BrilliantItem(BankItemType.BRILLIANT, BankYAML.getTitle(BankItemType.BRILLIANT, brilliantNum)));
        items.put(BankItemType.LEGENDARY, new RadiantItem(BankItemType.LEGENDARY, BankYAML.getTitle(BankItemType.LEGENDARY, legendaryNum)));
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

        int amountToRemove = bidAmount;

        // bid amount - their prev bid to adjust
        if (bankItem.hasBid(playerStats.getPlayerName()))
            amountToRemove -= bankItem.getBid(playerStats.getPlayerName());

        Parkour.getStatsManager().removeCoins(playerStats, amountToRemove); // remove coins
        bankItem.setCurrentHolder(playerStats.getPlayerName()); // update current holder
        bankItem.addBid(playerStats, bidAmount); // update in cache
        bankItem.broadcastNewBid(playerStats, bidAmount); // broadcast bid
        BankDB.updateBid(playerStats, type, bidAmount); // update in db
    }
}
