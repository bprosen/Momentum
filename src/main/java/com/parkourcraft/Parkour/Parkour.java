package com.parkourcraft.Parkour;

import com.parkourcraft.Parkour.commands.Level_CMD;
import com.parkourcraft.Parkour.commands.Location_CMD;
import com.parkourcraft.Parkour.storage.local.FileLoader;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;
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

        FileLoader.startUp();

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
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("location").setExecutor(new Location_CMD());
    }

}
