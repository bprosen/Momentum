package com.parkourcraft.parkour.data.plots;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotsManager {

    private HashMap<String, Plot> plotList = new HashMap<>();

    public PlotsManager() {
        load();
    }

    public void load() {
        // loop through and add to cache
        for (String uuidString : PlotsDB.getPlotOwnerUUIDs()) {

            String playerName = PlotsDB.getPlotOwnerName(uuidString);
            String locString = PlotsDB.getPlotCenter(uuidString);
            String[] locSplit = locString.split(":");

            // get offline player object and update in db if their name changed
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
            if (!offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                PlotsDB.updatePlayerName(offlinePlayer.getName(), uuidString);
                playerName = offlinePlayer.getName();
            }

            // loc from database, 0.5 for center of block
            Location loc = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                    Double.parseDouble(locSplit[0]) + 0.5, Parkour.getSettingsManager().player_submitted_plot_default_y,
                    Double.parseDouble(locSplit[1]) + 0.5);

            add(playerName, uuidString, loc);
        }
        Parkour.getPluginLogger().info("Plots Loaded: " + plotList.size());
    }
    // player param version
    public void add(Player player) {
        Plot plot = new Plot(player, player.getLocation());
        plotList.put(player.getName(), plot);
    }

    // string ver
    public void add(String playerName, String playerUUID, Location spawnLoc) {
        Plot plot = new Plot(playerName, playerUUID, spawnLoc);
        plotList.put(playerName, plot);
    }

    public Plot get(String playerName) {
        return plotList.get(playerName);
    }

    public boolean exists(String playerName) {
        return (get(playerName) != null);
    }

    public HashMap<String, Plot> getPlots() {
        return plotList;
    }

    public void remove(String playerName) {
        if (exists(playerName))
            plotList.remove(playerName);
    }

    // this needs to be a list due to #get(int)
    public List<Plot> getSubmittedPlots() {
        List<Plot> tempList = new ArrayList<>();

        for (Plot plot : plotList.values()) {
            if (plot.isSubmitted())
                tempList.add(plot);
        }
        return tempList;
    }

    public void addPlotToMenu(Plot plot) {
        // TODO: add plot to "submitted-plots" GUI automatically through this method
    }

    // creation algorithm
    public void createPlot(Player player) {

        // run algorithm in async
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(Parkour.getPlugin(), () -> {

        String result = findNextFreePlot(player.getUniqueId().toString());

        // run teleport and creation in sync
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Parkour.getPlugin(), () -> {
            if (result != null && !result.equalsIgnoreCase("")) {

                // if they have a plot
                if (result.equalsIgnoreCase("hasPlot")) {
                    player.sendMessage(Utils.translate("&cYou already have a plot!"));
                    return;
                }

                String[] split = result.split(":");
                // teleport to found plot!

                Location loc = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                        Double.parseDouble(split[0]), Parkour.getSettingsManager().player_submitted_plot_default_y,
                        Double.parseDouble(split[1]), player.getLocation().getYaw(), player.getLocation().getPitch());

                // set bedrock -1 where they teleport
                loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);

                player.teleport(loc.clone().add(0.5, 0, 0.5));
                // add data
                PlotsDB.addPlot(player, loc);
                add(player);
                player.sendMessage(Utils.translate("&7Your &a&lPlot &7has been created!" +
                                                        " &7Type &a/plot home &7to get back!"));
                }
            });
        });
    }

    public void deletePlot(Player player) {

        Plot plot = get(player.getName());

        if (plot != null) {
            // if they are in plot world, teleport them
            if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
                player.teleport(Parkour.getLocationManager().getLobbyLocation());

            // clear plot!
            clearPlot(plot);

            // remove from cache and teleport to spawn
            plotList.remove(plot);
            PlotsDB.removePlot(player);
            player.sendMessage(Utils.translate("&cYou have deleted your plot"));
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot!"));
        }
    }

    public void clearPlot(Plot plot) {

        WorldEdit FAWEAPI = WorldEdit.getInstance();

        if (FAWEAPI != null) {
            int plotWidth = Parkour.getSettingsManager().player_submitted_plot_width;

            double pos1X = (plot.getSpawnLoc().getBlockX() - (plotWidth / 2));
            double pos2X = (plot.getSpawnLoc().getBlockX() + (plotWidth / 2));
            double pos1Z = (plot.getSpawnLoc().getBlockZ() - (plotWidth / 2));
            double pos2Z = (plot.getSpawnLoc().getBlockZ() + (plotWidth / 2));

            LocalWorld world = new BukkitWorld(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world));

            Vector pos1 = new Vector(pos1X, 0, pos1Z);
            Vector pos2 = new Vector(pos2X, 256, pos2Z);
            Vector spawnVector = new Vector(plot.getSpawnLoc().getBlockX(),
                                            plot.getSpawnLoc().clone().subtract(0, 1, 0).getBlockY(),
                                            plot.getSpawnLoc().getBlockZ());

            CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

            try {
                EditSession editSession = FAWEAPI.getInstance().getEditSessionFactory().getEditSession(world, -1);
                editSession.setFastMode(true);
                editSession.setBlocks(selection, new BaseBlock(Material.AIR.getId()));
                editSession.flushQueue();
                editSession.setBlock(spawnVector, new BaseBlock(Material.BEDROCK.getId()));
                editSession.flushQueue();
                editSession.setFastMode(false);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        } else {
            Parkour.getPluginLogger().info("FAWE API found null in clearPlot");
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
        // add buffer so plots will not touch eachother
        int plotWidth = Parkour.getSettingsManager().player_submitted_plot_width
                        + Parkour.getSettingsManager().player_submitted_plot_buffer_width;


        List<String> plots = PlotsDB.getPlotCenters();
        if (!plots.isEmpty()) {

            List<String> checkedLocs = new ArrayList<>();
            // so plots do not hug eachother
            int x = (plotWidth / 2);
            int z = (plotWidth / 2);
            int direction = 1; // direction, 1 = north, 2 = east, 3 = south, 4 = west

            for (int i = 0; i < max; i++) {

                // check if current x and z are a plot center
                List<Map<String, String>> results = DatabaseQueries.getResults(
                        "plots",
                        "uuid, center_x, center_z",
                        " WHERE uuid='" + playerUUID + "'");

                if (results.isEmpty()) {
                    boolean isPlot = false;

                    for (String plotString : plots) {
                        String[] split = plotString.split(":");
                        int plotX = Integer.parseInt(split[0]);
                        int plotZ = Integer.parseInt(split[1]);

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
                    return "hasPlot";
                }
            }
        // first plot
        } else {
            return (plotWidth / 2) + ":" + (plotWidth / 2);
        }
        return null;
    }

    // get nearest plot from location
    public Plot getNearestPlot(Location loc) {

        Plot nearestPlot = null;
        for (Plot plot : plotList.values()) {
            if (blockInPlot(loc, plot)) {
                nearestPlot = plot;
                break;
            }
        }
        return nearestPlot;
    }

    public boolean blockInPlot(Location loc, Plot plot) {

        int maxX = plot.getSpawnLoc().getBlockX() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int maxZ = plot.getSpawnLoc().getBlockZ() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minX = plot.getSpawnLoc().getBlockX() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minZ = plot.getSpawnLoc().getBlockZ() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);

        if (loc.getBlockX() <= maxX && loc.getBlockX() >= minX && loc.getBlockZ() <= maxZ && loc.getBlockZ() >= minZ)
            return true;
        return false;
    }
}
