package com.parkourcraft.Parkour.utils.dependencies;

import com.parkourcraft.Parkour.Parkour;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardUtils {

    private static WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Parkour.getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
            return null;

        return (WorldGuardPlugin) plugin;
    }

    public static List<String> getRegions(Location location) {
        WorldGuardPlugin guard = getWorldGuard();
        Vector v = location.toVector();
        RegionManager manager = guard.getRegionManager(location.getWorld());

        ApplicableRegionSet regions = manager.getApplicableRegions(location);
        List<String> regionNames = new ArrayList<String>();

        for (ProtectedRegion region : regions)
            regionNames.add(region.getId());

        return regionNames;
    }

}
