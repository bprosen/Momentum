package com.renatusnetwork.momentum.data.elo;

import com.renatusnetwork.momentum.Momentum;

import java.util.*;

public class ELOTiersManager {

    private HashMap<String, ELOTier> tiers;

    public ELOTiersManager() {
        this.tiers = ELOTierDB.getTiers();
    }

    public void create(String name) {
        tiers.put(name, new ELOTier(name));
        ELOTierDB.create(name);
    }

    public ELOTier get(String name) {
        return tiers.get(name);
    }

    public void updateTitle(ELOTier tier, String title) {
        tier.setTitle(title);
        ELOTierDB.updateTitle(tier.getName(), title);
    }

    public void updateRequiredELO(ELOTier tier, int requiredELO) {
        tier.setRequiredELO(requiredELO);
        ELOTierDB.updateRequiredELO(tier.getName(), requiredELO);
    }

    public void updateNextELOTier(ELOTier tier, String nextELOTier) {
        tier.setNextELOTier(nextELOTier);
        ELOTierDB.updateNextTier(tier.getName(), nextELOTier);
    }

    public void updatePreviousELOTier(ELOTier tier, String previousELOTier) {
        tier.setPreviousELOTier(previousELOTier);
        ELOTierDB.updatePreviousTier(tier.getName(), previousELOTier);
    }

    public void deleteELOTier(ELOTier tier) {
        ELOTier next = tier.getNextELOTier();
        ELOTier prev = tier.getPreviousELOTier();

        if (next != null && prev != null) {
            prev.setNextELOTier(next.getName());
            next.setPreviousELOTier(prev.getName());
        } else if (next != null) {
            next.setPreviousELOTier(null);
        } else if (prev != null) {
            prev.setNextELOTier(null);
        }

        tiers.remove(tier.getName());
        ELOTierDB.deleteTier(tier.getName());
    }

    public ELOTier calculateELOTierDirectly(int elo) {
        ELOTier tier = tiers.get(Momentum.getSettingsManager().default_elo_tier);
        ELOTier next = tier.getNextELOTier();

        // if for some reason the argument elo is lower than the lowest tier
        if (elo <= tier.getRequiredELO()) {
            return tier;
        }

        while (next != null) {
            if (elo >= tier.getRequiredELO() && elo < next.getRequiredELO()) {
                return tier;
            }

            tier = next;
            next = tier.getNextELOTier();
        }

        // argument is higher than the highest tier
        // or lower than the lowest tier
        return tier;
    }
}
