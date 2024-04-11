package com.renatusnetwork.momentum.data.plots;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Plot {

    private int plotID;
    private String ownerName;
    private String ownerUUID;
    private Location spawnLoc;
    private List<String> trustedUUIDs;
    private boolean submitted;

    // add via player object (new plot)
    public Plot(int plotID, Player owner, Location spawnLoc)
    {
        this.plotID = plotID;
        this.ownerUUID = owner.getUniqueId().toString();
        this.ownerName = owner.getName();
        this.spawnLoc = spawnLoc;
        this.trustedUUIDs = new ArrayList<>();
        this.submitted = false;
    }

    // no player object addition (from db)
    public Plot(int plotID, String ownerName, String ownerUUID, Location spawnLoc, List<String> trustedUUIDs, boolean submitted)
    {
        this.plotID = plotID;
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
        this.spawnLoc = spawnLoc;
        this.trustedUUIDs = trustedUUIDs;
        this.submitted = submitted;
    }

    public int getPlotID() { return plotID; }

    public String getOwnerName() { return ownerName; }

    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public Location getSpawnLoc() {
        return spawnLoc;
    }

    public void teleportPlayerToEdge(Player player) {
        // teleport player if not null
        if (player != null) {
            // + 1 so they spawn OUTSIDE their plot, not at the border
            Location loc = spawnLoc.clone();
            loc.subtract(0, 0,(Momentum.getSettingsManager().player_submitted_plot_width / 2) + 1);
            loc.setPitch(0);
            loc.setYaw(0);
            loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
            player.teleport(loc);
        }
    }

    public boolean canBuild(Player player)
    {
        return ownerUUID.equalsIgnoreCase(player.getUniqueId().toString()) || trustedUUIDs.contains(player.getUniqueId().toString());
    }

    public List<String> getTrustedUUIDs() { return trustedUUIDs; }

    public boolean isTrusted(String uuid) { return trustedUUIDs.contains(uuid); }

    public void addTrusted(String trustedPlayerUUID) { trustedUUIDs.add(trustedPlayerUUID); }

    public void removeTrusted(String trustedPlayerUUID) { trustedUUIDs.remove(trustedPlayerUUID); }

    public boolean isSubmitted() {
        return submitted;
    }

    public void submit()
    {
        submitted = true;
    }

    public void desubmit()
    {
        submitted = false;
    }
}
