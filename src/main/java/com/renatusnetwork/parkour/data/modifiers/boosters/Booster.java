package com.renatusnetwork.parkour.data.modifiers.boosters;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;

public abstract class Booster extends Modifier
{
    private float multiplier;

    public Booster(ModifierType type, String name, String title, float multiplier)
    {
        super(type, name, title);

        this.multiplier = multiplier;
    }

    public void setMultiplier(float multiplier) { this.multiplier = multiplier; }

    public float getMultiplier()
    {
        return multiplier;
    }
}
