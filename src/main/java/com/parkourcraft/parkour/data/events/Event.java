package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import com.parkourcraft.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FallingSand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Event {

    private EventType eventType;
    private BukkitTask scheduler;
    private Level eventLevel;
    private ProtectedRegion levelRegion;

    /*
        Game Settings
     */
    private boolean fullHealth = false;
    private boolean halfAHeart = false;
    private boolean risingWater = false;
    private int taskDelay;

    /*
        Settings for Rising Water
     */
    private int currentY;

    public Event(EventType eventType) {
        this.eventType = eventType;

        loadGameSettings();
        runScheduler();
    }

    public void loadGameSettings() {

        Random ran = new Random();

        switch (eventType) {
            case PVP:
                fullHealth = true;
                taskDelay = 1;

                List<Level> pvpLevels = Parkour.getLevelManager().getPvPEventLevels();
                eventLevel = pvpLevels.get(ran.nextInt(pvpLevels.size()));
                break;
            case HALF_HEART:
                halfAHeart = true;
                taskDelay = 5;

                List<Level> halfHeartLevels = Parkour.getLevelManager().getHalfHeartEventLevels();
                eventLevel = halfHeartLevels.get(ran.nextInt(halfHeartLevels.size()));
                break;
            case RISING_WATER:
                risingWater = true;
                taskDelay = 100;

                List<Level> risingWaterLevels = Parkour.getLevelManager().getRisingWaterEventLevels();
                eventLevel = risingWaterLevels.get(ran.nextInt(risingWaterLevels.size()));
                currentY = eventLevel.getStartLocation().getBlockY() - 10;
                break;
        }
        // get level region
        levelRegion = WorldGuard.getRegion(eventLevel.getStartLocation());
    }

    public void runScheduler() {
        EventManager eventManager = Parkour.getEventManager();

        // settings scheduler
        scheduler = new BukkitRunnable() {
            @Override
            public void run() {

                // set all participants to 0.5 health if setting is enabled
                if (halfAHeart) {
                    for (EventParticipant participant : eventManager.getParticipants()) {

                        if (participant.getPlayer().getHealth() > 0.5)
                            participant.getPlayer().setHealth(0.5);

                    }

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
                            if (percent <= Parkour.getSettingsManager().anvil_spawn_percentage) {

                                World world = eventLevel.getStartLocation().getWorld();
                                Location spawnLocation = new Location(
                                        world,
                                        i,
                                        eventLevel.getStartLocation().getBlockY() + 40,
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

                }

                if (fullHealth) {
                    for (EventParticipant particpant : eventManager.getParticipants())
                        particpant.getPlayer().setHealth(20.0);
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), taskDelay, taskDelay);
    }

    public BukkitTask getScheduler() {
        return scheduler;
    }

    public Level getLevel() {
        return eventLevel;
    }

    public EventType getEventType() { return eventType; }
}

