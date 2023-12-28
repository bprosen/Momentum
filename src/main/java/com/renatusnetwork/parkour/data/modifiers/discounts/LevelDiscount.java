package com.renatusnetwork.parkour.data.modifiers.discounts;

import com.renatusnetwork.parkour.data.modifiers.ModifierType;

public class LevelDiscount extends Discount
{
    public LevelDiscount(ModifierType type, String name, String title, float discount)
    {
        super(type, name, title, discount);
    }
}
