package com.renatusnetwork.parkour.data.elo;

import com.renatusnetwork.parkour.data.stats.PlayerStats;

import java.util.HashMap;

public class ELOTiersManager
{
    private HashMap<String, ELOTier> tiers;

    public ELOTiersManager()
    {
        this.tiers = new HashMap<>();
    }

    public ELOTier get(String name)
    {
        return tiers.get(name);
    }

    public ELOTier getNextELOTier(ELOTier tier)
    {
        return tier != null ? tier.getNextELOTier() : null;
    }

    public ELOTier translate(int elo)
    {
        for (ELOTier tier : tiers.values())
            if (tier.getRequiredELO() <= elo)
            {
                ELOTier next = tier.getNextELOTier();

                if (next == null || next.getRequiredELO() > elo)
                    return tier;
            }

        return null;
    }

    public void processELOChange(PlayerStats playerStats)
    {
        int newELO = playerStats.getELO();
        ELOTier currentTier = playerStats.getELOTier();
        ELOTier nextTier = currentTier.getNextELOTier();

        if (nextTier != null)
        {
            // means next is last tier
            if (nextTier.getNextELOTier() == null)
            {
                // TODO: find last position in list of top 10, and check if they have passed them
            }
            else if (nextTier.getRequiredELO() <= newELO)
            {
                // TODO: level up tier, otherwise no change
            }
        }
        else
        {
            // TODO: they are currently in legend, update min if they are last in line in legend
        }
    }
}
