package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.modifiers.bonuses.RecordBonus;
import com.renatusnetwork.parkour.data.modifiers.boosters.*;
import com.renatusnetwork.parkour.data.modifiers.discounts.Discount;
import com.renatusnetwork.parkour.data.modifiers.discounts.LevelDiscount;
import com.renatusnetwork.parkour.data.modifiers.discounts.ShopDiscount;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

        if (isBooster(modifier.getType()))
            ModifiersDB.insertBoosterModifier(name, type, title, modifierValue);
        else if (isDiscount(modifier.getType()))
            ModifiersDB.insertDiscountModifier(name, type, title, modifierValue);
        else if (isBonus(modifier.getType()))
            ModifiersDB.insertBonusModifier(name, type, title, (int) modifierValue);
    }

    public void updateTitle(Modifier modifier, String title)
    {
        modifier.setTitle(title);
        ModifiersDB.updateTitle(modifier.getName(), title);
    }

    public boolean isBooster(ModifierType type)
    {
        return type == ModifierType.CLAN_XP_BOOSTER ||
               type == ModifierType.EVENT_BOOSTER ||
               type == ModifierType.GG_BOOSTER ||
               type == ModifierType.JACKPOT_BOOSTER ||
               type == ModifierType.LEVEL_BOOSTER ||
               type == ModifierType.INFINITE_BOOSTER;
    }

    public boolean isDiscount(ModifierType type)
    {
        return type == ModifierType.LEVEL_DISCOUNT || type == ModifierType.SHOP_DISCOUNT;
    }

    public boolean isBonus(ModifierType type)
    {
        return type == ModifierType.RECORD_BONUS;
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
