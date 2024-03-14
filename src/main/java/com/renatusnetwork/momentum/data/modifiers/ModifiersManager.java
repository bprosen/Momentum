package com.renatusnetwork.momentum.data.modifiers;

import com.renatusnetwork.momentum.data.modifiers.bonuses.RecordBonus;
import com.renatusnetwork.momentum.data.modifiers.boosters.*;
import com.renatusnetwork.momentum.data.modifiers.discounts.LevelDiscount;
import com.renatusnetwork.momentum.data.modifiers.discounts.ShopDiscount;

import java.util.Collection;
import java.util.HashMap;

public class ModifiersManager
{

    private HashMap<String, Modifier> modifiers;

    public ModifiersManager()
    {
        load();
    }
    public void load()
    {
        modifiers = ModifiersDB.getModifiers();
    }

    public void create(String name, ModifierType type, String title, float modifierValue)
    {
        Modifier modifier = createSubclass(name, type, title, modifierValue);
        modifiers.put(name, modifier);

        if (modifier.isBooster())
            ModifiersDB.insertBoosterModifier(name, type, title, modifierValue);
        else if (modifier.isDiscount())
            ModifiersDB.insertDiscountModifier(name, type, title, modifierValue);
        else if (modifier.isBonus())
            ModifiersDB.insertBonusModifier(name, type, title, (int) modifierValue);
    }

    public void updateTitle(Modifier modifier, String title)
    {
        modifier.setTitle(title);
        ModifiersDB.updateTitle(modifier.getName(), title);
    }

    public Modifier createSubclass(String name, ModifierType type, String title, float modifierValue)
    {
        // load all types!
        switch (type)
        {
            case GG_BOOSTER:
                return new GGBooster(ModifierType.GG_BOOSTER, name, title, modifierValue);
            case EVENT_BOOSTER:
                return new EventBooster(ModifierType.EVENT_BOOSTER, name, title, modifierValue);
            case LEVEL_BOOSTER:
                return new LevelBooster(ModifierType.LEVEL_BOOSTER, name, title, modifierValue);
            case CLAN_XP_BOOSTER:
                return new ClanXPBooster(ModifierType.CLAN_XP_BOOSTER, name, title, modifierValue);
            case JACKPOT_BOOSTER:
                return new JackpotBooster(ModifierType.JACKPOT_BOOSTER, name, title, modifierValue);
            case LEVEL_DISCOUNT:
                return new LevelDiscount(ModifierType.LEVEL_DISCOUNT, name, title, modifierValue);
            case SHOP_DISCOUNT:
                return new ShopDiscount(ModifierType.SHOP_DISCOUNT, name, title, modifierValue);
            case INFINITE_BOOSTER:
                return new InfiniteBooster(ModifierType.INFINITE_BOOSTER, name, title, modifierValue);
            case RECORD_BONUS:
                return new RecordBonus(ModifierType.RECORD_BONUS, name, title, (int) modifierValue);
        }

        return null;
    }

    public boolean exists(String name) { return modifiers.containsKey(name); }

    public Modifier getModifier(String name)
    {
        return modifiers.get(name);
    }

    public Collection<Modifier> getModifiers()
    {
        return modifiers.values();
    }
}
