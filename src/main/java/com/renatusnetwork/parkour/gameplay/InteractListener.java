package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.PlayerHider;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Openable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;

public class InteractListener implements Listener {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.HAND &&
           (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {

            // this is in case they try to click a trapdoor, door or something openable if they're in spectator
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock() != null &&
                event.getClickedBlock().getState().getData() instanceof Openable &&
                Parkour.getStatsManager().get(player).getPlayerToSpectate() != null) {
                event.setCancelled(true);
                return;
            }

            ItemStack item = event.getItem();

            if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null)
                return;

            if (item.getItemMeta().getDisplayName().startsWith(Utils.translate("&2Players &7»"))) {

                event.setCancelled(true);

                player.getInventory().removeItem(item);
                player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.7f, 0);
                if (PlayerHider.containsPlayer(player)) {
                    PlayerHider.showPlayer(player, false);

                    ItemStack newItem = new ItemStack(Material.REDSTONE_TORCH_ON);
                    ItemMeta meta = newItem.getItemMeta();
                    meta.setDisplayName(Utils.translate("&2Players &7» &2Enabled"));
                    newItem.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(newItem);

                    player.sendMessage(Utils.translate("&aYou have turned on players"));
                } else {
                    PlayerHider.hidePlayer(player, false);

                    ItemStack newItem = new ItemStack(Material.LEVER);
                    ItemMeta meta = newItem.getItemMeta();
                    meta.setDisplayName(Utils.translate("&2Players &7» &cDisabled"));
                    newItem.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(newItem);

                    player.sendMessage(Utils.translate("&cYou have turned off players"));
                }

            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&eLast Checkpoint"))) {

                event.setCancelled(true);
                Parkour.getCheckpointManager().teleportToCP(Parkour.getStatsManager().get(player));

            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&cReset"))) {

                event.setCancelled(true);
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                Level level = playerStats.getLevel();

                if (!playerStats.inRace()) {
                    if (!playerStats.isEventParticipant()) {
                        if (playerStats.getPlayerToSpectate() == null) {
                            if (level != null) {

                                // gets if they have right clicked it already, if so, cancel the task and reset them
                                if (!confirmMap.containsKey(player.getName())) {
                                    // otherwise, put them in and ask them to confirm within 5 seconds
                                    player.sendMessage(Utils.translate("&6Are you sure you want to reset? Right click again to confirm"));

                                    confirmMap.put(player.getName(), new BukkitRunnable() {
                                        public void run() {
                                            if (confirmMap.containsKey(player.getName())) {
                                                confirmMap.remove(player.getName());
                                                player.sendMessage(Utils.translate("&cYou did not confirm in time"));
                                            }
                                        }
                                    }.runTaskLater(Parkour.getPlugin(), 20 * 5));
                                } else {
                                    confirmMap.get(player.getName()).cancel();

                                    Parkour.getCheckpointManager().deleteCheckpoint(playerStats, level);

                                    playerStats.resetPracticeMode();

                                    if (!level.getPotionEffects().isEmpty()) {

                                        playerStats.clearPotionEffects();

                                        // if has nv status, add nv
                                        if (playerStats.hasNVStatus())
                                            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));

                                        for (PotionEffect potionEffect : level.getPotionEffects())
                                            player.addPotionEffect(potionEffect);
                                    }
                                    confirmMap.remove(player.getName());

                                    player.teleport(level.getStartLocation());
                                    player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.5f, 1f);
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou are not in a level"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                }
            }
        }
    }
}
