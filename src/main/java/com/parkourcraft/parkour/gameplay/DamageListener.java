package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamagePlayer(EntityDamageByEntityEvent event) {

        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            EventManager eventManager = Parkour.getEventManager();

            // only run code if event is running and type HALF_HEART
            if (eventManager.isEventRunning() && eventManager.getEventType() == EventType.HALF_HEART) {

                PlayerStats damagerStats = Parkour.getStatsManager().get(damager.getUniqueId().toString());
                PlayerStats victimStats = Parkour.getStatsManager().get(victim.getUniqueId().toString());

                // only run code if they are both participants, therefore the victim is eliminated
                if (victimStats.isEventParticipant() && damagerStats.isEventParticipant()) {

                    // then cancel and remove
                    event.setCancelled(true);
                    eventManager.removeParticipant(victim, false);
                    eventManager.addEliminated(victim);
                    victim.sendMessage(Utils.translate("&7You were hit and got &beliminated out &7of the event!"));
                }
            }
        }
    }
}
