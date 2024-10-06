package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

public class LegendaryItem extends BankItem {

    public LegendaryItem() {
        super(BankItemType.LEGENDARY);
        setFormattedType("&4Legendary");
    }

    @Override
    public void calcNextBid(int previous) {
        long total = getTotalBalance();
        int calculatedAmount = (int) (ThreadLocalRandom.current().nextDouble(29.5, 30.5) * Math.sqrt(total));

        setNextBid(Math.max(calculatedAmount, previous));
    }
}
