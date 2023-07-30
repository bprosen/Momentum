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
                    modifiers.put(key, new GGBooster(key));
                    break;
                case EVENT_BOOSTER:
                    modifiers.put(key, new EventBooster(key));
                    break;
                case LEVEL_BOOSTER:
                    modifiers.put(key, new LevelBooster(key));
                    break;
                case CLAN_XP_BOOSTER:
                    modifiers.put(key, new ClanXPBooster(key));
                    break;
                case JACKPOT_BOOSTER:
                    modifiers.put(key, new JackpotBooster(key));
                    break;
                case PRESTIGE_BOOSTER:
                    modifiers.put(key, new PrestigeBooster(key));
                    break;
                case LEVEL_DISCOUNT:
                    modifiers.put(key, new LevelDiscount(key));
                    break;
                case SHOP_DISCOUNT:
                    modifiers.put(key, new ShopDiscount(key));
                    break;
                case INFINITE_BOOSTER:
                    modifiers.put(key, new InfiniteBooster(key));
                    break;
            }
        }
    }
}
