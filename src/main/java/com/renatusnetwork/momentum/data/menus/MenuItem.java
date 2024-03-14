package com.renatusnetwork.momentum.data.menus;

import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuItem {

    private String name;
    private int pageNumber;
    private int slot;
    private ItemStack item;
    private String title;
    private String type;
    private String typeValue;
    private boolean glow = false;
    private List<String> lore;
    private List<String> commands;
    private List<String> consoleCommands;

    MenuItem(Menu menu, MenuPage menuPage, int slot) {
        this.name = menu.getName();
        this.pageNumber = menuPage.getPageNumber();
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
        glow = MenusYAML.getGlow(menu.getName(), menuPage.getPageNumber(), slot);
    }

    public String getMenuName()
    {
        return name;
    }

    public int getPageNumber() { return pageNumber; }

    public int getSlot() {
        return slot;
    }

    public boolean isGlowing() { return glow; }

    public ItemStack getItem() {
        return item;
    }

    public String getType() {
        return type;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public void setTypeValue(String newTypeValue) { typeValue = newTypeValue; }

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
