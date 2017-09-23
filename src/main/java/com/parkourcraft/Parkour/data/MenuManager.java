package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.data.menus.Menu;
import com.parkourcraft.Parkour.data.menus.Menus_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {

    private static Map<String, Menu> menuMap = new HashMap<>();

    public static void load(String menuName) {
        if (Menus_YAML.exists(menuName)) {
            Menu menu = new Menu();

            menu.load(menuName);

            menuMap.put(menuName, menu);
        }
    }

    public static void loadMenus() {
        menuMap = new HashMap<>();

        for (String menuName : Menus_YAML.getNames())
            load(menuName);
    }

    public static boolean exists(String menuName) {
        return menuMap.containsKey(menuName);
    }

    public static Menu getMenu(String menuName) {
        return menuMap.get(menuName);
    }

    public static Menu getMenuFromTitle(String menuTitle) {
        for (Menu menu : menuMap.values()) {
            if (menuTitle.startsWith(menu.getFormattedTitle()))
                return menu;
        }

        return null;
    }

    public static List<String> getMenuNames() {
        return new ArrayList<>(menuMap.keySet());
    }

    public static Inventory getInventory(String menuName, int pageNumber) {
        if (exists(menuName))
            return menuMap.get(menuName).getInventory(pageNumber);

        return null;
    }

    public static void updateInventory(PlayerStats playerStats, InventoryView inventory) {
        Menu menu = getMenuFromTitle(inventory.getTitle());

        if (menu != null)
            menu.updateInventory(playerStats, inventory, 1);
    }

    public static void updateInventory(PlayerStats playerStats, InventoryView inventory, String menuName) {
        if (exists(menuName))
            menuMap.get(menuName).updateInventory(playerStats, inventory, 1);
    }

}
