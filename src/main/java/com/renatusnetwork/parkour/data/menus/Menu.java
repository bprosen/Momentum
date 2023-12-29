package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.data.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Menu {

    private String name;
    private String title;
    private int pageCount;
    private boolean updating;

    private ItemStack selectItem;

    private Map<Integer, MenuPage> pages;

    public Menu(String name)
    {
        this.name = name;
        this.pages = new HashMap<>();

        load();
    }

    private void load() {
        if (MenusYAML.exists(name)) {

            title = MenusYAML.getTitle(name);
            pageCount = MenusYAML.getPageCount(name);
            updating = MenusYAML.getUpdating(name);
            selectItem = MenusYAML.getSelectItem(name);

            loadPages();
        }
    }

    private void loadPages() {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            if (MenusYAML.isSet(name, pageNumber + ""))
                pages.put(pageNumber, new MenuPage(this, pageNumber));
        }
    }

    public MenuPage getPage(int pageNumber)
    {
        return pages.get(pageNumber);
    }
    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getFormattedTitle(int pageNumber) {
        String menuTitle = ChatColor.translateAlternateColorCodes('&', title);

        if (pageCount > 1)
            menuTitle += ChatColor.GRAY + " Pg" + pageNumber;

        return menuTitle;
    }

    public String getFormattedTitleBase() {
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

    public Inventory getInventory(int pageNumber)
    {
        if (pages.containsKey(pageNumber))
        {
            MenuPage menuPage = pages.get(pageNumber);

            return Bukkit.createInventory(null, menuPage.getRowCount() * 9, getFormattedTitle(pageNumber));
        }

        return Bukkit.createInventory(null, 54, getFormattedTitle(pageNumber));
    }

    public void updateInventory(Player player, InventoryView inventory, int pageNumber)
    {
        if (pages.containsKey(pageNumber))
            pages.get(pageNumber).formatInventory(player, inventory);
    }

    public MenuItem getMenuItemFromTitle(int pageNumber, String itemTitle)
    {
        if (pages.containsKey(pageNumber))
            return pages.get(pageNumber).getMenuItemFromTitle(itemTitle);

        return null;
    }

    public MenuItem getMenuItem(int pageNumber, int slot)
    {
        if (pages.containsKey(pageNumber))
            return pages.get(pageNumber).getMenuItem(slot);

        return null;
    }

    public Collection<MenuPage> getPages() { return pages.values(); }
}
