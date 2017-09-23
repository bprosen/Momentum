package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Menu {

    public static Menu menu;

    private String name;
    private String title;
    private int pageCount;
    private boolean updating;
    private ItemStack selectItem;

    private Map<Integer, MenuPage> menuPageMap = new HashMap<>();

    static FileConfiguration menusConfig = FileManager.getFileConfig("menus");

    public Menu() {
        menu = this;
    }

    public void loadMenu(String menuName) {
        this.name = menuName;

        if (menusConfig.isSet(name)) {
            String settingsPath = name + ".settings";

            if (menusConfig.isSet(settingsPath + ".title"))
                title = menusConfig.getString(settingsPath + ".title");
            else
                title = name;

            if (menusConfig.isSet(settingsPath + ".page_count"))
                pageCount = menusConfig.getInt(settingsPath + ".page_count");
            else
                pageCount = 1;

            if (menusConfig.isSet(settingsPath + ".updating"))
                updating = menusConfig.getBoolean(settingsPath + ".updating");
            else
                updating = false;

            if (menusConfig.isSet(settingsPath + ".select_item")) {
                String itemMaterial = "";
                int itemType = 0;

                if (menusConfig.isSet(settingsPath + ".select_item.material"))
                    itemMaterial = menusConfig.getString(settingsPath + ".select_item.material");

                if (menusConfig.isSet(settingsPath + ".select_item.type"))
                    itemType = menusConfig.getInt(settingsPath + ".select_item.type");

                selectItem = Utils.convertItem(itemMaterial, itemType);
            }

            loadPages();
        }
    }

    private void loadPages() {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            if (menusConfig.isSet(name + "." + pageNumber)) {
                MenuPage menuPage = new MenuPage();

                menuPage.loadMenuPage(pageNumber);

                menuPageMap.put(pageNumber, menuPage);
            }
        }
    }

    public Inventory getMenu(PlayerStats playerStats, int pageNumber) {
        if (menuPageMap.containsKey(pageNumber))
            return menuPageMap.get(pageNumber).getInventory(playerStats);
        return null;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleFormatted() {
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

}
