package com.parkourcraft.parkour.utils.dependencies;

import com.boydti.fawe.Fawe;
import com.parkourcraft.parkour.Parkour;
import org.bukkit.plugin.Plugin;

public class FAWE {

    private static Fawe getFAWE() {
        Plugin plugin = Parkour.getPlugin().getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof Fawe))
            return null;

        return (Fawe) plugin;
    }
}
