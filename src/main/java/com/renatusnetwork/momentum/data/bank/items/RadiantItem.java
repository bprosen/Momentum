package com.renatusnetwork.momentum.data.bank.items;

import com.renatusnetwork.momentum.Momentum;

import java.util.concurrent.ThreadLocalRandom;

public class RadiantItem extends BankItem {

    public RadiantItem() {
        super(BankItemType.RADIANT);
        setFormattedType("&eRadiant");
    }

    @Override
    public void calcNextBid(int previous) {
        long total = getTotalBalance();
        int calculatedAmount = (int) (ThreadLocalRandom.current().nextDouble(9.5, 10.5) * Math.sqrt(total));

        setNextBid(Math.max(calculatedAmount, previous));
    }
}
