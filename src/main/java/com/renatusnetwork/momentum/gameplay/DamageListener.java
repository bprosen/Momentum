package com.renatusnetwork.momentum.gameplay;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.events.EventType;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
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
            EventManager eventManager = Momentum.getEventManager();

            // for anvil event
            if (event.getCause() == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
                // only run code if event is running and type HALF_HEART
                if (eventManager.isEventRunning() && eventManager.getEventType() == EventType.FALLING_ANVIL) {

                    Player victim = (Player) event.getEntity();
                    PlayerStats victimStats = Momentum.getStatsManager().get(victim.getUniqueId().toString());

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
                PlayerStats playerStats = Momentum.getStatsManager().get(player);

                if (playerStats.inLevel() && playerStats.getLevel().isDropperLevel()) {
                    event.setCancelled(true);

                    // just in case we use checkpoints for droppers at some point
                    if (playerStats.hasCurrentCheckpoint())
                        Momentum.getCheckpointManager().teleportToCP(playerStats);
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

            EventManager eventManager = Momentum.getEventManager();
            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            PlayerStats damagerStats = Momentum.getStatsManager().get(damager);

            // we only care about the one being damaged, make sure they are in the pvp event
            if (eventManager.isEventRunning() && eventManager.getEventType() == EventType.PVP &&
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
