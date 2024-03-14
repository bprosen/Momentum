package com.renatusnetwork.momentum.utils.dependencies;

import com.renatusnetwork.momentum.Momentum;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;

public class WorldGuard implements Listener {

    private static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Momentum.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
            return null;

        return (WorldGuardPlugin) plugin;
    }

    public static ProtectedRegion getRegionFromName(World world, String name) {
        return getWorldGuard().getRegionManager(world).getRegion(name);
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

    public static ProtectedRegion getRegion(Location location) {
        WorldGuardPlugin guard = getWorldGuard();
        RegionManager manager = guard.getRegionManager(location.getWorld());

        ApplicableRegionSet regions = manager.getApplicableRegions(location);

        for (ProtectedRegion region : regions)
            return region;

        return null;
    }
}
