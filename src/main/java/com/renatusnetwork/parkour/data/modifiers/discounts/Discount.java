package com.renatusnetwork.parkour.data.modifiers.discounts;

import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifiersYAML;

public class Discount extends Modifier
{
    private float discountPercentage;

    public Discount(String name)
    {
        super(name);

        this.discountPercentage = ModifiersYAML.getDiscountPercentage(name);
    }
}
