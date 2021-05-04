package com.parkourcraft.parkour.data.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
                pageMap.put(pageNumber, new MenuPage(this, pageNumber));
        }
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

    public Inventory getInventory(int pageNumber) {
        if (pageMap.containsKey(pageNumber)) {
            MenuPage menuPage = pageMap.get(pageNumber);

            return Bukkit.createInventory(null, menuPage.getRowCount() * 9, getFormattedTitle(pageNumber));
        }

        return Bukkit.createInventory(null, 54, getFormattedTitle(pageNumber));
    }

    public void updateInventory(Player player, InventoryView inventory, int pageNumber) {
        if (pageMap.containsKey(pageNumber))
            pageMap.get(pageNumber).formatInventory(player, inventory);
    }

    public MenuItem getMenuItemFromTitle(int pageNumber, String itemTitle) {
        if (pageMap.containsKey(pageNumber))
            return pageMap.get(pageNumber).getMenuItemFromTitle(itemTitle);

        return null;
    }

    public MenuItem getMenuItem(int pageNumber, int slot) {
        if (pageMap.containsKey(pageNumber))
            return pageMap.get(pageNumber).getMenuItem(slot);

        return null;
    }

}
