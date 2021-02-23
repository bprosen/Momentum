package com.parkourcraft.parkour.data.spectator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SpectatorManager {

    private List<Spectator> spectatorList = new ArrayList<>();

    public SpectatorManager(Plugin plugin) {
        startScheduler(plugin);
    }

    public void startScheduler(Plugin plugin) {
        // run clean every 5 seconds for garbage collection
        new BukkitRunnable() {
            public void run() {
                clean();
            }
        }.runTaskTimer(plugin, 20 * 5, 20 * 5);
    }

    public boolean exists(String UUID) {
        if (get(UUID) != null)
            return true;
        return false;
    }

    public void add(Player player) {
        if (!exists(player.getUniqueId().toString())) {
            Spectator spectator = new Spectator(player);
            spectatorList.add(spectator);
        }
    }

    public void remove(Spectator spectator) {
       if (exists(spectator.getUUID()))
           spectatorList.remove(spectator);
    }

    public Spectator get(String UUID) {
        for (Spectator spectators : spectatorList)
            if (spectators.getUUID().equals(UUID))
                return spectators;

        return null;
    }

    public Spectator getByName(String playerName) {
        for (Spectator spectators : spectatorList)
            if (spectators.getName().equalsIgnoreCase(playerName))
                return spectators;

        return null;
    }

    public void clean() {

        if (spectatorList.isEmpty())
            return;

        List<Spectator> removeList = new ArrayList<>();

        for (Spectator spectators : spectatorList)
            if (!spectators.getPlayer().isOnline())
                removeList.add(spectators);

        for (Spectator spectator : removeList)
            remove(spectator);
    }

    public List<Spectator> getSpectatorList() {
        return spectatorList;
    }
}
