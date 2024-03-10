package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MenuManager
{

    private HashMap<String, Menu> menus;
    private HashMap<String, CancelTasks> cancelTasks;
    private HashSet<String> shiftClicked;
    private HashMap<String, Level> choosingRating;

    public MenuManager()
    {
        this.menus = new HashMap<>();
        this.cancelTasks = new HashMap<>();
        this.shiftClicked = new HashSet<>();
        this.choosingRating = new HashMap<>();
        load();
    }

    public void reload()
    {
        load();
        loadItems();
        Parkour.getLevelManager().loadLevelsInMenus();
        loadConnectedMenus();
    }

    public void load()
    {
        for (String menuName : MenusYAML.getNames())
            menus.put(menuName, new Menu(menuName));

        Parkour.getPluginLogger().info("Menus loaded: " + menus.size());
    }

    public void loadItems()
    {
        for (Menu menu : menus.values())
        {
            for (MenuPage page : menu.getPages())
            {
                page.load(menu);
            }
            // sort levels after page
            if (menu.haveSortedLevels())
                menu.sortLevels();
        }
    }

    public void loadConnectedMenus()
    {
        for (Menu menu : menus.values())
            menu.loadConnectedMenus();
    }

    public List<Menu> getMenus()
    {
        List<Menu> menuArray = new ArrayList<>();

        // add menus to list
        for (Menu menu : menus.values())
            if (menu != null)
                menuArray.add(menu);

        return menuArray;
    }

    public boolean exists(String menuName) {
        return menus.containsKey(menuName);
    }

    public CancelTasks getCancelTasks(String playerName)
    {
        return cancelTasks.get(playerName);
    }

    public boolean hasCancelTasks(String playerName)
    {
        return cancelTasks.containsKey(playerName);
    }

    public void removeCancelTasks(String playerName)
    {
        cancelTasks.remove(playerName);
    }

    public void addShiftClicked(PlayerStats playerStats)
    {
        shiftClicked.add(playerStats.getName());
    }

    public boolean containsShiftClicked(PlayerStats playerStats)
    {
        return shiftClicked.contains(playerStats.getName());
    }

    public void removeShiftClicked(PlayerStats playerStats)
    {
        shiftClicked.remove(playerStats.getName());
    }

    public void addChoosingRating(PlayerStats playerStats, Level level)
    {
        choosingRating.put(playerStats.getName(), level);
    }

    public Level getChoosingRating(PlayerStats playerStats)
    {
        return choosingRating.get(playerStats.getName());
    }

    public void removeChoosingRating(String playerName)
    {
        choosingRating.remove(playerName);
    }

    public Inventory createInventory(MenuPage menuPage, int size, String title)
    {
        return Bukkit.createInventory(new MenuHolder(menuPage), size, title);
    }

    public Set<Level> getLevelsFromMenuDeep(Menu inMenu, Menu clickedMenu)
    {
        Set<Level> levels = new HashSet<>();

        // init previous and add main menu so skipping isn't an option
        Set<String> previous = new HashSet<>();
        previous.add(inMenu.getName()); // add own menu to prevent infinite looping

        // added menus
        ArrayList<Menu> menus = new ArrayList<>();
        HashMap<Menu, Set<Level>> menuLevels = Parkour.getLevelManager().getMenuLevels();

        Menu currentMenu = clickedMenu;

        while (currentMenu != null)
        {
            // prevent infinite looping
            if (!previous.contains(currentMenu.getName()))
            {
                previous.add(currentMenu.getName());
                Set<Level> levelsInMenu = menuLevels.get(currentMenu);

                // add levels from that menu
                if (levelsInMenu != null && !levelsInMenu.isEmpty())
                    levels.addAll(levelsInMenu);

                // if the connected menus are level menus, add to queue
                for (Menu subMenu : currentMenu.getConnectedMenus())
                    if (menuLevels.containsKey(subMenu) && !subMenu.getName().equalsIgnoreCase(Parkour.getSettingsManager().main_menu_name))
                        menus.add(subMenu);
            }

            if (!menus.isEmpty())
            {
                // pop first element
                currentMenu = menus.get(0);
                menus.remove(currentMenu);
            }
            else
                currentMenu = null;
        }

        return levels;
    }

    public boolean hasCancelledItem(String playerName, int slot)
    {
        boolean result = false;

        CancelTasks cancelTask = cancelTasks.get(playerName);

        if (cancelTask != null)
            result = cancelTask.hasItemInSlot(slot);

        return result;
    }

    public void addActiveCancel(String playerName, Inventory inventory, int slot, ItemStack itemStack, BukkitTask task) {

        CancelTasks menu = cancelTasks.get(playerName);

        // if non null, add task
        if (menu != null)
            menu.addSlot(slot, itemStack, task);
        else
        {
            CancelTasks cancelled = new CancelTasks(inventory);
            cancelled.addSlot(slot, itemStack, task);

            cancelTasks.put(playerName, cancelled);
        }
    }

    public Menu getMenu(String menuName) {
        return menus.get(menuName);
    }

    public Menu getMenuFromSelectItem(ItemStack item) {
        if (item != null)
            for (Menu menu : menus.values())
                if (menu.getSelectItem() != null && menu.getSelectItem().getType().equals(item.getType()))
                    return menu;

        return null;
    }

    public List<String> getMenuNames() {
        return new ArrayList<>(menus.keySet());
    }

    public Inventory getInventory(PlayerStats playerStats, String menuName, int pageNumber)
    {
        Menu menu = menus.get(menuName);

        return menu != null ? menu.getInventory(playerStats, pageNumber) : null;
    }

    public void openInventory(PlayerStats playerStats, Player opener, String menuName, int pageNumber, boolean showError)
    {
        Inventory inventory = getInventory(playerStats, menuName, pageNumber);

        if (inventory != null)
        {
            opener.openInventory(inventory);
            updateInventory(playerStats, opener.getOpenInventory(), menuName, pageNumber);
            opener.playSound(opener.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);
        }
        else if (showError)
            playerStats.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
    }

    public void openInventory(PlayerStats playerStats, String menuName, boolean showError)
    {
        openInventory(playerStats, playerStats.getPlayer(), menuName, 1, showError);
    }

    public void openInventory(PlayerStats playerStats, Player opener, String menuName, boolean showError)
    {
        openInventory(playerStats, opener, menuName, 1, showError);
    }

    public void updateInventory(PlayerStats playerStats, InventoryView inventory, String menuName, int pageNumber)
    {
        Menu menu = menus.get(menuName);

        if (menu != null)
            menu.updateInventory(playerStats, inventory, pageNumber);
    }

    public void updateInventory(PlayerStats playerStats, InventoryView inventory, MenuPage menuPage)
    {
        menuPage.getMenu().updateInventory(playerStats, inventory, menuPage.getPageNumber());
    }

    public void renameLevel(String oldLevelName, String newLevelName) {

        // variables once it has been found
        Menu correctMenu = null;
        MenuPage correctMenuPage = null;
        MenuItem correctMenuItem = null;

        // outer loop for menus
        outer: for (Menu menu : menus.values())
            // outer loop for menu pages
            for (MenuPage menuPage : menu.getPages())
                // inner loop for menu items in pages
                for (MenuItem menuItem : menuPage.getItems())
                    // check if they are equal, if so, break outer loop
                    if (menuItem.getTypeValue().equalsIgnoreCase(oldLevelName)) {
                        correctMenu = menu;
                        correctMenuPage = menuPage;
                        correctMenuItem = menuItem;
                        break outer;
                    }

        // now null check them all
        if (correctMenu != null && correctMenuPage != null && correctMenuItem != null) {
            correctMenuItem.setTypeValue(newLevelName);
            MenusYAML.setItemType(correctMenu.getName(), correctMenuPage.getPageNumber(), correctMenuItem.getSlot(), oldLevelName, newLevelName);
        }
    }

    // in ONE of TWO cases where a different GUI gets auto-filled, we have to use this special method that goes around the normal OOP menus
    public void openSubmittedPlotsGUI(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        Inventory inventory = getInventory(playerStats, "submitted-plots", 0);

        if (inventory != null)
        {
            List<Plot> submittedPlots = Parkour.getPlotsManager().getSubmittedPlots();
            for (int i = 0; i < submittedPlots.size() && i < inventory.getSize() - 9; i++)
            {
                Plot plot = submittedPlots.get(i);

                ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                ItemMeta skullMeta = item.getItemMeta();

                String plotOwnerName = plot.getOwnerName();

                skullMeta.setDisplayName(Utils.translate("&4" + plotOwnerName + "&c's Plot Submission"));

                List<String> itemLore = new ArrayList<String>() {{
                    add("");
                    add(Utils.translate("&7Click to teleport to"));
                    add(Utils.translate("&4" + plotOwnerName + "&c's Plot"));
                    add("");
                    add(Utils.translate("&7Awaiting &aaccept &7or &cdeny"));
                    add("");
                }};

                skullMeta.setLore(itemLore);
                item.setItemMeta(skullMeta);

                // non null
                if (item != null)
                    inventory.setItem(i, item);
            }
            // make black glass at the bottom row
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++)
            {
                ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(Utils.translate("&8Renatus Network"));
                item.setItemMeta(itemMeta);

                inventory.setItem(i, item);
            }
            player.openInventory(inventory);
        }
        else
        {
            player.sendMessage(Utils.translate("&cUnable to open inventory, null?"));
        }
    }
}
