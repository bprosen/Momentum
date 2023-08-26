package com.renatusnetwork.parkour.data.placeholders;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.entity.Player;

public class GeneralPlaceholders
{
    public static final String GENERAL_PREFIX = "general";

    public static String processPlaceholder(String placeholder)
    {
        // general placeholders
        if (placeholder.equals("global_completions"))
            return Utils.formatNumber(Parkour.getLevelManager().getTotalLevelCompletions());
        else if (placeholder.equals("total_coins"))
            return String.format("%,d", Parkour.getStatsManager().getTotalCoins());

        return "";
    }
}
