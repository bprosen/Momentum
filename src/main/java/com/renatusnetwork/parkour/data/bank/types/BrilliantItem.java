package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;

public class BrilliantItem extends BankItem
{
    public BrilliantItem(BankItemType type, String displayName, Modifier modifier)
    {
        super(type, Parkour.getSettingsManager().brilliantMinimumBid, displayName, "&a&lBrilliant", modifier);
    }
}
