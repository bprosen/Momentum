package com.parkourcraft.Parkour.utils;

public class CheckInteger {

    public static boolean check(String input) {
        try {
            Integer.parseInt(input);
        } catch(Exception e) {
            return false;
        }
        return true;
    }

}
