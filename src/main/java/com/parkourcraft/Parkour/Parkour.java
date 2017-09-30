package com.parkourcraft.Parkour;

import com.parkourcraft.Parkour.commands.*;
import com.parkourcraft.Parkour.data.*;
import com.parkourcraft.Parkour.gameplay.JoinLeaveHandler;
import com.parkourcraft.Parkour.gameplay.LevelListener;
import com.parkourcraft.Parkour.gameplay.MenuListener;
import com.parkourcraft.Parkour.gameplay.Scoreboard;
import com.parkourcraft.Parkour.storage.local.FileLoader;
import com.parkourcraft.Parkour.storage.mysql.DataQueries;
import com.parkourcraft.Parkour.storage.mysql.DatabaseConnection;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.TableManager;
import com.parkourcraft.Parkour.utils.dependencies.Vault;
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

        DatabaseConnection.open();
        TableManager.setUp();

        LocationManager.loadLocations();
        PerkManager.loadAll();
        DataQueries.loadPerkIDCache();
        LevelManager.loadAll();
        DataQueries.loadLevelIDCache();
        MenuManager.loadMenus();

        if (!Vault.setupEconomy()) { // vault setup
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        runScheduler();
    }

    @Override
    public void onDisable() {
        DatabaseManager.runCaches();

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

        // update open menus every .5 seconds
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                MenuManager.updateOpenInventories();
            }
        }, 0L, 10L);

        // update scoreboards every .2 seconds
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                Scoreboard.displayScoreboards();
            }
        }, 20L, 4L);

        /*
         * Asynchronously grabs leaderboards and total
         * number of completions from database
         * while syncing the information into memory
         * interval: every 30 seconds
         */
        scheduler.runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                DataQueries.loadTotalCompletions();
                DataQueries.loadLeaderboards();
            }
        }, 10L, 30L * 20L);

        // runs the queries in the cache (every .2 seconds (5 times per second))
        scheduler.runTaskTimerAsynchronously(this, new Runnable() {
            public void run() {
                DataQueries.syncLevelIDs();
                DataQueries.syncPerkIDs();
                DatabaseManager.runCaches();
            }
        }, 0L, 4L);
    }

    private void registerEvents() { // Register all of the gameplay
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new LevelListener(), this);
        pluginManager.registerEvents(new JoinLeaveHandler(), this);
        pluginManager.registerEvents(new MenuListener(), this);
    }

    private void registerCommands() {
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("location").setExecutor(new Location_CMD());
        getCommand("stats").setExecutor(new Stats_CMD());
        getCommand("menu").setExecutor(new Menu_CMD());
        getCommand("perks").setExecutor(new Perks_CMD());
        getCommand("setarmor").setExecutor(new SetArmor_CMD());
    }

}
