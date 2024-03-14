package com.renatusnetwork.momentum.data.modifiers.discounts;

import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;

public abstract class Discount extends Modifier
{
    private float discount;

    public Discount(ModifierType type, String name, String title, float discount)
    {
        super(type, name, title);

        this.discount = discount;
    }

    public void setDiscount(float discount) { this.discount = discount; }

    public float getDiscount()
    {
        return discount;
    }
}
