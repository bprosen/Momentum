package com.renatusnetwork.parkour.data.plots;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Plot {

    private String ownerName;
    private String ownerUUID;
    private Location spawnLoc;
    private List<String> trustedPlayers;
    private boolean submitted = false;

    // add via player object
    public Plot(Player owner, Location spawnLoc) {
        this.ownerName = owner.getName();
        this.ownerUUID = owner.getUniqueId().toString();
        this.spawnLoc = spawnLoc;

        // run async
        new BukkitRunnable() {
            public void run() {
                trustedPlayers = PlotsDB.getTrustedPlayers(owner.getUniqueId().toString());
                submitted = PlotsDB.isSubmitted(owner.getUniqueId().toString());
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    // no player object addition
    public Plot(String ownerName, String ownerUUID, Location spawnLoc) {
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
        this.spawnLoc = spawnLoc;

        // run async
        new BukkitRunnable() {
            public void run() {
                trustedPlayers = PlotsDB.getTrustedPlayers(ownerUUID);
                submitted = PlotsDB.isSubmitted(ownerUUID);
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public String getOwnerName() {
        return ownerName;
    }

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
            loc.subtract(0, 0,(Parkour.getSettingsManager().player_submitted_plot_width / 2) + 1);
            loc.setPitch(0);
            loc.setYaw(0);
            loc.clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
            player.teleport(loc);
        }
    }

    public boolean canBuild(String playerName) {
        return ownerName.equalsIgnoreCase(playerName) || trustedPlayers.contains(playerName);
    }

    public List<String> getTrustedPlayers() { return trustedPlayers; }

    public void addTrustedPlayer(Player player) { trustedPlayers.add(player.getName()); }

    public void removeTrustedPlayer(Player player) { trustedPlayers.remove(player.getName()); }

    public boolean isSubmitted() {
        return submitted;
    }

    public void submit() {
        submitted = true;
    }

    public void desubmit() { submitted = false; }
}
