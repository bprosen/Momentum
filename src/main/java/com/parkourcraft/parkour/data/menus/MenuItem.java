package com.parkourcraft.parkour.data.menus;

import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

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
        item = MenusYAML.getItem(menu.getName(), menuPage.getPageNumber(), slot);
        title = MenusYAML.getItemTitle(menu.getName(), menuPage.getPageNumber(), slot);
        type = MenusYAML.getItemType(menu.getName(), menuPage.getPageNumber(), slot);
        typeValue = MenusYAML.getItemTypeValue(menu.getName(), menuPage.getPageNumber(), slot);
        lore = MenusYAML.getItemLore(menu.getName(), menuPage.getPageNumber(), slot);
        commands = MenusYAML.getCommands(menu.getName(), menuPage.getPageNumber(), slot);
        consoleCommands = MenusYAML.getConsoleCommands(menu.getName(), menuPage.getPageNumber(), slot);
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
