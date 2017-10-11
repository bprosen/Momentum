package com.parkourcraft.Parkour;

import com.parkourcraft.Parkour.commands.*;
import com.parkourcraft.Parkour.data.clans.ClansManager;
import com.parkourcraft.Parkour.data.levels.LevelManager;
import com.parkourcraft.Parkour.data.locations.LocationManager;
import com.parkourcraft.Parkour.data.menus.MenuManager;
import com.parkourcraft.Parkour.data.perks.PerkManager;
import com.parkourcraft.Parkour.data.stats.StatsManager;
import com.parkourcraft.Parkour.gameplay.*;
import com.parkourcraft.Parkour.data.SettingsManager;
import com.parkourcraft.Parkour.storage.ConfigManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.utils.dependencies.GhostFactory;
import com.parkourcraft.Parkour.utils.dependencies.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Parkour extends JavaPlugin {

    private static Plugin plugin;
    private static Logger logger;

    public static ConfigManager configs;
    public static DatabaseManager database;
    public static SettingsManager settings;
    public static LocationManager locations;
    public static LevelManager levels;
    public static PerkManager perks;
    public static StatsManager stats;
    public static ClansManager clans;
    public static MenuManager menus;

    public static Economy economy;
    public static GhostFactory ghostFactory;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        registerEvents();
        registerCommands();

        configs = new ConfigManager(plugin);
        database = new DatabaseManager(plugin);

        loadData();

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
        unloadData();

        configs = null;
        database.close();
        database = null;

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
    }

    private static void loadData() {
        settings = new SettingsManager(configs.get("settings"));
        locations = new LocationManager();
        levels = new LevelManager(plugin);
        perks = new PerkManager(plugin);
        stats = new StatsManager(plugin);
        clans = new ClansManager(plugin);
        menus = new MenuManager(plugin);

        ghostFactory = new GhostFactory(plugin);
    }

    private static void unloadData() {
        menus = null;
        clans = null;
        stats = null;
        perks = null;
        levels = null;
        locations = null;
        settings = null;

        ghostFactory = null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static Logger getPluginLogger() {
        return logger;
    }

}
