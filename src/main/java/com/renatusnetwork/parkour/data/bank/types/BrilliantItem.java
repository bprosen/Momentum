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
        setNextBid(20 * (int) Math.sqrt(getTotalBalance()));
    }
}
