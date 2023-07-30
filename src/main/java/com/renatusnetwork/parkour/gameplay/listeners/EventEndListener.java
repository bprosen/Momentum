package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.api.ParkourEventEndEvent;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EventEndListener implements Listener
{
    @EventHandler
    public void onEventEnd(ParkourEventEndEvent event)
    {
        PlayerStats playerStats = event.getWinner();

        if (playerStats != null)
        {
            Modifier modifier = playerStats.getModifier(ModifierTypes.EVENT_BOOSTER);

            // has modifier
            if (modifier != null)
            {
                Booster booster = (Booster) modifier; // down cast
                event.setReward((int) (event.getReward() * booster.getMultiplier())); // reward * multiplier
            }
        }
    }
}
