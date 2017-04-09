package com.parkourcraft.Parkour;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Parkour extends JavaPlugin {

    private static Plugin plugin;
    private static Logger logger;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    private void registerEvents() { // Register all of the listeners
        PluginManager pluginManager = getServer().getPluginManager();

        //pluginManager.registerEvents(new JoinLeaveHandler(), this);
    }

    private void registerCommands() {
        //getCommand("arena").setExecutor(new ArenaCMD());
    }

}
