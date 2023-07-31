package com.renatusnetwork.parkour.data.modifiers.discounts;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.ModifiersYAML;

public abstract class Discount extends Modifier
{
    private float discount;

    public Discount(ModifierTypes type, String name)
    {
        super(type, name);

        this.discount = ModifiersYAML.getDiscount(name);
    }

    public float getDiscount()
    {
        return discount;
    }
}
