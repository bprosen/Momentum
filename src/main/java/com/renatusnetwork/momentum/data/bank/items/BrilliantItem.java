package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

public class BrilliantItem extends BankItem
{
    public BrilliantItem()
    {
        super(BankItemType.BRILLIANT);
        setFormattedType("&aBrilliant");
        setMinimumLock(Momentum.getSettingsManager().brilliant_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Momentum.getSettingsManager().brilliant_minimum_bid)
            setNextBid(Momentum.getSettingsManager().brilliant_minimum_bid);
        else
            setNextBid((int) (20 * Math.sqrt(total)));
    }
}
