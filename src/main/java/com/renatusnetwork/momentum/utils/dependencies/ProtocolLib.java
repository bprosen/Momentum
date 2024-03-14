package com.renatusnetwork.momentum.utils.dependencies;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;

public class ProtocolLib {

    public static boolean setupProtocol() {
        if (Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") == null)
            return false;

        if (!ProtocolLibrary.getPlugin().isEnabled() || ProtocolLibrary.getPlugin() == null)
            return false;

        return true;
    }
}
