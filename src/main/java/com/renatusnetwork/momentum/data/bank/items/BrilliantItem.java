package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

public class BrilliantItem extends BankItem {

    public BrilliantItem() {
        super(BankItemType.BRILLIANT);
        setFormattedType("&aBrilliant");
    }

    @Override
    public void calcNextBid(int previous) {
        long total = getTotalBalance();
        int calculatedAmount = (int) (ThreadLocalRandom.current().nextDouble(19.5, 20.5) * Math.sqrt(total));

        setNextBid(Math.max(calculatedAmount, previous));
    }
}
