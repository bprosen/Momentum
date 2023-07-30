package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.api.InfiniteEndEvent;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class InfiniteEndListener implements Listener
{
    @EventHandler
    public void onInfiniteEnd(InfiniteEndEvent event)
    {
        PlayerStats playerStats = event.getPlayerStats();

        if (playerStats != null)
        {
            Modifier modifier = playerStats.getModifier(ModifierTypes.INFINITE_BOOSTER);

            // has modifier
            if (modifier != null)
            {
                Booster booster = (Booster) modifier; // down cast
                event.setReward((int) (event.getReward() * booster.getMultiplier())); // reward * multiplier
            }
        }
    }
}
