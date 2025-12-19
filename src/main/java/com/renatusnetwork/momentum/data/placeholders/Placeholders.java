package com.renatusnetwork.momentum.data.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "momentum";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xxBen";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0.0";
    }

    @Override
    public @NotNull String onPlaceholderRequest(Player player, String placeholder) {
        String type = placeholder.substring(0, placeholder.indexOf("_"));
        String placeholderString = placeholder.substring(placeholder.indexOf("_") + 1);

        switch (type.toLowerCase()) {
            case LBPlaceholders.LB_PREFIX:
                return LBPlaceholders.processPlaceholder(placeholderString);
            case GeneralPlaceholders.GENERAL_PREFIX:
                return GeneralPlaceholders.processPlaceholder(placeholderString);
            case PlayerPlaceholders.PLAYER_PREFIX:
                return PlayerPlaceholders.processPlaceholder(player, placeholderString);
            case RewardsPlaceholders.REWARDS_PREFIX:
                return RewardsPlaceholders.processPlaceholder(player, placeholderString);
        }
        return "";
    }
}
