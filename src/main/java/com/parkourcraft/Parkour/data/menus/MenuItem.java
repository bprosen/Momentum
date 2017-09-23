package com.parkourcraft.Parkour.data.menus;

import org.bukkit.inventory.ItemStack;

public class MenuItem extends MenuPage {

    private int slot;
    private ItemStack item;
    private String type;
    private String typeValue;

    MenuItem(int slot) {
        load(slot);
    }

    public void load(int slot) {
        this.slot = slot;

        item = Menus_YAML.getItem(menu.getName(), menuPage.getPageNumber(), slot);
        type = Menus_YAML.getItemType(menu.getName(), menuPage.getPageNumber(), slot);
        typeValue = Menus_YAML.getItemTypeValue(menu.getName(), menuPage.getPageNumber(), slot);
    }

    int getSlot() {
        return slot;
    }

    ItemStack getItem() {
        return item;
    }

    String getType() {
        return type;
    }

    String getTypeValue() {
        return typeValue;
    }

}
