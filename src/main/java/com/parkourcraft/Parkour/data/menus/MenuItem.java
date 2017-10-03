package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {

    private int slot;
    private ItemStack item;
    private String title;
    private String type;
    private String typeValue;
    private List<String> lore;
    private List<String> commands;
    private List<String> consoleCommands;

    MenuItem(Menu menu, MenuPage menuPage, int slot) {
        this.slot = slot;

        load(menu, menuPage);
    }

    private void load(Menu menu, MenuPage menuPage) {
        item = Menus_YAML.getItem(menu.getName(), menuPage.getPageNumber(), slot);
        title = Menus_YAML.getItemTitle(menu.getName(), menuPage.getPageNumber(), slot);
        type = Menus_YAML.getItemType(menu.getName(), menuPage.getPageNumber(), slot);
        typeValue = Menus_YAML.getItemTypeValue(menu.getName(), menuPage.getPageNumber(), slot);
        lore = Menus_YAML.getItemLore(menu.getName(), menuPage.getPageNumber(), slot);
        commands = Menus_YAML.getCommands(menu.getName(), menuPage.getPageNumber(), slot);
        consoleCommands = Menus_YAML.getConsoleCommands(menu.getName(), menuPage.getPageNumber(), slot);
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

    public String getFormattedTitle() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public List<String> getFormattedLore() {
        return Utils.formatLore(lore);
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public boolean hasCommands() {
        return (commands.size() > 0 || consoleCommands.size() > 0);
    }

}
