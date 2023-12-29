package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;
import java.util.Map;

public class MenuPage {

    private int pageNumber;
    private int rowCount;

    private HashMap<Integer, MenuItem> pageItemsMap;

    public MenuPage(Menu menu, int pageNumber)
    {
        this.pageNumber = pageNumber;
        this.pageItemsMap = new HashMap<>();

        load(menu);
    }

    private void load(Menu menu)
    {
        rowCount = MenusYAML.getRowCount(menu.getName(), pageNumber);
        int slotCount = rowCount * 9;

        for (int slot = 0; slot <= slotCount - 1; slot++)
        {
            if (MenusYAML.hasItem(menu.getName(), pageNumber, slot))
                pageItemsMap.put(slot, new MenuItem(menu, this, slot));
        }
    }

    void formatInventory(Player player, InventoryView inventory)
    {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        for (MenuItem menuItem : pageItemsMap.values())
            inventory.setItem(menuItem.getSlot(), MenuItemFormatter.format(
                    player,
                    playerStats,
                    menuItem));
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

    public MenuItem getMenuItemFromTitle(String itemTitle)
    {
        for (MenuItem menuItem : pageItemsMap.values())
        {
            if (menuItem.getItem().getItemMeta().getDisplayName().equals(itemTitle))
                return menuItem;

            if (menuItem.getType().equals("level"))
            {
                Level level = Parkour.getLevelManager().get(menuItem.getTypeValue());

                if (level != null && level.getFormattedTitle().equals(itemTitle))
                    return menuItem;
            }
        }

        return null;
    }

    public HashMap<Integer, MenuItem> getPageItemsMap() { return pageItemsMap; }
}
