package com.renatusnetwork.parkour;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.renatusnetwork.parkour.commands.*;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.blackmarket.BlackMarketManager;
import com.renatusnetwork.parkour.data.clans.ClansManager;
import com.renatusnetwork.parkour.data.checkpoints.CheckpointManager;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.infinite.InfiniteManager;
import com.renatusnetwork.parkour.data.levels.CompletionsDB;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.modifiers.ModifiersManager;
import com.renatusnetwork.parkour.data.perks.PerkManager;
import com.renatusnetwork.parkour.data.placeholders.*;
import com.renatusnetwork.parkour.data.plots.PlotsManager;
import com.renatusnetwork.parkour.data.races.RaceManager;
import com.renatusnetwork.parkour.data.ranks.RanksManager;
import com.renatusnetwork.parkour.data.saves.SavesManager;
import com.renatusnetwork.parkour.data.stats.PlayerHiderManager;
import com.renatusnetwork.parkour.gameplay.*;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.gameplay.handlers.SpectatorHandler;
import com.renatusnetwork.parkour.gameplay.listeners.*;
import com.renatusnetwork.parkour.storage.ConfigManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.TablesDB;
import com.renatusnetwork.parkour.utils.dependencies.ProtocolLib;
import com.sk89q.worldedit.WorldEdit;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
    private static RaceManager races;
    private static RanksManager ranks;
    private static PlotsManager plots;
    private static ProtocolManager protocol;
    private static EventManager events;
    private static InfiniteManager infinite;
    private static BankManager bank;
    private static BlackMarketManager blackmarket;
    private static SavesManager saves;
    private static ModifiersManager modifiers;
    private static Placeholders placeholders;
    private static PlayerHiderManager playerHider;
    private static ViaAPI viaVersion;

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        // load all classes
        load();
        registerEvents();
        registerCommands();

        // check before loading classes
        if (!ProtocolLib.setupProtocol())
        {
            getLogger().info("ProtocolLib not found or disabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // register placeholders
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            registerPlaceholders();
        else
            getLogger().info("Placeholder not found, not able to initialize placeholders");

        // initialize packet listeners
        PacketListener.loadListeners(this);

        // start schedulers and any settings
        Scoreboard.startScheduler(plugin);

        getLogger().info("RN-Parkour Enabled");
    }

    @Override
    public void onDisable()
    {
        // save and do all shutdown methods
        unload();
        getLogger().info("RN-Parkour Disabled");
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new LevelListener(), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new TestChamberListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(), this);
        getServer().getPluginManager().registerEvents(new PacketListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new ItemSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlotLimitingListener(), this);
        getServer().getPluginManager().registerEvents(new WorldChangeListener(), this);
        getServer().getPluginManager().registerEvents(new CropGrowthListener(), this);
        getServer().getPluginManager().registerEvents(new BowListener(), this);
        getServer().getPluginManager().registerEvents(new GlideListener(), this);

        WorldEdit.getInstance().getEventBus().register(new SelectionListener());

    }

    private void registerCommands()
    {
        getCommand("db").setExecutor(new db());

        getCommand("practicego").setExecutor(new PracticeGoCMD());
        getCommand("level").setExecutor(new LevelCMD());
        getCommand("race").setExecutor(new RaceCMD());
        getCommand("records").setExecutor(new RecordsCMD());
        getCommand("location").setExecutor(new LocationCMD());
        getCommand("stats").setExecutor(new StatsCMD());
        getCommand("menu").setExecutor(new MenuCMD());
        getCommand("perks").setExecutor(new PerksCMD());
        getCommand("spectate").setExecutor(new SpectateCMD());
        getCommand("clan").setExecutor(new ClanCMD());
        getCommand("rn-parkour").setExecutor(new RNParkourCMD());
        getCommand("players").setExecutor(new PlayerToggleCMD());
        getCommand("checkpoint").setExecutor(new CheckpointCMD());
        getCommand("spawn").setExecutor(new SpawnCMD());
        getCommand("practice").setExecutor(new PracticeCMD());
        getCommand("ranks").setExecutor(new RankCMD());
        getCommand("rankup").setExecutor(new RankupCMD());
        getCommand("plot").setExecutor(new PlotCMD());
        getCommand("event").setExecutor(new EventCMD());
        getCommand("prestige").setExecutor(new PrestigeCMD());
        getCommand("rate").setExecutor(new RateCMD());
        getCommand("sword").setExecutor(new SwordShieldCMD());
        getCommand("infinite").setExecutor(new InfiniteCMD());
        getCommand("profile").setExecutor(new ProfileCMD());
        getCommand("cosmetics").setExecutor(new CosmeticsCMD());
        getCommand("ragequit").setExecutor(new RageQuitCMD());
        getCommand("setspawn").setExecutor(new SetSpawnCMD());
        getCommand("nightvision").setExecutor(new NightVisionCMD());
        getCommand("grinding").setExecutor(new GrindCMD());
        getCommand("coins").setExecutor(new CoinsCMD());
        getCommand("tutorial").setExecutor(new TutorialCMD());
        getCommand("fails").setExecutor(new FailsCMD());
        getCommand("join").setExecutor(new JoinCMD());
        getCommand("play").setExecutor(new PlayCMD());
        getCommand("save").setExecutor(new SaveCMD());
        getCommand("jackpot").setExecutor(new JackpotCMD());
        getCommand("pay").setExecutor(new PayCMD());
        getCommand("modifier").setExecutor(new ModifierCMD());
        getCommand("bank").setExecutor(new BankCMD());
        getCommand("blackmarket").setExecutor(new BlackMarketCMD());
        getCommand("bid").setExecutor(new BidCMD());
        getCommand("favorite").setExecutor(new FavoriteCMD());
        getCommand("stuck").setExecutor(new StuckCMD());
        getCommand("preview").setExecutor(new PreviewCMD());
    }

    private static void load()
    {
        configs = new ConfigManager(plugin);
        settings = new SettingsManager(configs.get("settings"));
        database = new DatabaseManager(plugin);
        TablesDB.initTables(); // needs to happen AFTER loading the manager
        locations = new LocationManager();
        checkpoint = new CheckpointManager();
        stats = new StatsManager(plugin);
        levels = new LevelManager(plugin);
        menus = new MenuManager();
        menus.loadItems();
        levels.loadLevelsInMenus(); // load after loading menus
        levels.pickFeatured();
        menus.loadConnectedMenus();

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                CompletionsDB.loadLeaderboards(); // needs to happen AFTER loading the manager, in async to speedup start up
                CompletionsDB.loadTotalCompletions();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());

        ranks = new RanksManager();
        perks = new PerkManager();
        clans = new ClansManager(plugin);
        races = new RaceManager();
        infinite = new InfiniteManager();
        plots = new PlotsManager();
        events = new EventManager();
        protocol = ProtocolLibrary.getProtocolManager();
        saves = new SavesManager();
        modifiers = new ModifiersManager();
        bank = new BankManager();
        blackmarket = new BlackMarketManager();
        playerHider = new PlayerHiderManager();
        viaVersion = Via.getAPI();
    }

    private static void unload()
    {
        PracticeHandler.shutdown();
        SpectatorHandler.shutdown();
        infinite.shutdown();
        events.shutdown();
        levels.shutdown();
        blackmarket.shutdown();
        stats.shutdown();

        // close database and unload classes
        database.close();

        // unregister if not null
        if (placeholders != null)
            placeholders.unregister();
    }

    private static void registerPlaceholders()
    {
        placeholders = new Placeholders();
        placeholders.register();
    }

    public static Plugin getPlugin() { return plugin; }
    public static Logger getPluginLogger() {
        return logger;
    }

    // all manager methods
    public static SettingsManager getSettingsManager() { return settings; }
    public static DatabaseManager getDatabaseManager() {
        return database;
    }
    public static ConfigManager getConfigManager() {
        return configs;
    }
    public static LocationManager getLocationManager() { return locations; }
    public static LevelManager getLevelManager() {
        return levels;
    }
    public static PerkManager getPerkManager() {
        return perks;
    }
    public static MenuManager getMenuManager() {
        return menus;
    }
    public static StatsManager getStatsManager() { return stats; }
    public static ClansManager getClansManager() {
        return clans;
    }
    public static CheckpointManager getCheckpointManager() { return checkpoint; }
    public static RaceManager getRaceManager() { return races; }
    public static RanksManager getRanksManager() { return ranks; }
    public static PlotsManager getPlotsManager() { return plots; }
    public static EventManager getEventManager() { return events; }
    public static InfiniteManager getInfiniteManager() { return infinite; }
    public static ProtocolManager getProtocolManager() { return protocol; }
    public static SavesManager getSavesManager() { return saves; }
    public static BankManager getBankManager() { return bank; }
    public static BlackMarketManager getBlackMarketManager() { return blackmarket; }
    public static ModifiersManager getModifiersManager() { return modifiers; }
    public static PlayerHiderManager getPlayerHiderManager() { return playerHider; }
    public static ViaAPI getViaVersion() { return  viaVersion; }
}