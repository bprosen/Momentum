package com.parkourcraft.parkour;

import com.parkourcraft.parkour.commands.*;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.clans.ClansManager;
import com.parkourcraft.parkour.data.checkpoints.CheckpointManager;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.locations.LocationManager;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.data.perks.PerkManager;
import com.parkourcraft.parkour.data.spectator.SpectatorManager;
import com.parkourcraft.parkour.data.stats.StatsManager;
import com.parkourcraft.parkour.gameplay.*;
import com.parkourcraft.parkour.data.SettingsManager;
import com.parkourcraft.parkour.storage.ConfigManager;
import com.parkourcraft.parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.parkour.utils.dependencies.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Parkour extends JavaPlugin {

    private static Plugin plugin;
    private static Logger logger;

    private static ConfigManager configs;
    private static DatabaseManager database;
    private static SettingsManager settings;
    private static LocationManager locations;
    private static LevelManager levels;
    private static PerkManager perks;
    private static StatsManager stats;
    private static ClansManager clans;
    private static MenuManager menus;
    private static CheckpointManager checkpoint;
    private static Economy economy;
    private static SpectatorManager spectator;
    // Pending recode for SpectatorManager
    //private static GhostFactory ghostFactory;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        registerEvents();
        registerCommands();
        loadClasses();

        if (!Vault.setupEconomy()) { // vault setup
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Scoreboard.startScheduler(plugin);
        SpectatorHandler.startScheduler(plugin);

        stats.addUnloadedPlayers();
    }

    @Override
    public void onDisable() {
        Checkpoint_DB.saveAllPlayers();
        database.close();
        unloadClasses();

        plugin = null;
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new LevelListener(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new TestChamberHandler(), this);
    }

    private void registerCommands() {
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("location").setExecutor(new Location_CMD());
        getCommand("stats").setExecutor(new Stats_CMD());
        getCommand("menu").setExecutor(new Menu_CMD());
        getCommand("perks").setExecutor(new Perks_CMD());
        getCommand("setarmor").setExecutor(new SetArmor_CMD());
        getCommand("spectate").setExecutor(new Spectate_CMD());
        getCommand("clan").setExecutor(new Clan_CMD());
        getCommand("pc-parkour").setExecutor(new PC_Parkour_CMD());
        getCommand("toggleplayers").setExecutor(new PlayerToggle_CMD());
    }

    private static void loadClasses() {
        configs = new ConfigManager(plugin);
        settings = new SettingsManager(configs.get("settings"));
        locations = new LocationManager();
        checkpoint = new CheckpointManager();
        database = new DatabaseManager(plugin);
        levels = new LevelManager(plugin);
        perks = new PerkManager(plugin);
        stats = new StatsManager(plugin);
        clans = new ClansManager(plugin);
        menus = new MenuManager(plugin);
        spectator = new SpectatorManager(plugin);
    }

    private static void unloadClasses() {
        menus = null;
        checkpoint = null;
        clans = null;
        stats = null;
        perks = null;
        levels = null;
        locations = null;
        settings = null;
        spectator = null;
        configs = null;
        database = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

    // All manager methods
    public static SettingsManager getSettingsManager() {
        return settings;
    }
    public static DatabaseManager getDatabaseManager() {
        return database;
    }
    public static ConfigManager getConfigManager() {
        return configs;
    }
    public static LocationManager getLocationManager() {
        return locations;
    }
    public static LevelManager getLevelManager() {
        return levels;
    }
    public static PerkManager getPerkManager() {
        return perks;
    }
    public static MenuManager getMenuManager() {
        return menus;
    }
    public static StatsManager getStatsManager() {
        return stats;
    }
    public static ClansManager getClansManager() {
        return clans;
    }
    public static Economy getEconomy() {
        return economy;
    }
    public static SpectatorManager getSpectatorManager() {
        return spectator;
    }
    public static CheckpointManager getCheckpointManager() { return checkpoint; }
    public static void setEconomy(Economy eco) { economy = eco; }
}
