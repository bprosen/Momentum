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
        setNextBid(30 * (int) Math.sqrt(getTotalBalance()));
    }
}
