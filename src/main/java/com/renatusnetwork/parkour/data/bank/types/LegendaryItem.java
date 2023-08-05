package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class LegendaryItem extends BankItem
{
    public LegendaryItem()
    {
        super(BankItemType.LEGENDARY);
        setFormattedType("&4Legendary");
        setMinimumLock(Parkour.getSettingsManager().legendary_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Parkour.getSettingsManager().legendary_minimum_bid)
            setNextBid(Parkour.getSettingsManager().legendary_minimum_bid);
        else
            setNextBid((int) (30 * Math.sqrt(total)));
    }
}
