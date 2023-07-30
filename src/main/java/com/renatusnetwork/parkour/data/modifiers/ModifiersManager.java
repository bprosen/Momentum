package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.data.modifiers.boosters.*;
import com.renatusnetwork.parkour.data.modifiers.discounts.LevelDiscount;
import com.renatusnetwork.parkour.data.modifiers.discounts.ShopDiscount;

import java.util.HashMap;

public class ModifiersManager
{

    private HashMap<String, Modifier> modifiers;

    public ModifiersManager()
    {
        load();
    }

    public Modifier getModifier(String name)
    {
        return modifiers.get(name);
    }

    private void load()
    {
        modifiers = new HashMap<>();

        for (String key : ModifiersYAML.getNames())
        {
            ModifierTypes type = ModifiersYAML.getType(key);

            // load all types!
            switch (type)
            {
                case GG_BOOSTER:
                    modifiers.put(key, new GGBooster(ModifierTypes.GG_BOOSTER, key));
                    break;
                case EVENT_BOOSTER:
                    modifiers.put(key, new EventBooster(ModifierTypes.EVENT_BOOSTER, key));
                    break;
                case LEVEL_BOOSTER:
                    modifiers.put(key, new LevelBooster(ModifierTypes.LEVEL_BOOSTER, key));
                    break;
                case CLAN_XP_BOOSTER:
                    modifiers.put(key, new ClanXPBooster(ModifierTypes.CLAN_XP_BOOSTER, key));
                    break;
                case JACKPOT_BOOSTER:
                    modifiers.put(key, new JackpotBooster(ModifierTypes.JACKPOT_BOOSTER, key));
                    break;
                case PRESTIGE_BOOSTER:
                    modifiers.put(key, new PrestigeBooster(ModifierTypes.PRESTIGE_BOOSTER, key));
                    break;
                case LEVEL_DISCOUNT:
                    modifiers.put(key, new LevelDiscount(ModifierTypes.LEVEL_DISCOUNT, key));
                    break;
                case SHOP_DISCOUNT:
                    modifiers.put(key, new ShopDiscount(ModifierTypes.SHOP_DISCOUNT, key));
                    break;
                case INFINITE_BOOSTER:
                    modifiers.put(key, new InfiniteBooster(ModifierTypes.INFINITE_BOOSTER, key));
                    break;
            }
        }
    }
}