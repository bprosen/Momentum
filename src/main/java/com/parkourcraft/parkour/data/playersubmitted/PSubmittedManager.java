package com.parkourcraft.parkour.data.playersubmitted;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

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
}
