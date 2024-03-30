package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

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
        int minimum = Momentum.getSettingsManager().brilliant_minimum_bid;
        int calculatedAmount = (int) (total + ((int) (ThreadLocalRandom.current().nextInt(8, 13) * Math.sqrt(total))));

        setNextBid(Math.max(calculatedAmount, minimum));
    }
}
