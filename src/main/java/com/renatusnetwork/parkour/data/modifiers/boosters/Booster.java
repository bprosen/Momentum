package com.renatusnetwork.parkour.data.modifiers.boosters;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifiersYAML;

public abstract class Booster extends Modifier
{
    private float factor;

    public Booster(String name)
    {
        super(name);

        this.factor = ModifiersYAML.getFactor(name);
    }

    public float getFactor()
    {
        return factor;
    }
}
