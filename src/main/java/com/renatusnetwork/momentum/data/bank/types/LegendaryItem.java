package com.renatusnetwork.momentum.data.bank.types;

import com.renatusnetwork.momentum.Momentum;

public class LegendaryItem extends BankItem
{
    public LegendaryItem()
    {
        super(BankItemType.LEGENDARY);
        setFormattedType("&4Legendary");
        setMinimumLock(Momentum.getSettingsManager().legendary_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Momentum.getSettingsManager().legendary_minimum_bid)
            setNextBid(Momentum.getSettingsManager().legendary_minimum_bid);
        else
            setNextBid((int) (30 * Math.sqrt(total)));
    }
}
