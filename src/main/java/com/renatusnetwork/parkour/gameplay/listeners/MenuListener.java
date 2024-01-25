package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.levels.RaceLevel;
import com.renatusnetwork.parkour.data.menus.*;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.MenuUtils;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MenuListener implements Listener
{

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MenuHolder)
        {
            MenuHolder menuHolder = (MenuHolder) holder;
            Menu menu = menuHolder.getMenu();

            if (menu != null)
            {
                event.setCancelled(true);
                ItemStack currentItem = event.getCurrentItem();

                if (currentItem != null
                    && currentItem.getType() != Material.AIR
                    && currentItem.hasItemMeta()
                    && currentItem.getItemMeta().hasDisplayName())
                {

                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    MenuItem menuItem = menu.getMenuItem(playerStats,
                            Utils.getTrailingInt(event.getInventory().getTitle()),
                            event.getSlot()
                    );

                    if (menuItem != null &&
                            ((menuItem.getItem().getType() == currentItem.getType() ||
                                    menuItem.getTypeValue().equalsIgnoreCase("featured") ||
                                    menuItem.getTypeValue().startsWith("favorite-level")
                            ) || Parkour.getLevelManager().isBuyingLevelMenu(player.getName()))) {
                        boolean shiftClicked = event.isShiftClick();
                        if (shiftClicked)
                            MenuUtils.addShiftClicked(player.getName());

                        MenuItemAction.perform(player, menuItem);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);

                        if (shiftClicked)
                            MenuUtils.removeShiftClicked(player.getName());

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
                            RaceLevel selectedLevel = null;

                            if (ChatColor.stripColor(currentItem.getItemMeta().getDisplayName()).equalsIgnoreCase("Random Level"))
                                randomLevel = true;
                            else
                                selectedLevel = (RaceLevel) Parkour.getLevelManager().getFromTitle(event.getCurrentItem().getItemMeta().getDisplayName());

                            // if it is an item that can be used for races, continue
                            if (selectedLevel != null) {
                                List<String> itemLore = currentItem.getItemMeta().getLore();

                                String lastString = ChatColor.stripColor(itemLore.get(itemLore.size() - 1));
                                boolean bet = false;
                                double betAmount = 0.0;
                                String opponentName = null;

                                if (lastString.toUpperCase().contains("bet amount".toUpperCase())) {
                                    bet = true;
                                    // get the right side of the ->
                                    betAmount = Double.parseDouble(lastString.split("-> ")[1]);
                                    // this means the against string is second last
                                    opponentName = ChatColor.stripColor(itemLore.get(itemLore.size() - 2)).split("-> ")[1];

                                } else if (lastString.toUpperCase().contains("against".toUpperCase()))
                                    opponentName = lastString.split("-> ")[1];

                                PlayerStats opponentStats = Parkour.getStatsManager().getByName(opponentName);

                                // close inventory
                                player.closeInventory();

                                if (opponentStats != null) {
                                    // then use the boolean if to run the appropriate conditions
                                    if (randomLevel)
                                        Parkour.getRaceManager().sendRequest(playerStats, opponentStats, true, null, bet, betAmount);
                                    else
                                        Parkour.getRaceManager().sendRequest(playerStats, opponentStats, false, selectedLevel, bet, betAmount);
                                } else
                                    player.sendMessage(Utils.translate("&4" + opponentName + " &cis not online"));
                            }
                        }
                    }
                }
            }
        }
        else if (!player.isOp() && !player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event)
    {
        LevelManager levelManager = Parkour.getLevelManager();
        String name = event.getPlayer().getName();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Inventory openedInventory = event.getInventory();
                Inventory nextInventory = event.getPlayer().getOpenInventory().getTopInventory();

                if (!openedInventory.getName().equalsIgnoreCase(nextInventory.getName()))
                {
                    // remove buying
                    if (levelManager.isBuyingLevelMenu(name))
                        levelManager.removeBuyingLevel(name);

                    // cancelled tasks
                    CancelTasks cancelTasks = Parkour.getMenuManager().getCancelTasks(name);

                    // if not null and contains, we need to cancel remaining tasks!
                    if (cancelTasks != null && cancelTasks.getCancelledSlots() != null)
                    {
                        for (BukkitTask task : cancelTasks.getCancelledSlots())
                            task.cancel();

                        Parkour.getMenuManager().removeCancelTasks(name); // remove
                    }
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 1);
    }

    @EventHandler
    public void onMenuItemUse(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world) &&
           (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
        {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (!playerStats.isInTutorial())
            {
                Menu menu = Parkour.getMenuManager().getMenuFromSelectItem(player.getInventory().getItemInMainHand());

                if (menu != null)
                    Parkour.getMenuManager().openInventory(playerStats, menu.getName(), false);
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial"));
            }
        }
    }
}
