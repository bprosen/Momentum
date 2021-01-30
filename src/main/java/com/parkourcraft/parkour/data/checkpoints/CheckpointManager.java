package com.parkourcraft.parkour.data.checkpoints;

import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;

public class CheckpointManager {

    private HashMap<String, Location> checkpointMap = new HashMap<>();

    public CheckpointManager() { }

    public void setCheckpoint(Player player, Location loc) {
        addPlayer(player.getName(), loc);
        player.sendMessage(Utils.translate("&eYour checkpoint has been set"));
    }

    public void addPlayer(String playerName, Location loc) {
        checkpointMap.put(playerName, loc);
    }

    public void removePlayer(Player player) {
        checkpointMap.remove(player.getName());
    }

    public Location get(Player player) {
        return checkpointMap.get(player.getName());
    }

    public boolean contains(Player player) {
        if (checkpointMap.containsKey(player.getName()))
            return true;
        return false;
    }

    public void teleportPlayer(Player player) {
        Location loc = get(player).clone();

        if (loc == null)
            return;

        loc.add(0.5, 0, 0.5);
        loc.setPitch(player.getLocation().getPitch());
        loc.setYaw(player.getLocation().getYaw());
        player.teleport(loc);
    }
}
