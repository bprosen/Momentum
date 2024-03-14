package com.renatusnetwork.momentum.data.events.types;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.locations.LocationManager;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashSet;
import java.util.Set;

public class RisingWaterEvent extends Event implements SchedulerInterface
{

    private BukkitTask scheduler;
    private int delay;

    private int currentY;

    public RisingWaterEvent(Level level)
    {
        super(level, "Rising Water");

        this.delay = Momentum.getSettingsManager().rising_water_event_task_delay;
        this.currentY = level.getStartLocation().getBlockY() - Momentum.getSettingsManager().rising_water_y_below_start_y;

        runScheduler();
    }

    @Override
    public void end()
    {
        clearWater();
        cancel();
    }

    private void clearWater()
    {

        BlockVector maxPoint = getRegion().getMaximumPoint().toBlockPoint();
        BlockVector minPoint = getRegion().getMinimumPoint().toBlockPoint();
        int minX = minPoint.getBlockX();
        int maxX = maxPoint.getBlockX();
        int minZ = minPoint.getBlockZ();
        int maxZ = maxPoint.getBlockZ();

        WorldEdit api = WorldEdit.getInstance();

        if (api != null)
        {
            LocalWorld world = new BukkitWorld(getLevel().getStartLocation().getWorld());
            Vector pos1 = new Vector(minX, 0, minZ);
            Vector pos2 = new Vector(maxX, 255, maxZ);
            CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

            try
            {
                // enable fast mode to do it w/o lag, then quickly disable fast mode once queue flushed
                EditSession editSession = api.getEditSessionFactory().getEditSession(world, -1);
                editSession.setFastMode(true);

                // create single base block set for replace
                Set<BaseBlock> baseBlockSet = new HashSet<BaseBlock>() {{ add(new BaseBlock(Material.WATER.getId())); }};

                editSession.replaceBlocks(selection, baseBlockSet, new BaseBlock(Material.AIR.getId()));
                editSession.flushQueue();
                editSession.setFastMode(false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
            Momentum.getPluginLogger().info("WorldEdit API found null in Event clearWater()");
    }

    public boolean isStartCoveredInWater()
    {
        Location startLoc = getLevel().getStartLocation();
        return startLoc.clone().getBlock().getType() == Material.WATER || startLoc.clone().add(0, 1, 0).getBlock().getType() == Material.WATER;
    }

    @Override
    public void runScheduler()
    {
        EventManager eventManager = Momentum.getEventManager();
        scheduler = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!eventManager.getParticipants().isEmpty())
                {
                    LocationManager locationManager = Momentum.getLocationManager();

                    // if there is no start loc, then cancel
                    if (locationManager.equals(getLevel().getStartLocation(), locationManager.getLobbyLocation()))
                        cancel();
                    else
                    {
                        // add another layer of water
                        BlockVector maxPoint = getRegion().getMaximumPoint().toBlockPoint();
                        BlockVector minPoint = getRegion().getMinimumPoint().toBlockPoint();
                        int minX = minPoint.getBlockX();
                        int maxX = maxPoint.getBlockX();
                        int minZ = minPoint.getBlockZ();
                        int maxZ = maxPoint.getBlockZ();

                        WorldEdit api = WorldEdit.getInstance();

                        if (api != null)
                        {

                            LocalWorld world = new BukkitWorld(getLevel().getStartLocation().getWorld());

                            Vector pos1 = new Vector(minX, currentY, minZ);
                            Vector pos2 = new Vector(maxX, currentY, maxZ);
                            CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

                            try
                            {
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
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            currentY++;
                        }
                        else
                            Momentum.getPluginLogger().info("WorldEdit API found null in RisingWaterEvent runScheduler");
                    }
                }
                else if (isStartCoveredInWater())
                    eventManager.endEvent(null, false, true);
            }
        }.runTaskTimer(Momentum.getPlugin(), delay, delay);
    }

    @Override
    public BukkitTask getScheduler()
    {
        return scheduler;
    }

    @Override
    public void cancel()
    {
        if (!scheduler.isCancelled())
            scheduler.cancel();
    }
}
