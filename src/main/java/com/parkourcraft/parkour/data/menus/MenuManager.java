package com.parkourcraft.parkour.data.menus;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class MenuManager {

    private HashMap<String, Menu> menuMap = new HashMap<>();

    public MenuManager() {
        load();
        //startScheduler(plugin);
    }

    public void load() {
        menuMap = new HashMap<>();

        for (String menuName : MenusYAML.getNames())
            load(menuName);
    }

    // disabled due to really not worth it for the micro-optimization lost from using it
    /*private void startScheduler(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                updateOpenInventories();
            }
        }, 0L, 10L);
    }*/

    public void load(String menuName) {
        if (MenusYAML.exists(menuName))
            menuMap.put(menuName, new Menu(menuName));
    }

    public boolean exists(String menuName) {
        return menuMap.containsKey(menuName);
    }

    public Menu getMenu(String menuName) {
        return menuMap.get(menuName);
    }

    public Menu getMenuFromStartingChars(String input) {
        for (Menu menu : menuMap.values())
            if (input.startsWith(menu.getName()))
                return menu;

        return null;
    }

    public Menu getMenuFromTitle(String menuTitle) {
        // need to make exception for rate level menu as the title gets changed to replace a placeholder
        for (Menu menu : menuMap.values())
            if (menuTitle.startsWith(menu.getFormattedTitleBase()) ||
               (menu.getName().equalsIgnoreCase("rate_level") && ChatColor.stripColor(menuTitle).contains("Rate")))
                return menu;

        return null;
    }

    public Menu getMenuFromSelectItem(ItemStack item) {
        if (item != null)
            for (Menu menu : menuMap.values())
                if (menu.getSelectItem() != null && menu.getSelectItem().getType().equals(item.getType()))
                    return menu;

        return null;
    }

    public List<String> getMenuNames() {
        return new ArrayList<>(menuMap.keySet());
    }

    public Inventory getInventory(String menuName, int pageNumber) {
        if (exists(menuName))
            return menuMap.get(menuName).getInventory(pageNumber);

        return null;
    }

    public void updateInventory(Player player, InventoryView inventory) {
        Menu menu = getMenuFromTitle(inventory.getTitle());

        if (menu != null)
            menu.updateInventory(player, inventory, Utils.getTrailingInt(inventory.getTitle()));
    }

    public void updateInventory(Player player, InventoryView inventory, String menuName, int pageNumber) {
        if (exists(menuName))
            menuMap.get(menuName).updateInventory(player, inventory, pageNumber);
    }

    public void renameLevel(String oldLevelName, String newLevelName) {

        // variables once it has been found
        Menu correctMenu = null;
        MenuPage correctMenuPage = null;
        MenuItem correctMenuItem = null;

        // outer loop for menus
        outer: for (Menu menu : menuMap.values())
            // outer loop for menu pages
            for (MenuPage menuPage : menu.getPageMap().values())
                // inner loop for menu items in pages
                for (MenuItem menuItem : menuPage.getPageItemsMap().values())
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
    public void openSubmittedPlotsGUI(Player player) {
        Inventory inventory = getInventory("submitted-plots", 0);

        if (inventory != null) {
            List<Plot> submittedPlots = Parkour.getPlotsManager().getSubmittedPlots();
            for (int i = 0; i < submittedPlots.size() && i < inventory.getSize() - 9; i++) {

                Plot plot = submittedPlots.get(i);
                // third byte is a player skull
                ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

                final String plotOwnerName = plot.getOwnerName();

                UUID targetUUID = UUID.fromString(plot.getOwnerUUID());
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(targetUUID));

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
                inventory.setItem(i, item);
            }
            // make black glass at the bottom row
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {
                ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(Utils.translate("&8Renatus Network"));
                item.setItemMeta(itemMeta);

                inventory.setItem(i, item);
            }
            player.openInventory(inventory);
        } else {
            player.sendMessage(Utils.translate("&cUnable to open inventory, null?"));
        }
    }

    // in ONE of TWO cases where a different GUI gets auto-filled, we have to use this special method that goes around the normal OOP menus
    public void openRaceLevelsGUI(Player player, Player target, double betAmount) {
        Inventory inventory = getInventory("pick-race-levels", 0);

        if (inventory != null) {
            RaceManager raceManager = Parkour.getRaceManager();
            List<String> notInUseRaceLevels = raceManager.getNotInUseRaceLevels();

            for (int i = 0; i < notInUseRaceLevels.size() && i < inventory.getSize() - 9; i++) {

                String levelName = notInUseRaceLevels.get(i);
                Level level = Parkour.getLevelManager().get(levelName);
                if (level != null) {

                    // if they have a menu item type configured, use it
                    Material raceMenuItem = level.getRaceLevelMenuItemType();
                    ItemStack item;
                    if (raceMenuItem != null)
                        item = new ItemStack(raceMenuItem);
                    else
                        item = new ItemStack(Material.QUARTZ_BLOCK);

                    ItemMeta itemMeta = item.getItemMeta();

                    itemMeta.setDisplayName(level.getFormattedTitle());
                    List<String> itemLore = new ArrayList<String>() {{
                        add(Utils.translate("&7Click to select &c" + level.getFormattedTitle()));
                        add(Utils.translate("&7for your race!"));
                        add(Utils.translate(""));
                        add(Utils.translate("&7Wins this Race Level Has &6" + level.getTotalCompletionsCount()));
                        add(Utils.translate("&7Racing Against &e-> &6" + target.getName()));
                    }};

                    // this means they put a bet!
                    if (betAmount > -1.0)
                        itemLore.add(Utils.translate("&7Bet Amount &e-> &6" + betAmount));

                    itemMeta.setLore(itemLore);
                    item.setItemMeta(itemMeta);
                    inventory.setItem(i, item);
                }
            }
            // make black glass at the bottom row
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {

                int middleSlot = inventory.getSize() - 5;
                ItemStack item;

                if (i == middleSlot) {
                    item = new ItemStack(Material.DIAMOND_BLOCK);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(Utils.translate("&c&lRandom Level"));

                    List<String> itemLore = new ArrayList<String>() {{
                        add(Utils.translate("&7Click me to select Random Level"));
                        add(Utils.translate(""));
                        add(Utils.translate("&7Racing Against &e-> &6" + target.getName()));
                    }};

                    if (betAmount > -1.0)
                        itemLore.add(Utils.translate("&7Bet Amount &e-> &6" + betAmount));

                    itemMeta.setLore(itemLore);

                    item.setItemMeta(itemMeta);
                } else {
                    item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 15);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(Utils.translate("&8Renatus Network"));
                    item.setItemMeta(itemMeta);
                }
                inventory.setItem(i, item);
            }
            player.openInventory(inventory);
        } else {
            player.sendMessage(Utils.translate("&cUnable to open inventory, null?"));
        }
    }

    /*public void updateOpenInventories() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView inventoryView = player.getOpenInventory();

            if (inventoryView != null) {
                Menu menu = getMenuFromTitle(inventoryView.getTitle());

                if (menu != null
                        && menu.isUpdating()) {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    updateInventory(
                            player,
                            inventoryView,
                            menu.getName(),
                            Utils.getTrailingInt(inventoryView.getTitle())
                    );
                }
            }
        }
    }*/
}
