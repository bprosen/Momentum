package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class RadiantItem extends BankItem
{
    public RadiantItem(BankItemType type, String displayName, Modifier modifier)
    {
        super(type, Parkour.getSettingsManager().radiantMinimumBid, displayName, "&e&lRadiant", modifier);
    }
}
