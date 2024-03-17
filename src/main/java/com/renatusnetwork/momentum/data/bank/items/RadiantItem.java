package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

public class RadiantItem extends BankItem
{
    public RadiantItem()
    {
        super(BankItemType.RADIANT);
        setFormattedType("&eRadiant");
        setMinimumLock(Momentum.getSettingsManager().radiant_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Momentum.getSettingsManager().radiant_minimum_bid)
            setNextBid(Momentum.getSettingsManager().radiant_minimum_bid);
        else
            setNextBid((int) (10 * Math.sqrt(total)));
    }
}
