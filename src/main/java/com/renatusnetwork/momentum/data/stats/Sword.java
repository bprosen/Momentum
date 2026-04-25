package com.renatusnetwork.momentum.data.stats;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.ranks.Rank;
import org.bukkit.inventory.ItemStack;

public class Sword {
    private ItemStack swordItem;
    private int requiredPrestige;
    private Rank requiredRank;

    public Sword(ItemStack swordItem, int requiredPrestige, Rank requiredRank) {
        this.swordItem = swordItem;
        this.requiredPrestige = requiredPrestige;
        this.requiredRank = requiredRank;
    }

    public ItemStack getSwordItem() {
        return swordItem;
    }

    public boolean hasAccess(PlayerStats playerStats) {
        if (requiredPrestige > playerStats.getPrestiges()) {
            return false;
        }

        return requiredRank != null && Momentum.getRanksManager().isPastOrAtRank(playerStats, requiredRank);
    }
}
