package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

            event.setCancelled(true);
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (item.getItemMeta().getDisplayName().startsWith(Utils.translate("&2Players &7»"))) {

                player.getInventory().removeItem(item);
                if (PlayerHider.containsPlayer(player)) {
                    PlayerHider.showPlayer(player);

                    ItemStack newItem = new ItemStack(Material.REDSTONE_TORCH_ON);
                    ItemMeta meta = newItem.getItemMeta();
                    meta.setDisplayName(Utils.translate("&2Players &7» &2Enabled"));
                    newItem.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(newItem);

                    player.sendMessage(Utils.translate("&aYou have turned on players"));
                } else {
                    PlayerHider.hidePlayer(player);

                    ItemStack newItem = new ItemStack(Material.LEVER);
                    ItemMeta meta = newItem.getItemMeta();
                    meta.setDisplayName(Utils.translate("&2Players &7» &cDisabled"));
                    newItem.setItemMeta(meta);
                    player.getInventory().setItemInMainHand(newItem);

                    player.sendMessage(Utils.translate("&cYou have turned off players"));
                }

            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&eLast Checkpoint"))) {

                if (playerStats.getCheckpoint() != null || playerStats.getPracticeLocation() != null)
                    Parkour.getCheckpointManager().teleportPlayer(player);
                else
                    player.sendMessage(Utils.translate("&cYou do not have a saved checkpoint"));
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&cReset"))) {

                String levelName = playerStats.getLevel();

                if (levelName != null) {
                    LevelObject level = Parkour.getLevelManager().get(levelName);
                    if (level != null) {

                        // gets if they have right clicked it already, if so, cancel the task and reset them
                        if (confirmMap.containsKey(player.getName())) {
                            confirmMap.get(player.getName()).cancel();
                            confirmMap.remove(player.getName());
                            playerStats.resetCheckpoint();
                            playerStats.resetPracticeMode();
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
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou are not in a level"));
                }
            }
        }
    }
}
