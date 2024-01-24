package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuItem
{
    private MenuPage menuPage;
    private int slot;
    private ItemStack item;
    private String title;
    private String type;
    private String typeValue;
    private MenuPage openOtherMenu;
    private boolean glow;
    private List<String> lore;
    private List<String> commands;
    private List<String> consoleCommands;
    public MenuItem(
            MenuPage menuPage,
            MenuPage openOtherMenu,
            int slot,
            ItemStack item,
            String title,
            String type,
            String typeValue,
            List<String> lore,
            List<String> commands,
            List<String> consoleCommands,
            boolean glow
    )
    {
        this.menuPage = menuPage;
        this.openOtherMenu = openOtherMenu;
        this.slot = slot;
        this.item = item;
        this.title = title;
        this.type = type;
        this.typeValue = typeValue;
        this.lore = lore;
        this.commands = commands;
        this.consoleCommands = consoleCommands;
        this.glow = glow;
    }

    public MenuItem(MenuPage menuPage, int slot)
    {
        this.menuPage = menuPage;
        this.slot = slot;

        load(menuPage.getMenu(), menuPage, String.valueOf(slot));
    }

    public MenuItem(MenuPage menuPage, int slot, int slotFrom, int slotTo)
    {
        this.menuPage = menuPage;
        this.slot = slot;

        load(menuPage.getMenu(), menuPage, slotFrom + "-" + slotTo);
    }

    public void load(Menu menu, MenuPage menuPage, String rangeWithin)
    {
        item = MenusYAML.getItem(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        title = MenusYAML.getItemTitle(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        type = MenusYAML.getItemType(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        typeValue = MenusYAML.getItemTypeValue(menu.getName(), menuPage.getPageNumber(), rangeWithin, type);
        lore = MenusYAML.getItemLore(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        commands = MenusYAML.getCommands(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        consoleCommands = MenusYAML.getConsoleCommands(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        glow = MenusYAML.getGlow(menu.getName(), menuPage.getPageNumber(), rangeWithin);
        openOtherMenu = MenusYAML.getOpenOtherMenu(menu.getName(), menuPage.getPageNumber(), rangeWithin);
    }

    public MenuPage getOpenMenu() { return openOtherMenu; }

    public boolean hasOpenMenu() { return openOtherMenu != null; }

    public MenuItem clone(MenuPage newPage, int newSlot)
    {
        return new MenuItem(newPage, openOtherMenu, newSlot, item, title, type, typeValue, lore, commands, consoleCommands, glow);
    }

    public Menu getMenu()
    {
        return menuPage.getMenu();
    }

    public void setPage(MenuPage menuPage) { this.menuPage = menuPage; }

    public MenuPage getPage() { return menuPage; }

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

    public boolean isLevel() { return type.equalsIgnoreCase("level"); }

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

    public boolean hasSpecificLore() { return !lore.isEmpty(); }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public boolean hasCommands() {
        return !(commands.isEmpty() && consoleCommands.isEmpty());
    }
}
