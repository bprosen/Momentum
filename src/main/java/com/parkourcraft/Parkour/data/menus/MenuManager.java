package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {

    private Map<String, Menu> menuMap = new HashMap<>();

    public MenuManager(Plugin plugin) {
        load();
        startScheduler(plugin);
    }

    public void load() {
        menuMap = new HashMap<>();

        for (String menuName : Menus_YAML.getNames())
            load(menuName);
    }

    private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                updateOpenInventories();
            }
        }, 0L, 10L);
    }

    public void load(String menuName) {
        if (Menus_YAML.exists(menuName))
            menuMap.put(menuName, new Menu(menuName));
    }

    public boolean exists(String menuName) {
        return menuMap.containsKey(menuName);
    }

    public Menu getMenu(String menuName) {
        return menuMap.get(menuName);
    }

    public Menu getMenuFromStartingChars(String input) {
        for (Menu menu : menuMap.values())
            if (input.startsWith(menu.getName()))
                return menu;

        return null;
    }

    public Menu getMenuFromTitle(String menuTitle) {
        for (Menu menu : menuMap.values())
            if (menuTitle.startsWith(menu.getFormattedTitleBase()))
                return menu;

        return null;
    }

    public Menu getMenuFromSelectItem(ItemStack item) {
        if (item != null)
            for (Menu menu : menuMap.values())
                if (menu.getSelectItem() != null
                        && menu.getSelectItem().getType().equals(item.getType()))
                    return menu;

        return null;
    }

    public List<String> getMenuNames() {
        return new ArrayList<>(menuMap.keySet());
    }

    public Inventory getInventory(String menuName, int pageNumber) {
        if (exists(menuName))
            return menuMap.get(menuName).getInventory(pageNumber);

        return null;
    }

    public void updateInventory(Player player, InventoryView inventory) {
        Menu menu = getMenuFromTitle(inventory.getTitle());

        if (menu != null)
            menu.updateInventory(player, inventory, Utils.getTrailingInt(inventory.getTitle()));
    }

    public void updateInventory(Player player, InventoryView inventory, String menuName, int pageNumber) {
        if (exists(menuName))
            menuMap.get(menuName).updateInventory(player, inventory, pageNumber);
    }

    public void updateOpenInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView inventoryView = player.getOpenInventory();

            if (inventoryView != null) {
                Menu menu = getMenuFromTitle(inventoryView.getTitle());

                if (menu != null
                        && menu.isUpdating()) {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    updateInventory(
                            player,
                            inventoryView,
                            menu.getName(),
                            Utils.getTrailingInt(inventoryView.getTitle())
                    );
                }
            }

        }
    }

}
