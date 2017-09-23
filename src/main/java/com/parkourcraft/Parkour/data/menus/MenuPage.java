package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;

public class MenuPage extends Menu {

    public static MenuPage menuPage;

    private static int pageNumber;
    private static int rowCount;

    private static Map<Integer, MenuItem> pageItemsMap = new HashMap<>();

    public MenuPage() {
        menuPage = this;
    }

    public void load(int pageNumber) {
        this.pageNumber = pageNumber;
        rowCount = Menus_YAML.getRowCount(getName(), pageNumber);
        int slotCount = rowCount * 9;

        for (int slot = 0; slot <= slotCount - 1; slot++) {
            if (Menus_YAML.hasItem(getName(), pageNumber, slot)) {
                MenuItem menuItem = new MenuItem();

                menuItem.load(slot);

                pageItemsMap.put(slot, menuItem);
            }
        }
    }

    public static void formatInventory(PlayerStats playerStats, InventoryView inventory) {
        for (MenuItem menuItem : pageItemsMap.values())
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(playerStats, menuItem));
    }

    public static int getPageNumber() {
        return pageNumber;
    }

    public static int getRowCount() {
        return rowCount;
    }

}
