package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.menus.Menu;
import com.parkourcraft.parkour.data.menus.MenuItem;
import com.parkourcraft.parkour.data.menus.MenuItemAction;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Menu menu = Parkour.getMenuManager().getMenuFromTitle(event.getInventory().getTitle());

        if (menu != null) {
            event.setCancelled(true);
            ItemStack currentItem = event.getCurrentItem();

            if (currentItem != null
                && currentItem.getType() != Material.AIR
                && currentItem.hasItemMeta()
                && currentItem.getItemMeta().hasDisplayName()) {

                MenuItem menuItem = menu.getMenuItem(
                        Utils.getTrailingInt(event.getInventory().getTitle()),
                        event.getSlot()
                );

                Player player = (Player) event.getWhoClicked();

                if (menuItem != null && menuItem.getItem().getType() == currentItem.getType()) {
                    MenuItemAction.perform(player, menuItem);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);
                } else {
                    // submitted plots section
                    String submittedPlotsTitle = Parkour.getMenuManager().getMenu("submitted-plots").getFormattedTitleBase();
                    String pickRaceLevelsTitle = Parkour.getMenuManager().getMenu("pick-race-levels").getFormattedTitleBase();

                    /*
                        Submitted Plots GUI
                     */
                    if (menu.getFormattedTitleBase().equalsIgnoreCase(submittedPlotsTitle)) {
                        if (currentItem.getType() == Material.SKULL_ITEM) {

                            String[] split = currentItem.getItemMeta().getDisplayName().split("'");
                            Plot plot = Parkour.getPlotsManager().get(ChatColor.stripColor(split[0]));

                            player.closeInventory();

                            if (plot != null) {
                                // set pitch and yaw for cleaner teleport
                                Location plotSpawn = plot.getSpawnLoc().clone();
                                plotSpawn.setPitch(player.getLocation().getPitch());
                                plotSpawn.setYaw(player.getLocation().getYaw());

                                player.teleport(plotSpawn);
                                player.sendMessage(Utils.translate("&cYou teleported to &4" + plot.getOwnerName() + "&c's Plot"));
                            } else {
                                player.sendMessage(Utils.translate("&cPlot does not exist"));
                            }
                        }
                    /*
                        Race Levels GUI, optimize by not including stained glass pane
                     */
                    } else if (menu.getFormattedTitleBase().equalsIgnoreCase(pickRaceLevelsTitle) && currentItem.getType() != Material.STAINED_GLASS_PANE) {
                        // conditions to determine if it is the right item, if it is a random level they selected, etc
                        boolean randomLevel = false;
                        boolean correctItem = false;
                        Level selectedLevel = null;

                        if (ChatColor.stripColor(currentItem.getItemMeta().getDisplayName()).equalsIgnoreCase("Random Level")) {
                            randomLevel = true;
                            correctItem = true;
                        } else {
                            Level level = Parkour.getLevelManager().getFromTitle(event.getCurrentItem().getItemMeta().getDisplayName());

                            // if they hit a selected level
                            if (level != null) {
                                selectedLevel = level;
                                correctItem = true;
                            }
                        }

                        // if it is an item that can be used for races, continue
                        if (correctItem) {
                            List<String> itemLore = currentItem.getItemMeta().getLore();

                            String lastString = ChatColor.stripColor(itemLore.get(itemLore.size() - 1));
                            boolean bet = false;
                            double betAmount = -1.0;
                            String opponentName = null;

                            if (lastString.toUpperCase().contains("bet".toUpperCase())) {
                                bet = true;
                                // get the right side of the ->
                                betAmount = Double.parseDouble(lastString.split("-> ")[1]);
                                // this means the against string is second last
                                opponentName = ChatColor.stripColor(itemLore.get(itemLore.size() - 2)).split("-> ")[1];

                            } else if (lastString.toUpperCase().contains("against".toUpperCase()))
                                opponentName = lastString.split("-> ")[1];

                            PlayerStats playerStats = Parkour.getStatsManager().get(player);
                            PlayerStats opponentStats = Parkour.getStatsManager().getByNameIgnoreCase(opponentName);

                            // close inventory
                            player.closeInventory();

                            if (opponentStats != null) {
                                // then use the boolean if to run the appropriate conditions
                                if (randomLevel)
                                    Parkour.getRaceManager().sendRequest(playerStats, opponentStats, true, null, bet, betAmount);
                                else
                                    Parkour.getRaceManager().sendRequest(playerStats, opponentStats, false, selectedLevel, bet, betAmount);
                            } else
                                player.sendMessage(Utils.translate("&4" + opponentName + " &cis not online anymore"));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMenuItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world) &&
           (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {

            Menu menu = Parkour.getMenuManager().getMenuFromSelectItem(player.getInventory().getItemInMainHand());

            if (menu != null) {
                player.openInventory(Parkour.getMenuManager().getInventory(menu.getName(), 1));
                Parkour.getMenuManager().updateInventory(player, player.getOpenInventory(), menu.getName(), 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);
            }
        }
    }
}
