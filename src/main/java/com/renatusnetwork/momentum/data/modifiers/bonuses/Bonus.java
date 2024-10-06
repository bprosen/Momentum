package com.renatusnetwork.momentum.data.modifiers.bonuses;

import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;

public class Bonus extends Modifier {

    private int bonus;

    public Bonus(ModifierType type, String name, String title, int bonus) {
        super(type, name, title);

        this.bonus = bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public int getBonus() {
        return bonus;
    }
}
