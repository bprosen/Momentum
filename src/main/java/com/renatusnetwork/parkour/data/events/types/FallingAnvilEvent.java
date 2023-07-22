package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public class FallingAnvilEvent extends Event implements SchedulerInterface
{
    private BukkitTask scheduler;
    private int delay;

    public FallingAnvilEvent(Level level)
    {
        super(level, "Falling Anvil");
        this.delay = Parkour.getSettingsManager().falling_anvil_event_task_delay;

        runScheduler();
    }

    @Override
    public void end()
    {
        scheduler.cancel();
    }

    @Override
    public void runScheduler()
    {
        EventManager eventManager = Parkour.getEventManager();
        scheduler = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                if (!eventManager.getParticipants().isEmpty())
                {
                    // variables for the region's bounds
                    BlockVector maxPoint = getRegion().getMaximumPoint().toBlockPoint();
                    BlockVector minPoint = getRegion().getMinimumPoint().toBlockPoint();
                    int minX = minPoint.getBlockX();
                    int maxX = maxPoint.getBlockX();
                    int minZ = minPoint.getBlockZ();
                    int maxZ = maxPoint.getBlockZ();

                    for (int i = minX; i <= maxX; i++)
                    {
                        for (int j = minZ; j <= maxZ; j++)
                        {
                            // get percent from 0 to 101
                            double percent = ThreadLocalRandom.current().nextDouble(0, 101);

                            // if the percent is less than the value, then spawn the anvil at i (x) and j (y)
                            if (percent <= Parkour.getSettingsManager().anvil_spawn_percentage) {

                                World world = getLevel().getStartLocation().getWorld();
                                Location spawnLocation = new Location(
                                        world,
                                        i,
                                        getLevel().getStartLocation().getBlockY() +
                                                Parkour.getSettingsManager().anvil_spawn_y_above_start_y,
                                        j);

                                ItemStack itemStack = new ItemStack(Material.ANVIL);
                                FallingBlock fallingBlock = world.spawnFallingBlock(spawnLocation, itemStack.getData());
                                fallingBlock.setDropItem(false);
                                fallingBlock.setHurtEntities(true);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), delay, delay);
    }

    @Override
    public BukkitTask getScheduler() {
        return scheduler;
    }

    @Override
    public void cancel()
    {
        if (!scheduler.isCancelled())
            scheduler.cancel();
    }
}
