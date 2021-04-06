package com.parkourcraft.parkour.data.plots;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
                trustedPlayers = Plots_DB.getTrustedPlayers(owner.getUniqueId().toString());
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
                trustedPlayers = Plots_DB.getTrustedPlayers(ownerUUID);
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

    public void teleportOwner() {
        Player player = Bukkit.getPlayer(ownerName);
        // teleport player if not null
        if (player != null) {
            Location loc = spawnLoc.clone();
            loc.setYaw(player.getLocation().getYaw());
            loc.setPitch(player.getLocation().getPitch());
            loc.add(0.5, 0, 0.5);
            player.teleport(loc);
        }
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
