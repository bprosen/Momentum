package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Menu {

    public static Menu menu;

    private static String name;
    private static String title;
    private static int pageCount;
    private static boolean updating;
    private static ItemStack selectItem;

    private static Map<Integer, MenuPage> pageMap = new HashMap<>();

    public Menu() {
        menu = this;
    }

    public void load(String menuName) {
        name = menuName;

        if (Menus_YAML.exists(menuName)) {

            title = Menus_YAML.getTitle(menuName);
            pageCount = Menus_YAML.getPageCount(menuName);
            updating = Menus_YAML.getUpdating(menuName);
            selectItem = Menus_YAML.getSelectItem(menuName);

            loadPages();
        }
    }

    private void loadPages() {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            if (Menus_YAML.isSet(name, pageNumber + "")) {
                MenuPage menuPage = new MenuPage();

                menuPage.load(pageNumber);

                pageMap.put(pageNumber, menuPage);
            }
        }
    }

    public static String getName() {
        return name;
    }

    public static String getTitle() {
        return title;
    }

    public static String getFormattedTitle() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public static int getPageCount() {
        return pageCount;
    }

    public static boolean isUpdating() {
        return updating;
    }

    public static ItemStack getSelectItem() {
        return selectItem;
    }

    public static Inventory getInventory(int pageNumber) {
        if (pageMap.containsKey(pageNumber)) {
            MenuPage menuPage = pageMap.get(pageNumber);

            return Bukkit.createInventory(null, menuPage.getRowCount() * 9, getFormattedTitle());
        }

        return Bukkit.createInventory(null, 9, getFormattedTitle());
    }

    public static void updateInventory(PlayerStats playerStats, InventoryView inventory, int pageNumber) {
        if (pageMap.containsKey(pageNumber))
            pageMap.get(pageNumber).formatInventory(playerStats, inventory);
    }




















}
