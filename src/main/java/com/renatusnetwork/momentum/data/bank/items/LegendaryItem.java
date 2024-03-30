package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

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
        int minimum = Momentum.getSettingsManager().legendary_minimum_bid;
        int calculatedAmount = (int) (total + ((int) (ThreadLocalRandom.current().nextInt(28, 33) * Math.sqrt(total))));

        setNextBid(Math.max(calculatedAmount, minimum));
    }
}
