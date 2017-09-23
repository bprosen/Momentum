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

    private String name;
    private String title;
    private int pageCount;
    private boolean updating;
    private ItemStack selectItem;

    private Map<Integer, MenuPage> pageMap = new HashMap<>();

    public Menu(String menuName) {
        name = menuName;

        load();
    }

    private void load() {
        if (Menus_YAML.exists(name)) {

            title = Menus_YAML.getTitle(name);
            pageCount = Menus_YAML.getPageCount(name);
            updating = Menus_YAML.getUpdating(name);
            selectItem = Menus_YAML.getSelectItem(name);

            loadPages();
        }
    }

    private void loadPages() {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            if (Menus_YAML.isSet(name, pageNumber + ""))
                pageMap.put(pageNumber, new MenuPage(this, pageNumber));
        }
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public int getPageCount() {
        return pageCount;
    }

    public boolean isUpdating() {
        return updating;
    }

    public ItemStack getSelectItem() {
        return selectItem;
    }

    public Inventory getInventory(int pageNumber) {
        if (pageMap.containsKey(pageNumber)) {
            MenuPage menuPage = pageMap.get(pageNumber);

            return Bukkit.createInventory(null, menuPage.getRowCount() * 9, getFormattedTitle());
        }

        return Bukkit.createInventory(null, 9, getFormattedTitle());
    }

    public void updateInventory(PlayerStats playerStats, InventoryView inventory, int pageNumber) {
        if (pageMap.containsKey(pageNumber))
            pageMap.get(pageNumber).formatInventory(playerStats, inventory);
    }




















}
