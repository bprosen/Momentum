package com.parkourcraft.Parkour;

import com.parkourcraft.Parkour.commands.Level_CMD;
import com.parkourcraft.Parkour.commands.Location_CMD;
import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.listeners.JoinLeaveHandler;
import com.parkourcraft.Parkour.listeners.LevelListener;
import com.parkourcraft.Parkour.stats.StatsManager;
import com.parkourcraft.Parkour.storage.local.FileLoader;
import com.parkourcraft.Parkour.storage.mysql.DatabaseConnection;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.TableManager;
import com.parkourcraft.Parkour.utils.dependencies.Vault;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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

        DatabaseConnection.open();
        TableManager.setUp();

        if (!Vault.setupEconomy()) { // vault setup
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        runScheduler();
    }

    @Override
    public void onDisable() {
        StatsManager.updateOnlinePlayersInDatabase();

        DatabaseConnection.close();

        if (!Vault.setupEconomy() ) { // disable vault
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        plugin = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    private void runScheduler() {
        BukkitScheduler scheduler = getServer().getScheduler();

        // update online player's data into database (every 10 seconds)
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                //StatsManager.updateOnlinePlayersInDatabase();
            }
        }, 0L, 10L * 20L);

        // runs the queries in the cache (every .2 seconds (5 times per second))
        scheduler.runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                //DatabaseManager.runCaches();
            }
        }, 0L, 4L);

        // keeps connection to the database alive (Every 1.5 minutes)
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                DatabaseManager.addUpdateQuery("SELECT * FROM players WHERE uuid='s'");
            }
        }, 1800L, 90L * 20L);
    }

    private void registerEvents() { // Register all of the listeners
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new LevelListener(), this);
        pluginManager.registerEvents(new JoinLeaveHandler(), this);
    }

    private void registerCommands() {
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("location").setExecutor(new Location_CMD());
    }

}
