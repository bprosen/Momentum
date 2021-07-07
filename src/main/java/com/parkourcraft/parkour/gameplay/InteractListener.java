package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;

public class InteractListener implements Listener {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {

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
                Parkour.getCheckpointManager().teleportPlayer(Parkour.getStatsManager().get(player));

            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&cReset"))) {

                event.setCancelled(true);
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                Level level = playerStats.getLevel();

                if (!playerStats.inRace()) {
                    if (!playerStats.isEventParticipant()) {
                        if (playerStats.getPlayerToSpectate() == null) {
                            if (level != null) {

                                // gets if they have right clicked it already, if so, cancel the task and reset them
                                if (confirmMap.containsKey(player.getName())) {
                                    confirmMap.get(player.getName()).cancel();
                                    confirmMap.remove(player.getName());
                                    playerStats.resetCheckpoint();
                                    playerStats.resetPracticeMode();

                                    if (!level.getPotionEffects().isEmpty()) {
                                        for (PotionEffect potionEffect : player.getActivePotionEffects())
                                            player.removePotionEffect(potionEffect.getType());

                                        for (PotionEffect potionEffect : level.getPotionEffects())
                                            player.addPotionEffect(potionEffect);
                                    }

                                    player.teleport(level.getStartLocation());
                                } else {
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
