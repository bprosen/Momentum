package com.parkourcraft.Parkour.data.menus;

import org.bukkit.inventory.ItemStack;

public class MenuItem {

    private int slot;
    private ItemStack item;
    private String type;
    private String typeValue;

    MenuItem(Menu menu, MenuPage menuPage, int slot) {
        this.slot = slot;

        load(menu, menuPage);
    }

    private void load(Menu menu, MenuPage menuPage) {
        item = Menus_YAML.getItem(menu.getName(), menuPage.getPageNumber(), slot);
        type = Menus_YAML.getItemType(menu.getName(), menuPage.getPageNumber(), slot);
        typeValue = Menus_YAML.getItemTypeValue(menu.getName(), menuPage.getPageNumber(), slot);
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getType() {
        return type;
    }

    public String getTypeValue() {
        return typeValue;
    }

}
