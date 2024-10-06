package com.renatusnetwork.momentum.data.plots;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlotsManager {

    private HashMap<String, Plot> plotList;

    private int currentMaxPlotID;
    private Location lastPlotLocation;
    private Location nextFreePlotLocation;
    private PlotDirection currentDirection;

    public PlotsManager() {
        this.plotList = new HashMap<>();
        load();
    }

    public void load() {
        plotList = PlotsDB.loadPlots();
        currentMaxPlotID = PlotsDB.getCurrentMaxPlotID();

        loadLastTwoPlotsFromDB();

        Momentum.getPluginLogger().info("Plots loaded: " + plotList.size());
    }

    public void loadLastTwoPlotsFromDB() {
        Location[] lastTwoPlots = PlotsDB.getLastTwoPlotLocations();
        Location lastPlot = lastTwoPlots[1];
        Location secondLastPlot = lastTwoPlots[0];

        this.lastPlotLocation = lastPlot;

        PlotDirection direction = PlotDirection.NORTH;
        // ensure not null, can do directional difference
        if (lastPlot != null && secondLastPlot != null) {
            if (lastPlot.getX() > secondLastPlot.getX()) {
                direction = PlotDirection.EAST;
            } else if (lastPlot.getZ() > secondLastPlot.getZ()) {
                direction = PlotDirection.SOUTH;
            } else if (lastPlot.getX() < secondLastPlot.getX()) {
                direction = PlotDirection.WEST;
            }
        }

        this.currentDirection = direction;

        if (lastPlotLocation != null) {
            loadNextFreePlot(lastPlotLocation);
        }
    }

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

    public void remove(String playerName) {
        plotList.remove(playerName);
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

        if (nextFreePlotLocation != null) {
            creationLoc = nextFreePlotLocation.clone();
        } else {
            creationLoc = new Location(Bukkit.getWorld(Momentum.getSettingsManager().player_submitted_world), 0, Momentum.getSettingsManager().player_submitted_plot_default_y, 0);
        }

        creationLoc.setYaw(player.getLocation().getYaw());
        creationLoc.setPitch(player.getLocation().getPitch());

        // set bedrock -1 where they teleport
        creationLoc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);

        playerStats.teleport(creationLoc.clone().add(0.5, 0, 0.5), false);

        // generate next free plot location
        loadNextFreePlot(creationLoc);

        currentMaxPlotID++;

        // add data
        PlotsDB.addPlot(playerStats, creationLoc);
        add(player);
        player.sendMessage(Utils.translate("&7Your &aPlot &7has been created! &7Type &a/plot home &7to get back!"));
    }

    public void loadNextFreePlot(Location newLastPlot) {
        Location clonedLastPlot = newLastPlot.clone();
        int plotWidthAndBuffer = Momentum.getSettingsManager().player_submitted_plot_buffer_width + Momentum.getSettingsManager().player_submitted_plot_width;
        Plot foundPlot = null;
        PlotDirection newDirection = this.currentDirection;

        // now need to get whatever direction is clockwise to it
        switch (newDirection) {
            case NORTH:
                foundPlot = getPlotInLocation(clonedLastPlot.add(plotWidthAndBuffer, 0, 0));
                newDirection = PlotDirection.EAST;
                break;
            case EAST:
                foundPlot = getPlotInLocation(clonedLastPlot.add(0, 0, plotWidthAndBuffer));
                newDirection = PlotDirection.SOUTH;
                break;
            case SOUTH:
                foundPlot = getPlotInLocation(clonedLastPlot.subtract(plotWidthAndBuffer, 0, 0));
                newDirection = PlotDirection.WEST;
                break;
            case WEST:
                foundPlot = getPlotInLocation(clonedLastPlot.subtract(0, 0, plotWidthAndBuffer));
                newDirection = PlotDirection.NORTH;
                break;
        }
        // found the plot loc
        if (foundPlot == null) {
            this.nextFreePlotLocation = clonedLastPlot;
        } else {
            this.nextFreePlotLocation = clonedLastPlot.add(clonedLastPlot.clone().subtract(this.lastPlotLocation));
            this.nextFreePlotLocation.setY(Momentum.getSettingsManager().player_submitted_plot_default_y);
        }
        this.currentDirection = newDirection;
        this.lastPlotLocation = newLastPlot;
    }

    public void deletePlot(Plot plot) {
        Player owner = Bukkit.getPlayer(plot.getOwnerName());

        // if they are in plot world, teleport them
        if (owner.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
            owner.teleport(Momentum.getLocationManager().getSpawnLocation());
        }

        // clear plot!
        clearPlot(plot, true);

        // remove from cache and teleport to spawn
        plotList.remove(owner.getName());

        new BukkitRunnable() {
            @Override
            public void run() {
                PlotsDB.removePlot(plot.getOwnerUUID(), false);

                // deleted last plot
                if (getPlotInLocation(lastPlotLocation) == null) {
                    loadLastTwoPlotsFromDB();
                }
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }

    public void addTrusted(Plot plot, String playerUUID) {
        plot.addTrusted(playerUUID);
        PlotsDB.addTrustedPlayer(plot.getPlotID(), playerUUID);
    }

    public void removeTrusted(Plot plot, String playerUUID) {
        plot.removeTrusted(playerUUID);
        PlotsDB.removeTrustedPlayer(plot.getPlotID(), playerUUID);
    }

    public void clearPlot(Plot plot, boolean deletePlot) {
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

                // if plot isnt being deleted then regen the bedrock
                if (!deletePlot) {
                    editSession.setBlock(spawnVector, new BaseBlock(Material.BEDROCK.getId()));
                    editSession.flushQueue();
                }
                editSession.setFastMode(false);
            } catch (MaxChangedBlocksException e) {
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
