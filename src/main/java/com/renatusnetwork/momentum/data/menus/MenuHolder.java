package com.renatusnetwork.momentum.data.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder
{
    private MenuPage menuPage;

    public MenuHolder(MenuPage menuPage)
    {
        this.menuPage = menuPage;
    }

    public MenuPage getMenuPage()
    {
        return menuPage;
    }

    public Menu getMenu() { return menuPage.getMenu(); }

    @Override
    public Inventory getInventory()
    {
        return null;
    }
}
