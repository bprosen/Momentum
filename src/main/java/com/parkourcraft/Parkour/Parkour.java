package com.parkourcraft.Parkour;

import com.parkourcraft.Parkour.commands.Level_CMD;
import com.parkourcraft.Parkour.commands.Location_CMD;
import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.listeners.LevelListener;
import com.parkourcraft.Parkour.storage.local.FileLoader;
import com.parkourcraft.Parkour.utils.dependencies.Vault;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Parkour extends JavaPlugin {

    private static Plugin plugin;
    private static Logger logger;
    public static Economy economy;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        FileLoader.startUp();

        registerEvents();
        registerCommands();

        LevelManager.loadLevels();

        if (!Vault.setupEconomy()) { // vault setup
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        plugin = null;

        if (!Vault.setupEconomy() ) { // disable vault
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    private void registerEvents() { // Register all of the listeners
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new LevelListener(), this);
    }

    private void registerCommands() {
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("location").setExecutor(new Location_CMD());
    }

}
