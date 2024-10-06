package com.renatusnetwork.momentum.data.elo;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.Utils;

public class ELOTier {

    private String name;
    private String title;
    private int requiredELO;
    private String previousELOTier;
    private String nextELOTier;

    public ELOTier(String name) {
        this.name = name;
    }

    public ELOTier(String name, String title, int requiredELO, String previousELOTier, String nextELOTier) {
        this.name = name;
        this.title = title != null ? title : name;
        this.requiredELO = requiredELO;
        this.previousELOTier = previousELOTier;
        this.nextELOTier = nextELOTier;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormattedTitle() {
        return Utils.translate(title);
    }

    public int getRequiredELO() {
        return requiredELO;
    }

    public void setRequiredELO(int requiredELO) {
        this.requiredELO = requiredELO;
    }

    public ELOTier getNextELOTier() {
        return Momentum.getELOTiersManager().get(nextELOTier);
    }

    public ELOTier getPreviousELOTier() {
        return Momentum.getELOTiersManager().get(previousELOTier);
    }

    public void setNextELOTier(String nextELOTier) {
        this.nextELOTier = nextELOTier;
    }

    public void setPreviousELOTier(String previousELOTier) {
        this.previousELOTier = previousELOTier;
    }
}
