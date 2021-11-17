package com.renatusnetwork.parkour.utils.dependencies;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class Vault {

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        Parkour.setEconomy(rsp.getProvider());
        return Parkour.getEconomy() != null;
    }
}
