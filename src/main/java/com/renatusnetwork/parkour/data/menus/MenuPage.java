package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.inventory.InventoryView;
import java.util.*;

public class MenuPage
{
    private Menu menu;
    private int pageNumber;
    private int rowCount;

    private HashMap<Integer, MenuItem> pageItemsMap;

    public MenuPage(Menu menu, int pageNumber, int rowCount, HashMap<Integer, MenuItem> items)
    {
        this.menu = menu;
        this.pageNumber = pageNumber;
        this.rowCount = rowCount;
        this.pageItemsMap = items;
    }

    public MenuPage(Menu menu, int pageNumber)
    {
        this.menu = menu;
        this.pageNumber = pageNumber;
        this.pageItemsMap = new HashMap<>();
        this.rowCount = MenusYAML.getRowCount(menu.getName(), pageNumber);
    }

    public Menu getMenu()
    {
        return menu;
    }

    public MenuPage clone(int newPageNumber, HashMap<Integer, MenuItem> newItems)
    {
        HashMap<Integer, MenuItem> items = new HashMap<>();

        // copy over rest of items
        for (Map.Entry<Integer, MenuItem> oldItems : pageItemsMap.entrySet())
            if (!newItems.containsKey(oldItems.getKey()))
                // clones ones that are not levels
                items.put(oldItems.getKey(), oldItems.getValue().clone(oldItems.getValue().getPage(), oldItems.getValue().getSlot()));

        items.putAll(newItems);

        return new MenuPage(menu, newPageNumber, rowCount, items);
    }

    public void load(Menu menu)
    {
        Set<String> pageKeys = MenusYAML.getKeys(menu.getName() + "." + pageNumber, false);

        for (String key : pageKeys)
        {
            // if it is a simple int (slot)
            if (Utils.isInteger(key))
            {
                int slot = Integer.parseInt(key);
                pageItemsMap.put(slot, new MenuItem(this, slot));
            }
            // support for being able to mass set an item (ex: 0-15)
            else if (key.contains("-"))
            {
                String[] splitKey = key.split("-");

                // make sure each side of the - is an int
                if (Utils.isInteger(splitKey[0]) && Utils.isInteger(splitKey[1]))
                {
                    int from = Integer.parseInt(splitKey[0]);
                    int to = Integer.parseInt(splitKey[1]);

                    for (int i = from; i <= to; i++)
                        pageItemsMap.put(i, new MenuItem(this, i, from, to));
                }
            }
        }
    }

    public void formatInventory(PlayerStats playerStats, InventoryView inventory)
    {
        for (MenuItem menuItem : pageItemsMap.values())
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(
                    playerStats,
                    menuItem));
    }

    public void setItem(MenuItem menuItem)
    {
        pageItemsMap.replace(menuItem.getSlot(), menuItem);
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

    public void setItems(Collection<MenuItem> items)
    {
        for (MenuItem menuItem : items)
            setItem(menuItem);
    }

    public Collection<MenuItem> getItems() { return pageItemsMap.values(); }
}
