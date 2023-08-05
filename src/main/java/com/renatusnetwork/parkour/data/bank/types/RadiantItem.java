package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankYAML;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class RadiantItem extends BankItem
{
    public RadiantItem()
    {
        super(BankItemType.RADIANT);
        setFormattedType("&eRadiant");
        setMinimumLock(Parkour.getSettingsManager().radiant_lock_minimum);
    }

    @Override
    public void calcNextBid()
    {
        long total = getTotalBalance();

        if (total < Parkour.getSettingsManager().radiant_minimum_bid)
            setNextBid(Parkour.getSettingsManager().radiant_minimum_bid);
        else
            setNextBid((int) (10 * Math.sqrt(total)));
    }
}
