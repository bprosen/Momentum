package com.renatusnetwork.parkour.utils;

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
}
