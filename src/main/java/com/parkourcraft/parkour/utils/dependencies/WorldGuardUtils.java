package com.parkourcraft.parkour.utils.dependencies;

import com.parkourcraft.parkour.Parkour;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldGuardUtils implements Listener {

    private static HashMap<String, String> inLevelRegions = new HashMap<>();

    private static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Parkour.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
            return null;

        return (WorldGuardPlugin) plugin;
    }

    public static List<String> getRegions(Location location) {
        WorldGuardPlugin guard = getWorldGuard();
        RegionManager manager = guard.getRegionManager(location.getWorld());

        ApplicableRegionSet regions = manager.getApplicableRegions(location);
        List<String> regionName = new ArrayList<>();

        for (ProtectedRegion region : regions)
            regionName.add(region.getId());

        return regionName;
    }

    public static HashMap<String, String> getPlayerRegionMap() {
        return inLevelRegions;
    }
}
