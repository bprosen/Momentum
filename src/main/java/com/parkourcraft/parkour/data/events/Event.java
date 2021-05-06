package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Event {

    private List<String> participants = new ArrayList<>();
    private EventType eventType;
    private BukkitTask scheduler;
    private HashMap<String, Location> previousLocations = new HashMap<>();
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
        // settings scheduler
        scheduler = new BukkitRunnable() {
            @Override
            public void run() {

                // set all participants to 0.5 health if setting is enabled
                if (halfAHeart && !fullHealth) {
                    for (String string : participants) {
                        Player participant = Bukkit.getPlayer(string);
                        participant.setHealth(0.5);
                    }
                }

                // rise water by 1 y
                if (risingWater) {

                }

                if (!halfAHeart && fullHealth) {
                    for (String string : participants) {
                        Player participant = Bukkit.getPlayer(string);
                        participant.setHealth(20.0);
                    }
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), taskDelay, taskDelay);
    }

    public boolean isParticipant(Player player) {
        boolean participant = false;

        if (participants.contains(player.getName()))
            participant = true;

        return participant;
    }

    public void addParticipant(Player player) {
        participants.add(player.getName());
        previousLocations.put(player.getName(), player.getLocation());
    }

    public void removeParticipant(Player player) {
        participants.remove(player.getName());
        previousLocations.remove(player.getName());
    }

    public Location getOriginalLoc(Player player) {
        Location originalLoc = null;

        if (isParticipant(player))
            originalLoc = previousLocations.get(player.getName());

        return originalLoc;
    }

    public BukkitTask getScheduler() {
        return scheduler;
    }

    public Level getLevel() {
        return eventLevel;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public EventType getEventType() { return eventType; }
}

