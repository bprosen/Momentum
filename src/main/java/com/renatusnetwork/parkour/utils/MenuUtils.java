package com.renatusnetwork.parkour.utils;

import com.renatusnetwork.parkour.data.menus.Menu;
import com.renatusnetwork.parkour.data.menus.MenuHolder;
import com.renatusnetwork.parkour.data.menus.MenuPage;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashSet;

public class MenuUtils
{

    private static final HashSet<String> shiftClicked = new HashSet<>();

    public static void addShiftClicked(String playerName)
    {
        shiftClicked.add(playerName);
    }

    public static boolean containsShiftClicked(String playerName)
    {
        return shiftClicked.contains(playerName);
    }

    public static boolean removeShiftClicked(String playerName)
    {
        return shiftClicked.remove(playerName);
    }

    public static Inventory createInventory(MenuPage menuPage, int size, String title)
    {
        return Bukkit.createInventory(new MenuHolder(menuPage), size, title);
    }
}
