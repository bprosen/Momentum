package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

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
        int minimum = Momentum.getSettingsManager().radiant_minimum_bid;
        int calculatedAmount = (int) (total + ((int) (ThreadLocalRandom.current().nextInt(8, 13) * Math.sqrt(total))));

        setNextBid(Math.max(calculatedAmount, minimum));
    }
}
