package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

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

    public void loadMenuPage(int pageNumber) {
        this.pageNumber = pageNumber;

        String pagePath = menu.getName() + "." + pageNumber;

        if (menusConfig.isSet(pagePath)) {

            if (menusConfig.isSet(pagePath + ".row_count"))
                rowCount = menusConfig.getInt(pagePath + ".row_count");
            else
                rowCount = 1;

            for (int slot = 0; slot <= 53; slot++) {
                if (menusConfig.isSet(pagePath + "." + slot)) {
                    MenuItem menuItem = new MenuItem();

                    menuItem.loadMenuItem(slot);

                    pageItems.add(menuItem);
                }
            }
        }
    }

    public Inventory getInventory(PlayerStats playerStats) {
        Inventory inventory = Bukkit.createInventory(
                null,
                rowCount * 9,
                menuPage.getTitleFormatted()
        );

        for (MenuItem menuItem : pageItems)
            inventory.setItem(menuItem.getSlot(), menuItem.getFormattedItem(playerStats));

        return inventory;
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
