package com.renatusnetwork.parkour.data.badges;

import com.renatusnetwork.parkour.data.stats.PlayerStats;

public class PermissionBadge extends Badge
{

    private String permission;

    public PermissionBadge(String name)
    {
        super(name, BadgeType.PERMISSION);
    }

    public String getPermission()
    {
        return permission;
    }

    @Override
    public boolean hasAccess(PlayerStats playerStats)
    {
        return playerStats.getPlayer().hasPermission(permission);
    }
}
