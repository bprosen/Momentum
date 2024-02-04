package com.renatusnetwork.parkour.gameplay.listeners;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.menus.*;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.races.RaceManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
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
                            Parkour.getMenuManager().addShiftClicked(player.getName());

                        MenuItemAction.perform(player, menuItem);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);

                        if (shiftClicked)
                            Parkour.getMenuManager().removeShiftClicked(player.getName());

                    } else {
                        // submitted plots section
                        String submittedPlotsTitle = Parkour.getMenuManager().getMenu("submitted-plots").getFormattedTitleBase();

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
        RaceManager raceManager = Parkour.getRaceManager();
        MenuManager menuManager = Parkour.getMenuManager();
        String name = event.getPlayer().getName();

        // remove if present
        raceManager.removeChoosingRaceLevel(name);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Inventory openedInventory = event.getInventory();
                Inventory nextInventory = event.getPlayer().getOpenInventory().getTopInventory();

                if (openedInventory instanceof MenuHolder && nextInventory instanceof MenuHolder)
                {
                    Menu openedMenu = ((MenuHolder) openedInventory).getMenu();
                    Menu nextMenu = ((MenuHolder) nextInventory).getMenu();

                    if (!openedMenu.equals(nextMenu))
                    {
                        // remove buying
                        if (levelManager.isBuyingLevelMenu(name))
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
