package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ModifiersYAML
{

    private static FileConfiguration modifiersConfig = Parkour.getConfigManager().get("modifiers");

    private static void commit() {
        Parkour.getConfigManager().save("modifiers");
    }

    public static List<String> getNames() {
        return new ArrayList<>(modifiersConfig.getKeys(false));
    }

    public static boolean exists(String modifierName) {
        return modifiersConfig.isSet(modifierName);
    }

    public static ModifierTypes getType(String modifierName)
    {
        return ModifierTypes.valueOf(modifiersConfig.getString(modifierName + ".type"));
    }

    public static String getDisplayName(String modifierName)
    {
        return modifiersConfig.getString(modifierName + ".display");
    }

    public static float getFactor(String modifierName)
    {
        return (float) modifiersConfig.getDouble(modifierName + ".factor");
    }

    public static float getDiscountPercentage(String modifierName)
    {
        float percent = (float) modifiersConfig.getDouble(modifierName + ".discount");

        // adjust for impossibles
        if (percent < 0.0)
            percent = 0.0f;

        if (percent > 1.00)
            percent = 1.00f;

        return percent;
    }
}
