package com.parkourcraft.Parkour.utils.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.parkourcraft.Parkour.Parkour;

import net.milkbowl.vault.economy.Economy;

public class Vault {

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;

        Parkour.economy = rsp.getProvider();
        return Parkour.economy != null;
    }

}
