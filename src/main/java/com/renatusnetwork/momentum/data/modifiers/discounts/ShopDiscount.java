package com.renatusnetwork.momentum.data.modifiers.discounts;

import com.renatusnetwork.momentum.data.modifiers.ModifierType;

public class ShopDiscount extends Discount {

    public ShopDiscount(ModifierType type, String name, String title, float discount) {
        super(type, name, title, discount);
    }
}
