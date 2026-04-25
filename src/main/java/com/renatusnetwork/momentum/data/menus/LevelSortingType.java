package com.renatusnetwork.momentum.data.menus;

import org.apache.commons.lang.StringUtils;

public enum LevelSortingType {
    NEWEST,
    OLDEST,
    ALPHABETICAL,
    EASIEST,
    HARDEST,
    LOWEST_REWARD,
    HIGHEST_REWARD,
    LOWEST_RATING,
    HIGHEST_RATING;

    public static LevelSortingType getNext(LevelSortingType type) {

        switch (type) {
            case NEWEST:
                return OLDEST;
            case OLDEST:
                return ALPHABETICAL;
            case ALPHABETICAL:
                return EASIEST;
            case EASIEST:
                return HARDEST;
            case HARDEST:
                return LOWEST_REWARD;
            case LOWEST_REWARD:
                return HIGHEST_REWARD;
            case HIGHEST_REWARD:
                return LOWEST_RATING;
            case LOWEST_RATING:
                return HIGHEST_RATING;
            case HIGHEST_RATING:
                return NEWEST;
        }

        return null;
    }

    public static String toString(LevelSortingType type) {
        String[] split = type.toString().toLowerCase().split("_");
        String newString = "";

        for (String splitString : split) {
            newString += StringUtils.capitalize(splitString) + " ";
        }

        return newString.substring(0, newString.length() - 1);
    }
}
