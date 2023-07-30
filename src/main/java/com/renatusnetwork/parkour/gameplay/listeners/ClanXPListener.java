package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.api.ClanXPRewardEvent;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClanXPListener implements Listener
{
    @EventHandler
    public void onClanXP(ClanXPRewardEvent event)
    {
        PlayerStats playerStats = event.getPlayerStats();

        if (playerStats != null)
        {
            Modifier modifier = playerStats.getModifier(ModifierTypes.CLAN_XP_BOOSTER);

            // has modifier
            if (modifier != null)
            {
                Booster booster = (Booster) modifier; // down cast
                event.setXP((int) (event.getXP() * booster.getMultiplier())); // reward * multiplier
            }
        }
    }
}
