package com.renatusnetwork.momentum.data.placeholders;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.Utils;

public class GeneralPlaceholders
{
    public static final String GENERAL_PREFIX = "general";

    public static String processPlaceholder(String placeholder)
    {
        // general placeholders
        if (placeholder.equals("global_completions"))
            return Utils.formatNumber(Momentum.getLevelManager().getTotalLevelCompletions());
        else if (placeholder.equals("total_coins"))
            return String.format("%,d", Momentum.getStatsManager().getTotalCoins());

        return "";
    }
}
