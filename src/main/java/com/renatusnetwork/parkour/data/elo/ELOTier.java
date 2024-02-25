package com.renatusnetwork.parkour.data.elo;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;

public class ELOTier
{
    private String name;
    private String title;
    private int requiredELO;
    private String nextELOTier;

    public ELOTier(String name)
    {
        this.name = name;
    }

    public ELOTier(String name, String title, int requiredELO, String nextELOTier)
    {
        this.name = name;
        this.title = title;
        this.requiredELO = requiredELO;
        this.nextELOTier = nextELOTier;
    }

    public String getTitle()
    {
        return title;
    }

    public String getFormattedTitle()
    {
        return Utils.translate(title);
    }

    public int getRequiredELO()
    {
        return requiredELO;
    }

    public ELOTier getNextELOTier()
    {
        return Parkour.getELOTiersManager().get(nextELOTier);
    }
}
