package com.parkourcraft.parkour.data.playersubmitted;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PSubmittedManager {

    private List<Plot> plotList = new ArrayList<>();

    public PSubmittedManager() {
    }

    // player param version
    public void add(Player player) {
        Plot rank = new Plot(player, player.getLocation());
        plotList.add(rank);
    }

    // string ver
    public void add(String playerName, String playerUUID, Location spawnLoc) {
        Plot rank = new Plot(playerName, playerUUID, spawnLoc);
        plotList.add(rank);
    }

    public Plot get(String playerName) {
        for (Plot plot : plotList)
            if (plot.getOwnerName().equalsIgnoreCase(playerName))
                return plot;

        return null;
    }

    public Plot getFromUUID(String playerUUID) {
        for (Plot plot : plotList)
            if (plot.getOwnerUUID().equalsIgnoreCase(playerUUID))
                return plot;

        return null;
    }

    public boolean existsFromUUID(String playerUUID) {
        return (getFromUUID(playerUUID) != null);
    }

    public boolean exists(String playerName) {
        return (get(playerName) != null);
    }

    public List<Plot> getPlots() {
        return plotList;
    }

    public void remove(String playerUUID) {
        if (existsFromUUID(playerUUID))
            plotList.remove(playerUUID);
    }

    // creation algorithm
    public void createPlot(Player player) {

        CompletableFuture.supplyAsync(() -> {
            return findNextFreePlot(player.getUniqueId().toString());
        }).thenAccept(result -> {
            if (result != null) {

            }
        });
    }

    private String findNextFreePlot(String playerUUID) {

        int max = Integer.MAX_VALUE;
        int plotWidth = Parkour.getSettingsManager().player_submitted_plot_width;

        List<String> plots = PSubmitted_DB.getPlotCenters();
        if (!plots.isEmpty()) {

            List<String> checkedLocs = new ArrayList<>();
            int x = plotWidth / 2;
            int z = plotWidth / 2;
            int direction = 1; // direction, 1 = north, 2 = east, 3 = south, 4 = west

            for (int i = 0; i < max; i++) {

                // check if current x and z are a plot center
                List<Map<String, String>> results = DatabaseQueries.getResults(
                        "plots",
                        "uuid, plot_id, center_x, center_z",
                        " WHERE uuid='" + playerUUID + "'");

                if (!results.isEmpty()) {
                    boolean isPlot = false;

                    for (Map<String, String> result : results) {
                        int plotX = Integer.parseInt(result.get("center_x"));
                        int plotZ = Integer.parseInt(result.get("center_z"));

                        // check if the x and z coords exist in database
                        if (plotX == x && plotZ == z) {
                            isPlot = true;
                            break;
                        }
                    }
                    // if is a plot, keep going in algorithm
                    if (isPlot) {
                        switch (direction) {
                            // north
                            case 1:
                                if (checkedLocs.contains((x + plotWidth) + ":" + z))
                                    z -= plotWidth;
                                else
                                    x += plotWidth;
                            // east
                            case 2:
                                if (checkedLocs.contains(x + ":" + (z + plotWidth)))
                                    x += plotWidth;
                                else
                                    z += plotWidth;
                            // south
                            case 3:
                                if (checkedLocs.contains((x - plotWidth) + ":" + z))
                                    z += plotWidth;
                                else
                                    x -= plotWidth;
                            // west
                            case 4:
                                if (checkedLocs.contains(x + ":" + (z - plotWidth)))
                                    x -= plotWidth;
                                else
                                    z -= plotWidth;
                        }
                        if (direction % 4 > 0)
                            direction++;
                        else
                            direction = 1;

                        // add to checked locs
                        checkedLocs.add(x + ":" + z);
                    // create plot
                    } else {
                        return x + ":" + z;
                    }
                // already has plot
                } else {

                }
            }
        // first plot
        } else {
            return (plotWidth / 2) + ":" + (plotWidth / 2);
        }
        return null;
    }
}
