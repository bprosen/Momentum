package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import com.parkourcraft.parkour.utils.dependencies.WorldGuardUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
            event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            ItemStack item = event.getItem();

            if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null)
                return;

            event.setCancelled(true);
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

                if (Parkour.getStatsManager().get(player).getCheckpoint() != null &&
                    !WorldGuardUtils.getRegions(player.getLocation()).get(0).equalsIgnoreCase("spawn")) {

                        Parkour.getCheckpointManager().teleportPlayer(player);

                } else {
                    player.sendMessage(Utils.translate("&cYou do not have a saved checkpoint"));
                }
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&cReset"))) {

                if (!WorldGuardUtils.getRegions(player.getLocation()).get(0).equalsIgnoreCase("spawn")) {
                    LevelObject level = Parkour.getLevelManager().get(LevelHandler.getLocationLevelName(player));
                    if (level != null) {
                        Parkour.getStatsManager().get(player).resetCheckpoint();
                        player.teleport(level.getStartLocation());
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou are not in a level"));
                }
            }
        }
    }
}
