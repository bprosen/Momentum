package com.renatusnetwork.parkour.data.badges;

import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;

public abstract class Badge
{
    private String name;
    private String title;
    private BadgeType type;

    public Badge(String name, BadgeType type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public BadgeType getType()
    {
        return type;
    }

    public abstract boolean hasAccess(PlayerStats playerStats);
}
