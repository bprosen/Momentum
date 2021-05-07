package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.List;
import java.util.Random;

public class Event {

    private EventType eventType;
    private BukkitTask scheduler;
    private Level eventLevel;

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
    private int startingY;

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
                taskDelay = 1;

                List<Level> halfHeartLevels = Parkour.getLevelManager().getHalfHeartEventLevels();
                eventLevel = halfHeartLevels.get(ran.nextInt(halfHeartLevels.size()));
                break;
            case RISING_WATER:
                risingWater = true;
                taskDelay = 100;

                List<Level> risingWaterLevels = Parkour.getLevelManager().getRisingWaterEventLevels();
                eventLevel = risingWaterLevels.get(ran.nextInt(risingWaterLevels.size()));
                break;
        }
    }

    public void runScheduler() {
        EventManager eventManager = Parkour.getEventManager();

        // settings scheduler
        scheduler = new BukkitRunnable() {
            @Override
            public void run() {

                // set all participants to 0.5 health if setting is enabled
                if (halfAHeart && !fullHealth)
                    for (EventParticipant particpant : eventManager.getParticipants())
                        if (particpant.getPlayer().getHealth() > 0.5)
                            particpant.getPlayer().setHealth(0.5);

                // rise water by 1 y
                if (risingWater) {

                }

                if (!halfAHeart && fullHealth)
                    for (EventParticipant particpant : eventManager.getParticipants())
                        particpant.getPlayer().setHealth(20.0);

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

