package com.parkourcraft.parkour;

import com.parkourcraft.parkour.commands.*;
import com.parkourcraft.parkour.data.checkpoints.Checkpoint_DB;
import com.parkourcraft.parkour.data.clans.ClansManager;
import com.parkourcraft.parkour.data.checkpoints.CheckpointManager;
import com.parkourcraft.parkour.data.levels.LevelManager;
import com.parkourcraft.parkour.data.locations.LocationManager;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.data.perks.PerkManager;
import com.parkourcraft.parkour.data.races.RaceManager;
import com.parkourcraft.parkour.data.rank.RanksManager;
import com.parkourcraft.parkour.gameplay.SpectatorHandler;
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
    private static RaceManager races;
    private static RanksManager ranks;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        registerEvents();
        registerCommands();
        loadClasses();

        if (!Vault.setupEconomy()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Scoreboard.startScheduler(plugin);
        SpectatorHandler.startScheduler(plugin);
        settings.loadSpawn();
        stats.addUnloadedPlayers();

        getLogger().info("PC-Parkour Enabled");
    }

    @Override
    public void onDisable() {
        Checkpoint_DB.saveAllPlayers();
        PracticeHandler.shutdown();
        SpectatorHandler.shutdown();
        database.close();
        unloadClasses();

        getLogger().info("PC-Parkour Disabled");

        plugin = null;
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new LevelListener(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new TestChamberHandler(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(), this);
    }

    private void registerCommands() {
        getCommand("level").setExecutor(new Level_CMD());
        getCommand("race").setExecutor(new Race_CMD());
        getCommand("location").setExecutor(new Location_CMD());
        getCommand("stats").setExecutor(new Stats_CMD());
        getCommand("menu").setExecutor(new Menu_CMD());
        getCommand("perks").setExecutor(new Perks_CMD());
        getCommand("setarmor").setExecutor(new SetArmor_CMD());
        getCommand("spectate").setExecutor(new Spectate_CMD());
        getCommand("clan").setExecutor(new Clan_CMD());
        getCommand("pc-parkour").setExecutor(new PC_Parkour_CMD());
        getCommand("toggleplayers").setExecutor(new PlayerToggle_CMD());
        getCommand("checkpoint").setExecutor(new Checkpoint_CMD());
        getCommand("spawn").setExecutor(new Spawn_CMD());
        getCommand("setspawn").setExecutor(new SetSpawn_CMD());
        getCommand("practice").setExecutor(new Practice_CMD());
        getCommand("ranks").setExecutor(new Ranks_CMD());
        getCommand("rankup").setExecutor(new Rankup_CMD());
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
        menus = new MenuManager();
        races = new RaceManager();
        ranks = new RanksManager();
    }

    private static void unloadClasses() {
        menus = null;
        checkpoint = null;
        clans = null;
        stats = null;
        perks = null;
        levels = null;
        ranks = null;
        races = null;
        locations = null;
        settings = null;
        configs = null;
        database = null;
    }

    public static Plugin getPlugin() { return plugin; }
    public static Logger getPluginLogger() {
        return logger;
    }
    public static void setEconomy(Economy eco) { economy = eco; }

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
    public static CheckpointManager getCheckpointManager() { return checkpoint; }
    public static RaceManager getRaceManager() { return races; }
    public static RanksManager getRanksManager() { return ranks; }
}