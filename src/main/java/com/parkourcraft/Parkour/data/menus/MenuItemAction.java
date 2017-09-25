package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.data.MenuManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuItemAction {

    public static void perform(Player player, MenuItem menuItem) {
        String itemType = menuItem.getType();

        if (itemType.equals("level")) {
            LevelObject level = LevelManager.get(menuItem.getTypeValue());

            player.closeInventory();
            player.teleport(level.getStartLocation());
            player.sendMessage(
                    ChatColor.GRAY + "You were teleported to the beginning of "
                            + level.getFormattedTitle()
            );
        } else if (itemType.equals("teleport")) {
            Location location = LocationManager.get(menuItem.getTypeValue());

            if (location != null) {
                player.closeInventory();
                player.teleport(location);
            }
        } else if (itemType.equals("open")) {
            Menu menu = MenuManager.getMenuFromStartingChars(menuItem.getTypeValue());

            if (menu != null) {
                int pageeNumber = Utils.getTrailingInt(menuItem.getTypeValue());

                Inventory inventory = MenuManager.getInventory(menu.getName(), pageeNumber);

                if (inventory != null) {
                    PlayerStats playerStats = StatsManager.get(player);

                    player.closeInventory();
                    player.openInventory(inventory);
                    MenuManager.updateInventory(playerStats, player.getOpenInventory(), menu.getName());
                }
            }
        }
    }
}
