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

        chooseType(BankItemType.RADIANT);
        chooseType(BankItemType.BRILLIANT);
        chooseType(BankItemType.LEGENDARY);
    }

    public BankItem getItem(BankItemType type)
    {
        return items.get(type);
    }

    private void chooseType(BankItemType type)
    {
        int num = BankYAML.chooseBankItem(type);
        String title = BankYAML.getTitle(type, num);

        items.put(type, new BankItem(type, title)); // add to items
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
