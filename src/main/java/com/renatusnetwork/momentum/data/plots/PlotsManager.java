package com.renatusnetwork.momentum.data.plots;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotsManager {

    private HashMap<String, Plot> plotList;

    // no more deletion so this cached value will always be in sync with the id in the database
    private int currentMaxPlotID;
    private int currentRing;
    private int currentIndex;
    private Location lastPlotLocation;

    public PlotsManager() {
        this.plotList = new HashMap<>();
        load();
    }

    public void load() {
        plotList = PlotsDB.loadPlots();

        loadLastPlotFromDB();

        Momentum.getPluginLogger().info("Plots loaded: " + plotList.size());
    }

    public void loadLastPlotFromDB() {
        this.lastPlotLocation = PlotsDB.getLastPLotLocation();
        this.currentMaxPlotID = PlotsDB.getCurrentMaxPlotID();

        // if there is no last plot or the last plot is the origin then keep the defaults
        if (lastPlotLocation == null || (lastPlotLocation.getBlockX() == 0 && lastPlotLocation.getBlockZ() == 0)) {
            return;
        }

        // calculate current ring from how far the last plot location is from the origin
        this.currentRing = (int) Math.max(Math.abs(lastPlotLocation.getX()), Math.abs(lastPlotLocation.getZ())) / (Momentum.getSettingsManager().player_submitted_plot_buffer_width + Momentum.getSettingsManager().player_submitted_plot_width);

        // the current index within a ring starting at 0 from the bottom right corner going clockwise around the ring
        // is always in the range [0, 8 * currentRing)
        // so, the current index in a ring is given by subtracting from the max id (guaranteed to be the last inserted plot)
        // the sum of all the plot locations before it which is given by
        // 2 + 8 * (sum of all natural numbers up to currentRing - 1)
        // using the arithmetic series formula yields
        // 4 * currentRing * (currentRing - 1) + 2
        this.currentIndex = currentMaxPlotID - (4 * currentRing * (currentRing - 1) + 2);
    }

    /* keep jic for development
    public Location getLastPlotLocation() {
        return lastPlotLocation;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getCurrentMaxPlotID() {
        return currentMaxPlotID;
    }

    public int getCurrentRing() {
        return currentRing;
    }
    */

    // player param version
    public void add(Player player) {
        plotList.put(player.getName(), new Plot(currentMaxPlotID, player, player.getLocation()));
    }

    public Plot get(String name) {
        return plotList.get(name);
    }

    public Plot getIgnoreCase(String name) {
        for (Map.Entry<String, Plot> entry : plotList.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public boolean exists(String playerName) {
        return get(playerName) != null;
    }

    public HashMap<String, Plot> getPlots() {
        return plotList;
    }

    // this needs to be a list due to #get(int)
    public List<Plot> getSubmittedPlots() {
        List<Plot> tempList = new ArrayList<>();

        for (Plot plot : plotList.values()) {
            if (plot.isSubmitted()) {
                tempList.add(plot);
            }
        }
        return tempList;
    }

    // creation algorithm
    public void createPlot(PlayerStats playerStats) {
        Location creationLoc;
        Player player = playerStats.getPlayer();

        creationLoc = getNextFreePlotLocation();

        creationLoc.setYaw(player.getLocation().getYaw());
        creationLoc.setPitch(player.getLocation().getPitch());

        // set bedrock -1 where they teleport
        creationLoc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
        buildOutline(creationLoc);

        playerStats.teleport(creationLoc.clone().add(0.5, 0, 0.5), false);

        // add data
        PlotsDB.addPlot(playerStats, creationLoc);
        currentMaxPlotID++;
        lastPlotLocation = creationLoc;

        add(player);
        player.sendMessage(Utils.translate("&7Your &aPlot &7has been created! &7Type &a/plot home &7to get back!"));
    }

    public Location getNextFreePlotLocation() {
        int plotWidthAndBuffer = Momentum.getSettingsManager().player_submitted_plot_buffer_width + Momentum.getSettingsManager().player_submitted_plot_width;
        int plotDefaultY = Momentum.getSettingsManager().player_submitted_plot_default_y;
        int currentRingRadius = currentRing * plotWidthAndBuffer;

        if (lastPlotLocation == null) {
            return new Location(Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world), 0, plotDefaultY, 0);
        }

        // this condition is to re-sync the current ring with the actual next free location (ring 1)
        // when creating the very 2nd plot
        if (currentRing == 0) {
            currentRing++;
            currentIndex = 0;
            return new Location(lastPlotLocation.getWorld(), plotWidthAndBuffer, plotDefaultY, plotWidthAndBuffer);
        }

        // if the last plot location is 1 plot location before the first in the ring then reset to next ring
        if (currentIndex == 8 * currentRing - 1) {
            currentRing++;
            currentIndex = 0;
            return new Location(lastPlotLocation.getWorld(), currentRingRadius + plotWidthAndBuffer, plotDefaultY, currentRingRadius + plotWidthAndBuffer);
        }

        Location nextFreePlotLocation = lastPlotLocation.clone();

        // given the index in the ring, the side or "section" in which the index lies is used to calculate direction
        // 0 < index <= 2 * side * currentRing => side = index / ( 2 * currentRing)
        // the side is either 0, 1, 2, or 3 corresponding to west, north, east, south in a spiraling pattern along the ring
        int side = currentIndex / (2 * currentRing);

        // the most significant bit of the side gives the axis
        // 0 for x, 1 for z
        int axis = side & 0b01;

        // the least significant bit of the side gives the direction within the axis (+ or -)
        int sign = 2 * ((side >> 1) & 0b01) - 1;

        // e.g. 0 = 0b00 gives -x axis
        //      3 = 0b11 gives +z axis

        currentIndex++;

        nextFreePlotLocation.add(plotWidthAndBuffer * (1 - axis) * sign, 0, plotWidthAndBuffer * axis * sign);
        return nextFreePlotLocation;
    }

    /* keep jic for development
    public void deleteAllPlots() {
        // PlotsDB.deleteAllPlots();
        for (Plot plot : this.plotList.values()) {
            clearPlot(plot);
            PlotsDB.removePlot(plot.getOwnerUUID(), false);
        }

        PlotsDB.resetAutoIncrement();
        plotList.clear();
        this.currentRing = 0;
        this.currentIndex = 0;
        this.currentMaxPlotID = 0;
        this.lastPlotLocation = null;
    }
    */

    public void addTrusted(Plot plot, String playerUUID) {
        plot.addTrusted(playerUUID);
        PlotsDB.addTrustedPlayer(plot.getPlotID(), playerUUID);
    }

    public void removeTrusted(Plot plot, String playerUUID) {
        plot.removeTrusted(playerUUID);
        PlotsDB.removeTrustedPlayer(plot.getPlotID(), playerUUID);
    }

    public void clearPlot(Plot plot) {
        WorldEdit api = WorldEdit.getInstance();

        if (api != null) {
            int plotWidth = Momentum.getSettingsManager().player_submitted_plot_width;

            double pos1X = (plot.getSpawnLoc().getBlockX() - (plotWidth / 2));
            double pos2X = (plot.getSpawnLoc().getBlockX() + (plotWidth / 2));
            double pos1Z = (plot.getSpawnLoc().getBlockZ() - (plotWidth / 2));
            double pos2Z = (plot.getSpawnLoc().getBlockZ() + (plotWidth / 2));

            LocalWorld world = new BukkitWorld(Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world));

            Vector pos1 = new Vector(pos1X, 0, pos1Z);
            Vector pos2 = new Vector(pos2X, 256, pos2Z);
            Vector spawnVector = new Vector(plot.getSpawnLoc().getBlockX(),
                                            plot.getSpawnLoc().clone().subtract(0, 1, 0).getBlockY(),
                                            plot.getSpawnLoc().getBlockZ());

            CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

            try {
                EditSession editSession = api.getEditSessionFactory().getEditSession(world, -1);
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
            Momentum.getPluginLogger().info("WorldEdit API found null in clearPlot");
        }

        buildOutline(plot.getSpawnLoc());
    }

    public void buildOutline(Location creationLocation) {
        WorldEdit api = WorldEdit.getInstance();

        if (api != null) {
            int plotWidth = Momentum.getSettingsManager().player_submitted_plot_width;
            int plotDefaultY = Momentum.getSettingsManager().player_submitted_plot_default_y;

            double pos1X = (creationLocation.getBlockX() - (plotWidth / 2));
            double pos2X = (creationLocation.getBlockX() + (plotWidth / 2));
            double pos1Z = (creationLocation.getBlockZ() - (plotWidth / 2));
            double pos2Z = (creationLocation.getBlockZ() + (plotWidth / 2));

            LocalWorld world = new BukkitWorld(Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world));

            Vector pos1 = new Vector(pos1X, plotDefaultY - 1, pos1Z);
            Vector pos2 = new Vector(pos2X, plotDefaultY - 1, pos2Z);

            CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

            try {
                EditSession editSession = api.getEditSessionFactory().getEditSession(world, -1);
                editSession.setFastMode(true);
                editSession.makeWalls(selection, new BlockPattern(Material.BEDROCK.getId()));
                editSession.flushQueue();

                editSession.setFastMode(false);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            Momentum.getPluginLogger().info("WorldEdit API found null in clearPlot");
        }
    }

    // get nearest plot from location
    public Plot getPlotInLocation(Location loc) {
        Plot nearestPlot = null;
        for (Plot plot : plotList.values()) {
            if (blockInPlot(loc, plot)) {
                nearestPlot = plot;
                break;
            }
        }
        return nearestPlot;
    }

    public void updatePlayerNameInPlot(String oldName, String newName) {
        Plot plot = get(oldName);

        if (plot != null) {
            // remove
            plotList.remove(oldName);

            // then set and add
            plot.setOwnerName(newName);
            plotList.put(newName, plot);
        }
    }

    public boolean blockInPlot(Location loc, Plot plot) {
        int maxX = plot.getSpawnLoc().getBlockX() + (Momentum.getSettingsManager().player_submitted_plot_width / 2);
        int maxZ = plot.getSpawnLoc().getBlockZ() + (Momentum.getSettingsManager().player_submitted_plot_width / 2);
        int minX = plot.getSpawnLoc().getBlockX() - (Momentum.getSettingsManager().player_submitted_plot_width / 2);
        int minZ = plot.getSpawnLoc().getBlockZ() - (Momentum.getSettingsManager().player_submitted_plot_width / 2);

        return loc.getBlockX() <= maxX && loc.getBlockX() >= minX && loc.getBlockZ() <= maxZ && loc.getBlockZ() >= minZ;
    }
}
