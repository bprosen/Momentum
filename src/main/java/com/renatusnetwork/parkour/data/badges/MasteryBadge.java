package com.renatusnetwork.parkour.data.badges;

import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;

import java.util.HashSet;

public class MasteryBadge extends Badge
{
    private HashSet<Level> levels;

    public MasteryBadge(String name)
    {
        super(name, BadgeType.LEVEL_MASTERY);
    }

    public void addLevel(Level level)
    {
        levels.add(level);
    }

    @Override
    public boolean hasAccess(PlayerStats playerStats)
    {
        for (Level level : levels)
            if (!playerStats.hasCompleted(level))
                return false;

        return true;
    }
}
