package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class BrilliantItem extends BankItem
{
    public BrilliantItem()
    {
        super(BankItemType.BRILLIANT);
        setFormattedType("&aBrilliant");
        setMinimumLock(Parkour.getSettingsManager().brilliant_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Parkour.getSettingsManager().brilliant_minimum_bid)
            setNextBid(Parkour.getSettingsManager().brilliant_minimum_bid);
        else
            setNextBid((int) (20 * Math.sqrt(total)));
    }
}
