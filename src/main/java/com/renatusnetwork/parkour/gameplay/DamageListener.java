package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamagePlayer(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            EventManager eventManager = Parkour.getEventManager();

            // for anvil event
            if (event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
                // only run code if event is running and type HALF_HEART
                if (eventManager.isEventRunning() && eventManager.isFallingAnvilEvent()) {

                    Player victim = (Player) event.getEntity();
                    PlayerStats victimStats = Parkour.getStatsManager().get(victim.getUniqueId().toString());

                    // cancel damage event just for safety
                    event.setCancelled(true);
                    // only run code if they are both participants, therefore respawn player
                    if (victimStats.isEventParticipant())
                        player.teleport(eventManager.getRunningEvent().getLevel().getStartLocation());
                }
            // for elytra
            } else if (event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
                event.setCancelled(true);
            // for droppers
            } else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats.inLevel() && playerStats.getLevel().isDropperLevel()) {
                    event.setCancelled(true);

                    // just in case we use checkpoints for droppers at some point
                    if (playerStats.hasCurrentCheckpoint())
                        Parkour.getCheckpointManager().teleportToCP(playerStats);
                    else
                        LevelHandler.respawnPlayer(playerStats, playerStats.getLevel());
                }
            }
        }
    }

    @EventHandler
    public void onDamageOther(EntityDamageByEntityEvent event)
    {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player)
        {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            EventManager eventManager = Parkour.getEventManager();
            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            PlayerStats damagerStats = Parkour.getStatsManager().get(damager);

            // we only care about the one being damaged, make sure they are in the pvp event
            if (eventManager.isEventRunning() && eventManager.isPvPEvent() &&
                playerStats.isEventParticipant() && damagerStats.isEventParticipant())
            {
                // damage to 0
                event.setDamage(0.0);
                return;
            }
        }
        event.setCancelled(true);
    }
}
