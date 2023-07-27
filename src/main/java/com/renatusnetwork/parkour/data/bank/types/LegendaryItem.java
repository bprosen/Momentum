package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class LegendaryItem extends BankItem
{
    public LegendaryItem(BankItemType type, String displayName)
    {
        super(type, Parkour.getSettingsManager().legendaryMinimumBid, displayName, "&4&lLegendary");
        setMinimumNextBidRate(Parkour.getSettingsManager().legendaryNextBidMinimum);
    }
}