package com.renatusnetwork.momentum.data.modifiers;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.storage.mysql.DatabaseManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifiersDB
{
    public static HashMap<String, Modifier> getModifiers()
    {
        List<Map<String, String>> modifierResults = DatabaseQueries.getResults(DatabaseManager.MODIFIERS_TABLE, "*", "");
        HashMap<String, Modifier> modifiers = new HashMap<>();

        for (Map<String, String> modifierResult : modifierResults)
        {
            String name = modifierResult.get("name");
            ModifierType type = ModifierType.valueOf(modifierResult.get("type").toUpperCase());
            String title = Utils.translate(modifierResult.get("title"));

            float modifierValue;

            String multiplier = modifierResult.get("multiplier");
            String discount = modifierResult.get("discount");

            if (multiplier != null)
                modifierValue = Float.parseFloat(multiplier);
            else if (discount != null)
                modifierValue = Float.parseFloat(discount);
            else
                modifierValue = Integer.parseInt(modifierResult.get("bonus"));

            modifiers.put(name, Momentum.getModifiersManager().createSubclass(name, type, title, modifierValue));
        }

        return modifiers;
    }

    public static void insertBoosterModifier(String name, ModifierType type, String title, float multiplier)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.MODIFIERS_TABLE + " (name, type, title, multiplier) VALUES (?,?,?,?)",
                name, type.name(), title, multiplier
        );
    }

    public static void insertDiscountModifier(String name, ModifierType type, String title, float discount)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.MODIFIERS_TABLE + " (name, type, title, discount) VALUES (?,?,?,?)",
                name, type.name(), title, discount
        );
    }

    public static void insertBonusModifier(String name, ModifierType type, String title, int bonus)
    {
        DatabaseQueries.runAsyncQuery(
                "INSERT INTO " + DatabaseManager.MODIFIERS_TABLE + " (name, type, title, bonus) VALUES (?,?,?,?)",
                name, type.name(), title, bonus
        );
    }

    public static void updateTitle(String name, String title)
    {
        DatabaseQueries.runAsyncQuery(
                "UPDATE " + DatabaseManager.MODIFIERS_TABLE + " SET title=? WHERE name=?", title, name
        );
    }
}
