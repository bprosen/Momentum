package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.Menu;
import com.parkourcraft.parkour.data.menus.MenuItem;
import com.parkourcraft.parkour.data.menus.MenuItemAction;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Menu menu = Parkour.getMenuManager().getMenuFromTitle(event.getInventory().getTitle());

        if (menu != null) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null
                    && event.getCurrentItem().getType() != Material.AIR
                    && event.getCurrentItem().getItemMeta().getDisplayName() != null) {

                MenuItem menuItem = menu.getMenuItem(
                        Utils.getTrailingInt(event.getInventory().getTitle()),
                        event.getSlot()
                );

                Player player = (Player) event.getWhoClicked();
                ItemStack itemClicked = event.getCurrentItem();

                if (menuItem != null)
                    MenuItemAction.perform(player, menuItem);
                // submitted plots section
                else if (menu.getName().equals("submitted-plots") &&
                        itemClicked.equals(Material.SKULL)) {

                    String[] split = itemClicked.getItemMeta().getDisplayName().split("'");
                    Plot plot = Parkour.getPlotsManager().get(ChatColor.stripColor(split[0]));

                    if (plot != null) {
                        player.teleport(plot.getSpawnLoc());
                        player.sendMessage(Utils.translate("&cYou have teleported to &4" + plot.getOwnerName() + "&c's Plot"));
                    } else {
                        player.sendMessage(Utils.translate("&cPlot does not exist"));
                        player.closeInventory();
                    }
                }
            }
        } else if (event.getWhoClicked() instanceof Player
                && !event.getWhoClicked().isOp())
            event.setCancelled(true);
    }

    @EventHandler
    public void onMenuItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Menu menu = Parkour.getMenuManager().getMenuFromSelectItem(player.getInventory().getItemInMainHand());

            if (menu != null) {
                player.openInventory(Parkour.getMenuManager().getInventory(menu.getName(), 1));
                Parkour.getMenuManager().updateInventory(player, player.getOpenInventory(), menu.getName(), 1);
            }
        }
    }
}
