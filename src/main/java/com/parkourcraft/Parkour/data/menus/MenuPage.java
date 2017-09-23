package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.List;

public class MenuPage extends Menu {

    public static MenuPage menuPage;

    private int pageNumber;
    private int rowCount;

    private List<MenuItem> pageItems = new ArrayList<>();

    public MenuPage() {
        menuPage = this;
    }

    public void load(int pageNumber) {
        this.pageNumber = pageNumber;
        rowCount = Menus_YAML.getRowCount(menu.getName(), pageNumber);

        for (int slot = 0; slot <= 53; slot++) {
            if (Menus_YAML.hasItem(menu.getName(), pageNumber, slot)) {
                MenuItem menuItem = new MenuItem();

                menuItem.load(slot);

                pageItems.add(menuItem);
            }
        }

    }

    public void formatInventory(PlayerStats playerStats, InventoryView inventory) {
        for (MenuItem menuItem : pageItems)
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(playerStats, menuItem));
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getRowCount() {
        return rowCount;
    }

    public List<MenuItem> getPageItems() {
        return pageItems;
    }

}
