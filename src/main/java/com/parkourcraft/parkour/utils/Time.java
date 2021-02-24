package com.parkourcraft.parkour.utils;

public class Time {

    public static String elapsed(int seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;
        String time = years + " Years " + months % 12 + " Months " + days % 30 + " Days " + hours % 24 + " Hours " + minutes % 60 + " Minutes " + seconds % 60 + " Seconds ";
        if (years == 0L)
            time = time.replace("0 Years ", "");
        else if (years == 1L)
            time = time.replace("Years", "Year");
        if (months % 12 == 0L)
            time = time.replace("0 Months ", "");
        else if (months % 12 == 1L)
            time = time.replace("Months", "Month");
        if (days % 365 == 0L)
            time = time.replace("0 Days ", "");
        else if (days % 365 == 1L)
            time = time.replace("Days", "Day");
        if (hours % 24 == 0L)
            time = time.replace("0 Hours ", "");
        else if (hours % 24 == 1L)
            time = time.replace("Hours", "Hour");
        if (minutes % 60 == 0L)
            time = time.replace("0 Minutes ", "");
        else if (minutes % 60 == 1L)
            time = time.replace("Minutes", "Minute");
        if (seconds % 60 == 0L)
            time = time.replace("0 Seconds ", "");
        else if (seconds % 60 == 1L)
            time = time.replace("Seconds", "Second");
        return time;
    }

    public static String elapsedShortened(int seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;
        String time = years + "Y " + months % 12 + "M " + days % 30 + "D " + hours % 24 + "h " + minutes % 60 + "m ";
        if (years == 0L)
            time = time.replace("0Y ", "");
        if (months % 12 == 0L)
            time = time.replace("0M ", "");
        if (days % 365 == 0L)
            time = time.replace("0D ", "");
        if (hours % 24 == 0L)
            time = time.replace("0h ", "");
        if (minutes % 60 == 0L)
            time = time.replace("0m ", "");
        return time;
    }

    public static String elapsed(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;
        String time = years + " Years " + months % 12 + " Months " + days % 30 + " Days " + hours % 24 + " Hours " + minutes % 60 + " Minutes " + seconds % 60 + " Seconds ";
        if (years == 0L)
            time = time.replace("0 Years ", "");
        else if (years == 1L)
            time = time.replace("Years", "Year");
        if (months % 12 == 0L)
            time = time.replace("0 Months ", "");
        else if (months % 12 == 1L)
            time = time.replace("Months", "Month");
        if (days % 365 == 0L)
            time = time.replace("0 Days ", "");
        else if (days % 365 == 1L)
            time = time.replace("Days", "Day");
        if (hours % 24 == 0L)
            time = time.replace("0 Hours ", "");
        else if (hours % 24 == 1L)
            time = time.replace("Hours", "Hour");
        if (minutes % 60 == 0L)
            time = time.replace("0 Minutes ", "");
        else if (minutes % 60 == 1L)
            time = time.replace("Minutes", "Minute");
        if (seconds % 60 == 0L)
            time = time.replace("0 Seconds ", "");
        else if (seconds % 60 == 1L)
            time = time.replace("Seconds", "Second");
        return time;
    }

    public static String elapsedShortened(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;
        String time = years + "Y " + months % 12 + "M " + days % 30 + "D " + hours % 24 + "h " + minutes % 60 + "m ";
        if (years == 0L)
            time = time.replace("0Y ", "");
        if (months % 12 == 0L)
            time = time.replace("0M ", "");
        if (days % 365 == 0L)
            time = time.replace("0D ", "");
        if (hours % 24 == 0L)
            time = time.replace("0h ", "");
        if (minutes % 60 == 0L)
            time = time.replace("0m ", "");
        return time;
    }

    public static String elapsedSingle(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;
        String time = years + " Years " + months % 12 + " Months " + days % 30 + " Days " + hours % 24 + " Hours " + minutes % 60 + " Minutes " + seconds % 60 + " Seconds ";
        if (years == 0L)
            time = time.replace("0 Years ", "");
        else if (years == 1L)
            time = time.replace("Years", "Year");
        if (months % 12 == 0L)
            time = time.replace("0 Months ", "");
        else if (months % 12 == 1L)
            time = time.replace("Months", "Month");
        if (days % 365 == 0L)
            time = time.replace("0 Days ", "");
        else if (days % 365 == 1L)
            time = time.replace("Days", "Day");
        if (hours % 24 == 0L)
            time = time.replace("0 Hours ", "");
        else if (hours % 24 == 1L)
            time = time.replace("Hours", "Hour");
        if (minutes % 60 == 0L)
            time = time.replace("0 Minutes ", "");
        else if (minutes % 60 == 1L)
            time = time.replace("Minutes", "Minute");
        if (time.contains("Min")) {
            return time.replace(seconds % 60 + " Seconds", "");
        }
        if (seconds % 60 == 0L)
            time = time.replace("0 Seconds ", "");
        else if (seconds % 60 == 1L)
            time = time.replace("Seconds", "Second");
        return time;
    }

}
