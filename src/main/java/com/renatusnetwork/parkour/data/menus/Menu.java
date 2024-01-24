package com.renatusnetwork.parkour.data.menus;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.MenuUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Menu
{
    private String name;
    private String title;
    private int pageCount;
    private ItemStack selectItem;
    private HashMap<Integer, MenuPage> pages;
    private HashMap<LevelSortingType, HashMap<Integer, MenuPage>> sortedPages;
    private HashSet<Menu> connectedMenus;

    private boolean sortLevelTypes;

    public Menu(String name)
    {
        this.name = name;
        this.pages = new HashMap<>();
        this.sortedPages = new HashMap<>();
        this.connectedMenus = new HashSet<>();

        load();
    }

    private void load()
    {
        if (MenusYAML.exists(name))
        {
            sortLevelTypes = MenusYAML.getSortedLevelTypes(name);
            title = MenusYAML.getTitle(name);
            pageCount = MenusYAML.getPageCount(name);
            selectItem = MenusYAML.getSelectItem(name);

            loadPages();
        }
    }

    private void loadPages()
    {
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++)
        {
            if (MenusYAML.isSet(name, pageNumber + ""))
                pages.put(pageNumber, new MenuPage(this, pageNumber));
        }
    }

    public boolean haveSortedLevels() { return sortLevelTypes; }

    public void sortLevels()
    {
        for (LevelSortingType type : LevelSortingType.values())
            sortLevels(type);
    }

    private void sortLevels(LevelSortingType sortingType)
    {
        HashMap<Level, MenuItem> levelsInMenu = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> slots = new HashMap<>();

        // parse into levels in menu
        ArrayList<Integer> pageSlots = new ArrayList<>();

        for (MenuPage page : pages.values())
        {
            for (MenuItem item : page.getItems())
            {
                if (item.isLevel())
                {
                    Level level = Parkour.getLevelManager().get(item.getTypeValue());
                    if (level != null)
                    {
                        levelsInMenu.put(level, item);
                        pageSlots.add(item.getSlot());
                    }
                }
            }
            // sort collection of slots
            Collections.sort(pageSlots);
            slots.put(page.getPageNumber(), pageSlots);

            // reset data
            pageSlots = new ArrayList<>();
        }

        if (!levelsInMenu.isEmpty())
        {
            HashMap<Integer, HashMap<Integer, MenuItem>> sortedLevels = new HashMap<>();
            HashSet<Level> addedLevels = new HashSet<>();

            int pageNumber = 1;
            int currentSlotIndex = 0; // get first slot

            HashMap<Integer, MenuItem> sortedPage = new HashMap<>();

            // go until we have them all sorted
            while (addedLevels.size() < levelsInMenu.size())
            {
                Level max = null;
                int maxSize = slots.get(pageNumber).size();

                for (Map.Entry<Level, MenuItem> entry : levelsInMenu.entrySet())
                {
                    Level currentLevel = entry.getKey();

                    // only continue if we do not already have the level
                    if (!addedLevels.contains(currentLevel))
                    {
                        // if null, just skip sorting
                        if (max == null)
                            max = currentLevel;
                        else
                        {
                            if (sortingType == LevelSortingType.NEWEST && currentLevel.getCreationTime() > max.getCreationTime())
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.OLDEST && currentLevel.getCreationTime() < max.getCreationTime())
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.ALPHABETICAL &&
                                    ChatColor.stripColor(currentLevel.getFormattedTitle()).compareToIgnoreCase(ChatColor.stripColor(max.getFormattedTitle())) < 0.0)
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.EASIEST &&
                                    currentLevel.hasDifficulty() && (!max.hasDifficulty() || currentLevel.getDifficulty() <= max.getDifficulty()))
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.HARDEST &&
                                    currentLevel.hasDifficulty() && (!max.hasDifficulty() || currentLevel.getDifficulty() > max.getDifficulty()))
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.HIGHEST_REWARD && currentLevel.getReward() > max.getReward())
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.LOWEST_REWARD && currentLevel.getReward() < max.getReward())
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.HIGHEST_RATING &&
                                    currentLevel.hasRating() && (!max.hasRating() || currentLevel.getRating() > max.getRating()))
                                max = currentLevel;
                            else if (sortingType == LevelSortingType.LOWEST_RATING &&
                                    currentLevel.hasRating() && (!max.hasRating() || currentLevel.getRating() <= max.getRating()))
                                max = currentLevel;
                        }
                    }
                }
                // if not null by the end, then add
                if (max != null)
                {
                    int newSlot = slots.get(pageNumber).get(currentSlotIndex);

                    sortedPage.put(newSlot, levelsInMenu.get(max).clone(pages.get(pageNumber), newSlot)); // need to clone it to the new page number and slot
                    addedLevels.add(max);
                    currentSlotIndex++;
                }

                // if it is time to add a new page
                if (maxSize <= sortedPage.size())
                {
                    sortedLevels.put(pageNumber, sortedPage);

                    pageNumber++;

                    // reset data
                    currentSlotIndex = 0;
                    sortedPage = new HashMap<>();
                }
            }

            HashMap<Integer, MenuPage> newSortedPages = new HashMap<>();

            // update the menu
            for (Map.Entry<Integer, HashMap<Integer, MenuItem>> entry : sortedLevels.entrySet())
            {
                MenuPage oldMenuPage = pages.get(entry.getKey());
                HashMap<Integer, MenuItem> newItems = entry.getValue();
                newSortedPages.put(entry.getKey(), oldMenuPage.clone(entry.getKey(), newItems));
            }
            sortedPages.put(sortingType, newSortedPages); // add to new sorted pages
        }
    }

    public void loadConnectedMenus()
    {
        for (MenuPage menuPage : pages.values())
            for (MenuItem menuItem : menuPage.getItems())
                if (menuItem.hasOpenMenu())
                {
                    Menu value = menuItem.getOpenMenu().getMenu();

                    if (value != null && !value.equals(this))
                        connectedMenus.add(value);
                }
    }

    public Set<Menu> getConnectedMenus()
    {
        return connectedMenus;
    }

    public MenuPage getPage(int pageNumber)
    {
        return pages.get(pageNumber);
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

    public ItemStack getSelectItem() {
        return selectItem;
    }

    public Inventory getInventory(PlayerStats playerStats, int pageNumber)
    {
        String title = getFormattedTitle(pageNumber);

        if (sortLevelTypes)
        {
            HashMap<Integer, MenuPage> sortedMenu = sortedPages.get(playerStats.getLevelSortingType());

            if (sortedMenu != null && sortedMenu.containsKey(pageNumber))
            {
                MenuPage menuPage = sortedMenu.get(pageNumber);

                return MenuUtils.createInventory(menuPage, menuPage.getRowCount() * 9, title);
            }
        }

        if (pages.containsKey(pageNumber))
        {
            MenuPage menuPage = pages.get(pageNumber);

            return MenuUtils.createInventory(menuPage, menuPage.getRowCount() * 9, title);
        }

        return MenuUtils.createInventory(getPage(1),  54, title);
    }

    public void updateInventory(PlayerStats playerStats, InventoryView inventory, int pageNumber)
    {
        if (sortLevelTypes)
        {
            HashMap<Integer, MenuPage> sortedMenu = sortedPages.get(playerStats.getLevelSortingType());

            if (sortedMenu.containsKey(pageNumber))
                sortedMenu.get(pageNumber).formatInventory(playerStats, inventory);
        }
        else if (pages.containsKey(pageNumber))
            pages.get(pageNumber).formatInventory(playerStats, inventory);
    }

    public MenuItem getMenuItem(PlayerStats playerStats, int pageNumber, int slot)
    {
        if (sortLevelTypes)
        {
            HashMap<Integer, MenuPage> sortedMenu = sortedPages.get(playerStats.getLevelSortingType());
            if (sortedMenu.containsKey(pageNumber))
                return sortedMenu.get(pageNumber).getMenuItem(slot);
        }

        if (pages.containsKey(pageNumber))
            return pages.get(pageNumber).getMenuItem(slot);

        return null;
    }

    public Collection<MenuPage> getPages() { return pages.values(); }

    public boolean equals(Menu menu) { return menu.getName().equalsIgnoreCase(name); }

}
