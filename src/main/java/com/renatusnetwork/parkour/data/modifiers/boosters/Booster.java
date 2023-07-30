package com.renatusnetwork.parkour.data.modifiers.boosters;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.ModifiersYAML;

public abstract class Booster extends Modifier
{
    private float multiplier;

    public Booster(ModifierTypes type, String name)
    {
        super(type, name);

        this.multiplier = ModifiersYAML.getMultiplier(name);
    }

    public float getMultiplier()
    {
        return multiplier;
    }
}
