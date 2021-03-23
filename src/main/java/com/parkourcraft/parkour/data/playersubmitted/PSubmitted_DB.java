package com.parkourcraft.parkour.data.playersubmitted;

import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PSubmitted_DB {

    public static List<String> getPlotCenters() {

        // get all plots from database
        List<Map<String, String>> results = DatabaseQueries.getResults(
                "plots",
                "uuid, plot_id, center_x, center_y, center_z", "");

        List<String> tempList = new ArrayList<>();
        for (Map<String, String> result : results)
            tempList.add(result.get("center_x") + ":" + result.get("center_y") + ":" + result.get("center_z"));

        return tempList;
    }
}
