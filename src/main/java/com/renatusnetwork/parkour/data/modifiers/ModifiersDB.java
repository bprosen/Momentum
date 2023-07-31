package com.renatusnetwork.parkour.data.modifiers;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModifiersDB
{
    public static void loadModifiers(PlayerStats playerStats)
    {
        ArrayList<Modifier> modifiers = new ArrayList<>();

        List<Map<String, String>> playerResults = DatabaseQueries.getResults(
                "modifiers",
                "*",
                " WHERE uuid='" + playerStats.getUUID() + "'"
        );

        if (playerResults.size() > 0)
        {
            for (Map<String, String> playerResult : playerResults)
            {
                String modifierName = playerResult.get("modifier_name");

                modifiers.add(Parkour.getModifiersManager().getModifier(modifierName));
            }
        }

        playerStats.setModifiers(modifiers);
    }

    public static void addModifier(PlayerStats playerStats, Modifier modifier)
    {
        Parkour.getDatabaseManager().add("INSERT INTO modifiers (uuid, player_name, modifier_name) VALUES('" +
                playerStats.getUUID() + "','" +
                playerStats.getPlayerName() + "','" +
                modifier.getName() + "'"
        );
    }

    public static void removeModifier(PlayerStats playerStats, Modifier modifier)
    {
        Parkour.getDatabaseManager().add("DELETE FROM modifiers WHERE uuid='" + playerStats.getUUID() + "' AND modifier_name='" + modifier.getName() + "'");
    }
}
