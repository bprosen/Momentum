package com.parkourcraft.Parkour.menus;


import com.parkourcraft.Parkour.menus.objects.Menu;
import com.parkourcraft.Parkour.stats.objects.PlayerStats;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MenuManager {

    private static List<Menu> menuList = new ArrayList<>();

    private static FileConfiguration menusConfig = FileManager.getFileConfig("menus");

    public static void loadMenus() {
        menuList = new ArrayList<>();

        for (String menuName : menusConfig.getKeys(false)) {
            Menu menu = new Menu();

            menu.loadMenu(menuName);

            menuList.add(menu);
        }
    }

    public static Menu getMenu(String menuName) {
        for (Menu menu : menuList) {
            if (menu.getName().equals(menuName))
                return menu;
        }

        return null;
    }

    public static List<String> getMenuNames() {
        List<String> menuNamesList = new ArrayList<>();

        for (Menu menu : menuList)
            menuNamesList.add(menu.getName());

        return menuNamesList;
    }

    public static boolean menuExists(String menuName) {

        for (Menu menu : menuList) {
            if (menu.getName().equals(menuName))
                return true;
        }

        return false;
    }

    public static Inventory getInventory(PlayerStats playerStats, String menuName, int pageNumber) {
        if (menuExists(menuName))
            return getMenu(menuName).getMenu(playerStats, pageNumber);

        return null;
    }

}
