package com.renatusnetwork.parkour.data.levels;

public class LevelCooldown
{
    private int completionsCount;
    private Level level;
    private float modifier;

    public LevelCooldown(Level level)
    {
        this.level = level;
        this.modifier = 1.00f;
    }

    public void setModifier(float modifier)
    {
        this.modifier = modifier;
    }

    public float getModifier()
    {
        return modifier;
    }

    public void addCompletion()
    {
        completionsCount++;
    }

    public Level getLevel()
    {
        return level;
    }
}
