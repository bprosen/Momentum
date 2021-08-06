package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
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
                if (eventManager.isEventRunning() && eventManager.getEventType() == EventType.FALLING_ANVIL) {

                    Player victim = (Player) event.getEntity();
                    PlayerStats victimStats = Parkour.getStatsManager().get(victim.getUniqueId().toString());

                    // cancel damage event just for safety
                    event.setCancelled(true);
                    // only run code if they are both participants, therefore the victim is eliminated
                    if (victimStats.isEventParticipant()) {

                        // then cancel and remove, and do firework
                        eventManager.doFireworkExplosion(victim.getLocation());
                        eventManager.removeParticipant(victim, false);
                        eventManager.addEliminated(victim);
                        victim.sendMessage(Utils.translate("&7You were hit and got &beliminated out &7of the event!"));
                    }
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
                    if (playerStats.getCheckpoint() != null)
                        Parkour.getCheckpointManager().teleportPlayer(playerStats);
                    else
                        LevelHandler.respawnPlayer(player, playerStats.getLevel());
                }
            // for pvp event
            } else if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) event;

                if (entityDamageEvent.getDamager() instanceof Player) {
                    Player damager = (Player) entityDamageEvent.getDamager();

                    if (!(eventManager.isEventRunning() &&
                        eventManager.getEventType() == EventType.PVP &&
                        eventManager.isParticipant(player) &&
                        eventManager.isParticipant(damager))) {
                        // cancel event
                        entityDamageEvent.setCancelled(true);
                    } else {
                        // set damage to 0
                        entityDamageEvent.setDamage(0.0);
                    }
                }
            }
        }
    }
}
