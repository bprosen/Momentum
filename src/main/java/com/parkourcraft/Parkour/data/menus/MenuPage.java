package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;

public class MenuPage {

    private int pageNumber;
    private int rowCount;

    private Map<Integer, MenuItem> pageItemsMap = new HashMap<>();

    MenuPage(Menu menu, int pageNumber) {
        this.pageNumber = pageNumber;

        load(menu);
    }

    private void load(Menu menu) {
        rowCount = Menus_YAML.getRowCount(menu.getName(), pageNumber);
        int slotCount = rowCount * 9;

        for (int slot = 0; slot <= slotCount - 1; slot++) {
            if (Menus_YAML.hasItem(menu.getName(), pageNumber, slot))
                pageItemsMap.put(slot, new MenuItem(menu, this, slot));
        }
    }

    void formatInventory(PlayerStats playerStats, InventoryView inventory) {
        for (MenuItem menuItem : pageItemsMap.values())
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(playerStats, menuItem));
    }

    int getPageNumber() {
        return pageNumber;
    }

    int getRowCount() {
        return rowCount;
    }

}
