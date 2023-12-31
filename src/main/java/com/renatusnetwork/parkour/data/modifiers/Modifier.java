package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.modifiers.discounts.Discount;

public abstract class Modifier
{
    private ModifierType type;
    private String name;
    private String title;

    public Modifier(ModifierType type, String name, String title)
    {
        this.type = type;
        this.name = name;
        this.title = title;
    }

    public ModifierType getType() { return type; }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public boolean isBonus() { return this instanceof Bonus; }

    public boolean isBooster() { return this instanceof Booster; }

    public boolean isDiscount() { return this instanceof Discount; }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean equals(Modifier modifier)
    {
        return modifier.getName().equals(name);
    }
}
