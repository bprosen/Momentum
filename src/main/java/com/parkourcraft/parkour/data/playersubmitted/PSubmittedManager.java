package com.parkourcraft.parkour.data.playersubmitted;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

    // creation algorithm with CompletableFuture
    public void createPlot(Player player) {

        CompletableFuture.supplyAsync(() -> {
            return findNextFreePlot(player.getUniqueId().toString());
        }).thenAccept(result -> {
            Bukkit.broadcastMessage(result);
            if (result != null && !result.equalsIgnoreCase("")) {

                String[] split = result.split(":");
                // teleport to found plot!
                Location loc = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                               Double.parseDouble(split[0]), Parkour.getSettingsManager().player_submitted_plot_default_y,
                               Double.parseDouble(split[1]));

                // set bedrock -1 where they teleport
                loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
                player.teleport(loc);
                // add data
                add(player);
                PSubmitted_DB.addPlot(player, loc);
            }
        });
    }

    public void deletePlot(Player player) {

        Plot plot = getFromUUID(player.getUniqueId().toString());

        if (plot != null) {
            // remove from cache and teleport to spawn
            plotList.remove(plot);
            PSubmitted_DB.removePlot(player);
            player.teleport(Parkour.getSettingsManager().spawn_location);
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot!"));
        }
    }

    /*
      This method works in complete async and operates in an infinite (biggest int) loop.
      It will start at 0 + half plot width, 0 + half plot width then keeps track of which way it
      is going so it can check if there is a plot to the right of it based on direction. If there is no
      plot to the right, it turns right otherwise if there is a plot to the right, it moves forward.
     */
    private String findNextFreePlot(String playerUUID) {

        int max = Integer.MAX_VALUE;
        int plotWidth = Parkour.getSettingsManager().player_submitted_plot_width;

        List<String> plots = PSubmitted_DB.getPlotCenters();
        if (!plots.isEmpty()) {

            Bukkit.broadcastMessage("plots exist!");

            List<String> checkedLocs = new ArrayList<>();
            int x = plotWidth / 2;
            int z = plotWidth / 2;
            int direction = 1; // direction, 1 = north, 2 = east, 3 = south, 4 = west

            for (int i = 0; i < max; i++) {

                // check if current x and z are a plot center
                List<Map<String, String>> results = DatabaseQueries.getResults(
                        "plots",
                        "uuid, center_x, center_z",
                        " WHERE uuid='" + playerUUID + "'");

                if (results.isEmpty()) {
                    boolean isPlot = false;

                    Bukkit.broadcastMessage("player does not have plot!");

                    for (String plotString : plots) {
                        String[] split = plotString.split(":");
                        int plotX = Integer.parseInt(split[0]);
                        int plotZ = Integer.parseInt(split[1]);

                        // check if the x and z coords exist in database
                        if (plotX == x && plotZ == z) {
                            isPlot = true;
                            Bukkit.broadcastMessage("found plot for player");
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
                        Bukkit.broadcastMessage("changed direction and x and y");
                    // create plot
                    } else {
                        Bukkit.broadcastMessage("returns x and y");
                        return x + ":" + z;
                    }
                // already has plot
                } else {
                    Bukkit.broadcastMessage("already has plot");
                    return null;
                }
            }
        // first plot
        } else {
            Bukkit.broadcastMessage("first plot created!");
            return (plotWidth / 2) + ":" + (plotWidth / 2);
        }
        return null;
    }
}
