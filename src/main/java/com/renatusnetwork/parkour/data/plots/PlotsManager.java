package com.renatusnetwork.parkour.data.plots;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class PlotsManager {

    private HashMap<String, Plot> plotList = new HashMap<>();
    private Location lastPlotLocation;
    private Location nextFreePlotLocation;
    private PlotDirection currentDirection;

    public PlotsManager() {
        load();
    }

    public void load()
    {
        plotList = PlotsDB.loadPlots();
        loadNextFreePlotFromDB();

        Parkour.getPluginLogger().info("Plots Loaded: " + plotList.size());
    }

    private void loadNextFreePlotFromDB()
    {
        Location[] lastTwoPlots = PlotsDB.getLastTwoPlotLocations();
        Location lastPlot = lastTwoPlots[1];
        Location secondLastPlot = lastTwoPlots[0];
        int plotWidthAndBuffer = Parkour.getSettingsManager().player_submitted_plot_buffer_width + Parkour.getSettingsManager().player_submitted_plot_width;

        // ensure not null, can do directional difference
        if (lastPlot != null && secondLastPlot != null)
        {
            PlotDirection direction = PlotDirection.NORTH;

            if (lastPlot.getZ() < secondLastPlot.getZ())
                direction = PlotDirection.NORTH;
            else if (lastPlot.getX() > secondLastPlot.getX())
                direction = PlotDirection.EAST;
            else if (lastPlot.getZ() > secondLastPlot.getZ())
                direction = PlotDirection.SOUTH;
            else if (lastPlot.getX() < secondLastPlot.getX())
                direction = PlotDirection.WEST;

            Location clonedLastPlot = lastPlot.clone();

            Plot foundPlot = null;
            PlotDirection newDirection = null;
            // now need to get whatever direction is clockwise to it
            switch (direction)
            {
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
            if (foundPlot == null)
            {
                this.nextFreePlotLocation = clonedLastPlot;
                this.currentDirection = newDirection;
            }
            else
            {
                this.nextFreePlotLocation = lastPlot.clone().add(lastPlot.clone().subtract(secondLastPlot.clone()));
                this.nextFreePlotLocation.setY(Parkour.getSettingsManager().player_submitted_plot_default_y);
                this.currentDirection = direction;
            }

            this.lastPlotLocation = lastPlot;
        }
        // only have 1 plot in db
        else if (secondLastPlot == null && lastPlot != null)
        {
            this.nextFreePlotLocation = lastPlot.add(plotWidthAndBuffer, 0, 0);
            this.currentDirection = PlotDirection.SOUTH;
        }
        // no plots have been made
        else
        {
            this.nextFreePlotLocation = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world), 0, Parkour.getSettingsManager().player_submitted_plot_default_y, 0);
            this.currentDirection = PlotDirection.EAST;
        }
    }

    private void loadNextFreePlotLocation(Location oldLastPlot)
    {
        Location clonedLastPlot = lastPlotLocation.clone();
        int plotWidthAndBuffer = Parkour.getSettingsManager().player_submitted_plot_buffer_width + Parkour.getSettingsManager().player_submitted_plot_width;
        Plot foundPlot = null;
        PlotDirection newDirection = null;

        if (this.currentDirection != null)
        {
            // now need to get whatever direction is clockwise to it
            switch (this.currentDirection)
            {
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
        }
        else
        {
            this.nextFreePlotLocation = clonedLastPlot.clone().add(clonedLastPlot.clone().subtract(oldLastPlot.clone()));
            this.nextFreePlotLocation.setY(Parkour.getSettingsManager().player_submitted_plot_default_y);
            this.currentDirection = newDirection;
        }

        // found the plot loc
        if (foundPlot == null)
        {
            this.nextFreePlotLocation = clonedLastPlot;
            this.currentDirection = newDirection;
        }
    }

    // player param version
    public void add(Player player)
    {
        plotList.put(player.getName(), new Plot(player, player.getLocation()));
    }

    public Plot get(String name) {
        return plotList.get(name);
    }

    public Plot getIgnoreCase(String name)
    {
        for (Map.Entry<String, Plot> entry : plotList.entrySet())
            if (entry.getKey().equalsIgnoreCase(name))
                return entry.getValue();

        return null;
    }

    public boolean exists(String playerName) {
        return get(playerName) != null;
    }

    public HashMap<String, Plot> getPlots() {
        return plotList;
    }

    public void remove(String playerName)
    {
        plotList.remove(playerName);
    }

    // this needs to be a list due to #get(int)
    public List<Plot> getSubmittedPlots()
    {
        List<Plot> tempList = new ArrayList<>();

        for (Plot plot : plotList.values())
        {
            if (plot.isSubmitted())
                tempList.add(plot);
        }
        return tempList;
    }

    // creation algorithm
    public void createPlot(Player player)
    {
        Location loc = nextFreePlotLocation.clone();

        // generate next free plot location
        this.lastPlotLocation = loc;
        loadNextFreePlotLocation();

        loc.setYaw(player.getLocation().getYaw());
        loc.setPitch(player.getLocation().getPitch());

        // set bedrock -1 where they teleport
        loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);

        player.teleport(loc.clone().add(0.5, 0, 0.5));
        // add data
        PlotsDB.addPlot(player, loc);
        add(player);
        player.sendMessage(Utils.translate("&7Your &a&lPlot &7has been created! &7Type &a/plot home &7to get back!"));
    }

    public void deletePlot(Plot plot)
    {

        Player owner = Bukkit.getPlayer(plot.getOwnerName());

        // if they are in plot world, teleport them
        if (owner.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
            owner.teleport(Parkour.getLocationManager().getLobbyLocation());

        // clear plot!
        clearPlot(plot, true);

        // remove from cache and teleport to spawn
        plotList.remove(owner.getName());
        PlotsDB.removePlot(plot.getOwnerUUID());
    }

    public void clearPlot(Plot plot, boolean deletePlot)
    {
        WorldEdit api = WorldEdit.getInstance();

        if (api != null) {
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
                EditSession editSession = api.getEditSessionFactory().getEditSession(world, -1);
                editSession.setFastMode(true);
                editSession.setBlocks(selection, new BaseBlock(Material.AIR.getId()));
                editSession.flushQueue();

                // if plot isnt being deleted then regen the bedrock
                if (!deletePlot)
                {
                    editSession.setBlock(spawnVector, new BaseBlock(Material.BEDROCK.getId()));
                    editSession.flushQueue();
                }
                editSession.setFastMode(false);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        } else {
            Parkour.getPluginLogger().info("WorldEdit API found null in clearPlot");
        }
    }

    // get nearest plot from location
    public Plot getPlotInLocation(Location loc)
    {
        Plot nearestPlot = null;
        for (Plot plot : plotList.values())
        {
            if (blockInPlot(loc, plot))
            {
                nearestPlot = plot;
                break;
            }
        }
        return nearestPlot;
    }

    public void updatePlayerNameInPlot(String oldName, String newName)
    {
        Plot plot = get(oldName);

        if (plot != null)
        {
            // remove
            plotList.remove(oldName);

            // then set and add
            plot.setOwnerName(newName);
            plotList.put(newName, plot);
        }
    }

    public boolean blockInPlot(Location loc, Plot plot)
    {
        int maxX = plot.getSpawnLoc().getBlockX() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int maxZ = plot.getSpawnLoc().getBlockZ() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minX = plot.getSpawnLoc().getBlockX() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minZ = plot.getSpawnLoc().getBlockZ() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);

        return (loc.getBlockX() <= maxX && loc.getBlockX() >= minX && loc.getBlockZ() <= maxZ && loc.getBlockZ() >= minZ);
    }
}
