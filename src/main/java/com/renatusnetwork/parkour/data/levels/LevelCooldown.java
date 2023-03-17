package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;

import java.util.Arrays;
import java.util.Map;

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

        Integer[] keys = (Integer[]) Parkour.getSettingsManager().cooldownModifiers.keySet().toArray();

        // do backwards iteration for algorithm
        for (int i = keys.length - 1; i >= 0; i--)
        {
            // if less than completions, calc modifier
            if (keys[i].intValue() < completionsCount)
                // calc from 1.00 - modifier
                modifier = (float) (1.00 - Parkour.getSettingsManager().cooldownModifiers.get(keys[i].intValue()));
        }
    }

    public Level getLevel()
    {
        return level;
    }
}
