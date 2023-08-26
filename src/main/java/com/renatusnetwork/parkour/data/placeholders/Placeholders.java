package com.renatusnetwork.parkour.data.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion
{
    @Override
    public @NotNull String getIdentifier()
    {
        return "rn-parkour";
    }

    @Override
    public @NotNull String getAuthor()
    {
        return "xxBen";
    }

    @Override
    public @NotNull String getVersion()
    {
        return "2.0.0";
    }

    @Override
    public @NotNull String onPlaceholderRequest(Player player, String placeholder)
    {
        if (!placeholder.contains("_"))
            Bukkit.broadcastMessage(placeholder);

        String type = placeholder.substring(0, placeholder.indexOf("_"));
        String placeholderString = placeholder.substring(placeholder.indexOf("_") + 1);

        switch (type.toLowerCase())
        {
            case LBPlaceholders.LB_PREFIX:
                return LBPlaceholders.processPlaceholder(placeholderString);
            case GeneralPlaceholders.GENERAL_PREFIX:
                return GeneralPlaceholders.processPlaceholder(placeholderString);
            case BankPlaceholders.BANK_PREFIX:
                return BankPlaceholders.processPlaceholder(placeholderString);
            case PlayerPlaceholders.PLAYER_PREFIX:
                return PlayerPlaceholders.processPlaceholder(player, placeholderString);
            case RewardsPlaceholders.REWARDS_PREFIX:
                return RewardsPlaceholders.processPlaceholder(player, placeholderString);
        }
        return "";
    }
}
