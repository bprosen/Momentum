package com.renatusnetwork.momentum.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    private static String formatCompletionTimeNoSeconds(long milliseconds) {
        long minutes = milliseconds / 60000;
        long hours = minutes / 60;

        String hourString = "";
        String minuteString = "";

        if (hours > 0) {
            hourString = hours + ":";
        }

        if (minutes > 0) {
            minuteString = minutes < 10 && hours > 0 ? "0" + minutes : String.valueOf(minutes);
        }

        return hourString + minuteString;
    }

    public static String getDate(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

        return dateFormat.format(date);
    }

    public static String formatCompletionTimeTaken(long milliseconds, int decimalPlaces) {
        String time = formatCompletionTimeNoSeconds(milliseconds);

        double seconds = (milliseconds / 1000d) % 60;
        String secondsString = String.valueOf((int) seconds);
        if (decimalPlaces > 0) {
            DecimalFormat format = new DecimalFormat("#.#");
            format.setMaximumFractionDigits(decimalPlaces);
            format.setMinimumFractionDigits(1);

            secondsString = format.format(seconds);
        }

        if (!time.equalsIgnoreCase("")) {
            if (seconds < 10.0) {
                secondsString = time + ":0" + secondsString;
            } else {
                secondsString = time + ":" + secondsString;
            }
        }

        return secondsString;
    }

    public static String formatTimeWithSeconds(long milliseconds) {
        return formatTime(milliseconds) + ((milliseconds / 1000) % 60) + "s";
    }

    public static String formatTime(long milliseconds) {
        long minutes = milliseconds / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;

        String time = "";
        if (years > 0) {
            time += years + "Y ";
        }
        if (months > 0) {
            time += (months % 12) + "M ";
        }
        if (days > 0) {
            time += (days % 30) + "D ";
        }
        if (hours > 0) {
            time += (hours % 24) + "h ";
        }
        if (minutes > 0) {
            time += (minutes % 60) + "m ";
        }

        return time;
    }
}
