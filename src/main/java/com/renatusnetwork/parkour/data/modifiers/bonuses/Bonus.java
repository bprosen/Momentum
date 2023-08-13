package com.renatusnetwork.parkour.data.modifiers.bonuses;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.ModifiersYAML;

public class Bonus extends Modifier
{
    private int bonus;

    public Bonus(ModifierTypes type, String name)
    {
        super(type, name);

        this.bonus = ModifiersYAML.getBonus(name);
    }

    public int getBonus()
    {
        return bonus;
    }
}
