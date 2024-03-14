package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.LevelManager;
import com.renatusnetwork.momentum.data.menus.*;
import com.renatusnetwork.momentum.data.plots.Plot;
import com.renatusnetwork.momentum.data.races.RaceManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
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

                    PlayerStats playerStats = Momentum.getStatsManager().get(player);

                    MenuItem menuItem = menu.getMenuItem(playerStats,
                            Utils.getTrailingInt(event.getInventory().getTitle()),
                            event.getSlot()
                    );

                    if (menuItem != null &&
                            ((menuItem.getItem().getType() == currentItem.getType() ||
                                    menuItem.getTypeValue().equalsIgnoreCase("featured") ||
                                    menuItem.getTypeValue().startsWith("favorite-level")
                            ) || Momentum.getLevelManager().isBuyingLevelMenu(player.getName()))) {
                        boolean shiftClicked = event.isShiftClick();

                        if (shiftClicked)
                            Momentum.getMenuManager().addShiftClicked(playerStats);

                        MenuItemAction.perform(player, menuItem);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);

                        if (shiftClicked)
                            Momentum.getMenuManager().removeShiftClicked(playerStats);

                    } else {
                        // submitted plots section
                        String submittedPlotsTitle = Momentum.getMenuManager().getMenu("submitted-plots").getFormattedTitleBase();

                    /*
                        Submitted Plots GUI
                     */
                        if (menu.getFormattedTitleBase().equalsIgnoreCase(submittedPlotsTitle)) {
                            if (currentItem.getType() == Material.SKULL_ITEM) {

                                String[] split = currentItem.getItemMeta().getDisplayName().split("'");
                                Plot plot = Momentum.getPlotsManager().get(ChatColor.stripColor(split[0]));

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
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event)
    {
        RaceManager raceManager = Momentum.getRaceManager();
        MenuManager menuManager = Momentum.getMenuManager();
        String name = event.getPlayer().getName();

        // remove if present
        menuManager.removeChoosingRating(name);

        // remove if present
        raceManager.removeChoosingRaceLevel(name);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Inventory openedInventory = event.getInventory();
                Inventory nextInventory = event.getPlayer().getOpenInventory().getTopInventory();

                if (!openedInventory.getName().equalsIgnoreCase(nextInventory.getName()))
                {
                    LevelManager levelManager = Momentum.getLevelManager();

                    // remove buying
                    levelManager.removeBuyingLevel(name);

                    // cancelled tasks
                    CancelTasks cancelTasks = menuManager.getCancelTasks(name);

                    // if not null and contains, we need to cancel remaining tasks!
                    if (cancelTasks != null && cancelTasks.getCancelledSlots() != null)
                    {
                        for (BukkitTask task : cancelTasks.getCancelledSlots())
                            task.cancel();

                        menuManager.removeCancelTasks(name); // remove
                    }
                }
            }
        }.runTaskLater(Momentum.getPlugin(), 1);
    }

    @EventHandler
    public void onMenuItemUse(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world) &&
           (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK ||
            event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
        {

            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            if (playerStats.isLoaded())
            {
                if (!playerStats.isInTutorial())
                {
                    Menu menu = Momentum.getMenuManager().getMenuFromSelectItem(player.getInventory().getItemInMainHand());

                    if (menu != null)
                        Momentum.getMenuManager().openInventory(playerStats, menu.getName(), false);
                }
                else
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial"));
                }
            }
        }
    }
}
