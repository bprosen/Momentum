package com.renatusnetwork.momentum.data.menus.gui;

import com.renatusnetwork.momentum.data.menus.MenuItemFormatter;
import com.renatusnetwork.momentum.data.menus.MenusYAML;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.InventoryView;

import java.util.*;

public class MenuPage {

    private Menu menu;
    private int pageNumber;
    private int rowCount;

    private HashMap<Integer, MenuItem> pageItemsMap;

    public MenuPage(Menu menu, int pageNumber, int rowCount, HashMap<Integer, MenuItem> items) {
        this.menu = menu;
        this.pageNumber = pageNumber;
        this.rowCount = rowCount;
        this.pageItemsMap = items;
    }

    public MenuPage(Menu menu, int pageNumber) {
        this.menu = menu;
        this.pageNumber = pageNumber;
        this.pageItemsMap = new HashMap<>();
        this.rowCount = MenusYAML.getRowCount(menu.getName(), pageNumber);
    }

    public Menu getMenu() {
        return menu;
    }

    public MenuPage clone(int newPageNumber, HashMap<Integer, MenuItem> newItems) {
        HashMap<Integer, MenuItem> items = new HashMap<>();

        // copy over rest of items
        for (Map.Entry<Integer, MenuItem> oldItems : pageItemsMap.entrySet()) {
            if (!newItems.containsKey(oldItems.getKey()))
            // clones ones that are not levels
            {
                items.put(oldItems.getKey(), oldItems.getValue().clone(oldItems.getValue().getPage(), oldItems.getValue().getSlot()));
            }
        }

        items.putAll(newItems);

        return new MenuPage(menu, newPageNumber, rowCount, items);
    }

    public void load(Menu menu) {
        Set<String> pageKeys = MenusYAML.getKeys(menu.getName() + "." + pageNumber, false);

        for (String key : pageKeys) {
            // if it is a simple int (slot)
            if (Utils.isInteger(key)) {
                int slot = Integer.parseInt(key);
                pageItemsMap.put(slot, new MenuItem(this, slot));
            }
            // support for being able to mass set an item (ex: 0-15,24,19,4-2)
            else {
                List<Integer> slots = new ArrayList<>();
                String[] commaSplit = {key};

                if (key.contains(",")) {
                    commaSplit = key.split(",");
                }

                for (String subComma : commaSplit) {
                    if (subComma.contains("-")) {
                        String[] subRange = subComma.split("-");
                        // make sure each side of the - is an int
                        if (Utils.isInteger(subRange[0]) && Utils.isInteger(subRange[1])) {
                            int from = Integer.parseInt(subRange[0]);
                            int to = Integer.parseInt(subRange[1]);

                            for (int i = from; i <= to; i++) {
                                slots.add(i);
                            }
                        }
                    } else if (Utils.isInteger(subComma)) {
                        slots.add(Integer.parseInt(subComma));
                    }
                }

                for (Integer slot : slots) {
                    pageItemsMap.put(slot, new MenuItem(this, slot, key));
                }
            }
        }
    }

    public void formatInventory(PlayerStats playerStats, InventoryView inventory) {
        for (MenuItem menuItem : pageItemsMap.values()) {
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(
                    playerStats,
                    menuItem));
        }
    }

    public void setItem(MenuItem menuItem) {
        if (pageItemsMap.containsKey(menuItem.getSlot())) {
            pageItemsMap.replace(menuItem.getSlot(), menuItem);
        } else {
            pageItemsMap.put(menuItem.getSlot(), menuItem);
        }

        menuItem.setPage(this);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getRowCount() {
        return rowCount;
    }

    public MenuItem getMenuItem(int slot) {
        return pageItemsMap.get(slot);
    }

    public void setItems(Collection<MenuItem> items) {
        for (MenuItem menuItem : items) {
            setItem(menuItem);
        }
    }

    public Collection<MenuItem> getItems() {
        return pageItemsMap.values();
    }
}
