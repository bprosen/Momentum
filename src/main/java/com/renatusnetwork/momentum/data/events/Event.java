package com.renatusnetwork.momentum.data.events;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.levels.LevelsYAML;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Event {

    private EventType eventType;
    private BukkitTask scheduler;
    private Level eventLevel;
    private ProtectedRegion levelRegion;

    /*
        Game Settings
     */
    private boolean fallingAnvil = false;
    private boolean risingWater = false;
    private int taskDelay;

    /*
        Settings for Rising Water
     */
    private int currentWaterY;

    public Event(EventType eventType) {
        this.eventType = eventType;

        loadGameSettings();
        runScheduler();
    }

    public void loadGameSettings() {

        Random ran = new Random();

        switch (eventType) {
            case PVP:
                taskDelay = Momentum.getSettingsManager().pvp_event_task_delay;

                List<Level> pvpLevels = Momentum.getLevelManager().getPvPEventLevels();
                eventLevel = pvpLevels.get(ran.nextInt(pvpLevels.size()));
                break;
            case FALLING_ANVIL:
                fallingAnvil = true;
                taskDelay = Momentum.getSettingsManager().half_heart_event_task_delay;

                List<Level> halfHeartLevels = Momentum.getLevelManager().getFallingAnvilEventLevels();
                eventLevel = halfHeartLevels.get(ran.nextInt(halfHeartLevels.size()));
                break;
            case RISING_WATER:
                risingWater = true;
                taskDelay = Momentum.getSettingsManager().rising_water_event_task_delay;

                List<Level> risingWaterLevels = Momentum.getLevelManager().getRisingWaterEventLevels();
                eventLevel = risingWaterLevels.get(ran.nextInt(risingWaterLevels.size()));
                currentWaterY = eventLevel.getStartLocation().getBlockY() - LevelsYAML.getRisingWaterStartingMinusY(eventLevel.getName());
                break;
        }
        // get level region
        levelRegion = WorldGuard.getRegion(eventLevel.getStartLocation());
    }

    public void runScheduler() {
        EventManager eventManager = Momentum.getEventManager();

        // settings scheduler
        scheduler = new BukkitRunnable() {
            @Override
            public void run() {

                // dont do scheduler if nobody is playing
                if (!eventManager.getParticipants().isEmpty()) {
                    // set all participants to 0.5 health if setting is enabled
                    if (fallingAnvil) {

                        // variables for the region's bounds
                        BlockVector maxPoint = levelRegion.getMaximumPoint().toBlockPoint();
                        BlockVector minPoint = levelRegion.getMinimumPoint().toBlockPoint();
                        int minX = minPoint.getBlockX();
                        int maxX = maxPoint.getBlockX();
                        int minZ = minPoint.getBlockZ();
                        int maxZ = maxPoint.getBlockZ();

                        for (int i = minX; i <= maxX; i++) {
                            for (int j = minZ; j <= maxZ; j++) {
                                // get percent from 0 to 101
                                double percent = ThreadLocalRandom.current().nextDouble(0, 101);

                                // if the percent is less than the value, then spawn the anvil at i (x) and j (y)
                                if (percent <= Momentum.getSettingsManager().anvil_spawn_percentage) {

                                    World world = eventLevel.getStartLocation().getWorld();
                                    Location spawnLocation = new Location(
                                            world,
                                            i,
                                            eventLevel.getStartLocation().getBlockY() +
                                            Momentum.getSettingsManager().anvil_spawn_y_above_start_y,
                                            j);

                                    ItemStack itemStack = new ItemStack(Material.ANVIL);
                                    FallingBlock fallingBlock = world.spawnFallingBlock(spawnLocation, itemStack.getData());
                                    fallingBlock.setDropItem(false);
                                    fallingBlock.setHurtEntities(true);
                                }
                            }
                        }
                    }

                    // rise water by 1 y
                    if (risingWater) {

                        // if there is no start loc, then cancel
                        if (eventLevel.getStartLocation().getX() == Momentum.getLocationManager().get("spawn").getX() &&
                            eventLevel.getStartLocation().getZ() == Momentum.getLocationManager().get("spawn").getZ())
                            cancel();
                        else {

                            // add another layer of water
                            BlockVector maxPoint = levelRegion.getMaximumPoint().toBlockPoint();
                            BlockVector minPoint = levelRegion.getMinimumPoint().toBlockPoint();
                            int minX = minPoint.getBlockX();
                            int maxX = maxPoint.getBlockX();
                            int minZ = minPoint.getBlockZ();
                            int maxZ = maxPoint.getBlockZ();

                            WorldEdit api = WorldEdit.getInstance();

                            if (api != null) {

                                LocalWorld world = new BukkitWorld(eventLevel.getStartLocation().getWorld());

                                Vector pos1 = new Vector(minX, currentWaterY, minZ);
                                Vector pos2 = new Vector(maxX, currentWaterY, maxZ);
                                CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

                                try {
                                    // enable fast mode to do it w/o lag, then quickly disable fast mode once queue flushed
                                    EditSession editSession = api.getEditSessionFactory().getEditSession(world, -1);
                                    editSession.setFastMode(true);

                                    // create single base block set for replace
                                    Set<BaseBlock> baseBlockSet = new HashSet<BaseBlock>() {{
                                        add(new BaseBlock(Material.AIR.getId()));
                                    }};

                                    editSession.replaceBlocks(selection, baseBlockSet, new BaseBlock(Material.WATER.getId()));
                                    editSession.flushQueue();
                                    editSession.setFastMode(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                currentWaterY++;
                            } else {
                                Momentum.getPluginLogger().info("WorldEdit API found null in Event runScheduler");
                            }
                        }
                    }
                // if nobody is playing and spawn is filled (rising water event), there is no way for anyone to beat it!
                } else if (eventManager.isStartCoveredInWater()) {
                    eventManager.endEvent(null, false, true);
                }
            }
        }.runTaskTimer(Momentum.getPlugin(), taskDelay, taskDelay);
    }

    public BukkitTask getScheduler() {
        return scheduler;
    }

    public Level getLevel() {
        return eventLevel;
    }

    public EventType getEventType() { return eventType; }

    public ProtectedRegion getLevelRegion() { return levelRegion; }
}

