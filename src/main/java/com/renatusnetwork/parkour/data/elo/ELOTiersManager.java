package com.renatusnetwork.parkour.data.elo;
import java.util.HashMap;

public class ELOTiersManager
{
    private HashMap<String, ELOTier> tiers;

    public ELOTiersManager()
    {
        this.tiers = ELOTierDB.getTiers();
    }

    public void create(String name)
    {
        tiers.put(name, new ELOTier(name));
        ELOTierDB.create(name);
    }

    public ELOTier get(String name)
    {
        return tiers.get(name);
    }

    public void updateTitle(ELOTier tier, String title)
    {
        tier.setTitle(title);
        ELOTierDB.updateTitle(tier.getName(), title);
    }

    public void updateRequiredELO(ELOTier tier, int requiredELO)
    {
        tier.setRequiredELO(requiredELO);
        ELOTierDB.updateRequiredELO(tier.getName(), requiredELO);
    }

    public void updateNextELOTier(ELOTier tier, String nextELOTier)
    {
        tier.setNextELOTier(nextELOTier);
        ELOTierDB.updateNextTier(tier.getName(), nextELOTier);
    }

    public void updatePreviousELOTier(ELOTier tier, String previousELOTier)
    {
        tier.setPreviousELOTier(previousELOTier);
        ELOTierDB.updatePreviousTier(tier.getName(), previousELOTier);
    }
}
