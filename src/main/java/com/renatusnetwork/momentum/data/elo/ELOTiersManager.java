package com.renatusnetwork.momentum.data.elo;

import java.util.*;

public class ELOTiersManager {

    private List<ELOTier> orderedTiers;
    private HashMap<String, ELOTier> tiers;

    public ELOTiersManager() {
        this.tiers = ELOTierDB.getTiers();
        this.orderedTiers = new ArrayList<>(tiers.values());
        Collections.sort(this.orderedTiers);
    }

    public void create(String name) {
        ELOTier tier = new ELOTier(name);
        tiers.put(name, tier);
        orderedTiers.add(tier);
        Collections.sort(orderedTiers);
        ELOTierDB.create(name);
    }

    public List<ELOTier> getAll() {
        return orderedTiers;
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
        Collections.sort(orderedTiers);
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

    // one pitfall is this assumes that elo tiers are linked in the same order as if their required elo's are in ascending order
    // there's no reason for them not to be the same, but it is an assumption to mention
    public ELOTier calculateELOTierDirectly(int elo) {
        // simple binary search since elo tiers are sorted by required elo
        ELOTier dummyTier = new ELOTier("");
        dummyTier.setRequiredELO(elo);

        int i = Arrays.binarySearch(orderedTiers.toArray(new ELOTier[0]), dummyTier);

        if (i >= 0) {
            return orderedTiers.get(i);
        }

        // if there is no exact match, Arrays#binarySearch returns `-(insertionPoint) - 1`
        // so to get the element before the insertion point (the floor index),
        // just solve for the insertion point and subtract 1 yielding `-i - 2`
        int floorIndex = Math.max(0, -i - 2); // if the floor index is less than 0, then an elo less than the first elo tier was supplied
        return orderedTiers.get(floorIndex);
    }
}
