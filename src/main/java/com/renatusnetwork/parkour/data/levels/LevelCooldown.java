package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Bukkit;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class LevelCooldown
{
    private int completionsCount;
    private Level level;
    private float modifier;

    public LevelCooldown(Level level)
    {
        this.level = level;
        this.modifier = 1.00f;

        addCompletion();
    }

    public float getModifier()
    {
        return modifier;
    }

    public void addCompletion()
    {
        completionsCount++;

        Object[] keys = Parkour.getSettingsManager().cooldownModifiers.keySet().toArray();
        boolean done = false;

        // do backwards iteration for algorithm
        for (int i = keys.length - 1; i >= 0 && !done; i--)
        {
            int value = ((Integer) keys[i]).intValue();

            // if less than completions, calc modifier
            if (value <= completionsCount)
            {
                // calc from 1.00 - modifier
                modifier = (float) (1.00 - Parkour.getSettingsManager().cooldownModifiers.get(value));
                done = true;
            }
        }
    }

    public Level getLevel()
    {
        return level;
    }
}
