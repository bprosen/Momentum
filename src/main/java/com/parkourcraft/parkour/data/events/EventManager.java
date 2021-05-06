package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.checkpoints.CheckpointManager;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.gameplay.PracticeHandler;
import com.parkourcraft.parkour.gameplay.SpectatorHandler;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {

    private Event runningEvent = null;
    private BukkitTask maxRunTimer;
    private List<EventParticipant> participants = new ArrayList<>();

    public EventManager() {
        startScheduler();
    }

    // scheduler to handle the next event and reminder for running event
    public void startScheduler() {
        // run a timer scheduler for next event
        new BukkitRunnable() {
            @Override
            public void run() {
                if (runningEvent == null) {
                    // get random type from list
                    EventType[] eventTypes = EventType.values();
                    Random ran = new Random();
                    EventType eventType = eventTypes[ran.nextInt(eventTypes.length)];

                    startEvent(eventType);
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().check_next_event_delay,
                                           20 * Parkour.getSettingsManager().check_next_event_delay);
        // run a timer scheduler for reminder to join running event
        new BukkitRunnable() {
            @Override
            public void run() {
                if (runningEvent != null) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(Utils.translate("&7A &b" + formatName(runningEvent.getEventType()) +
                            " Event &7is still running! Type &b/event join &7to join!"));
                    Bukkit.broadcastMessage("");
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().event_reminder_delay,
                                           20 * Parkour.getSettingsManager().event_reminder_delay);
    }

    // method to start event
    public void startEvent(EventType eventType) {
        runningEvent = new Event(eventType);

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(Utils.translate("&7A &b" + formatName(runningEvent.getEventType()) +
                " Event &7has begun! Type &b/event join &7to join!"));
        Bukkit.broadcastMessage("");

        // start max time timer
        startTimer();
    }

    // method to start the timer
    private void startTimer() {
        maxRunTimer = new BukkitRunnable() {
            @Override
            public void run() {

                if (runningEvent != null)
                    endEvent(null, false, true);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().max_event_run_time);
    }

    // method to end event
    public void endEvent(Player winner, boolean forceEnded, boolean ranOutOfTime) {
        // cancel schedulers first
        runningEvent.getScheduler().cancel();
        maxRunTimer.cancel();

        // then teleport all players back to where they entered
        for (EventParticipant participant : participants) {
            removeParticipant(participant.getPlayer());
        }

        if (forceEnded) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A &b" + formatName(runningEvent.getEventType())
                    + " &7Event has been force ended!"));
            Bukkit.broadcastMessage("");
        } else if (ranOutOfTime) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A &b" + formatName(runningEvent.getEventType())
                    + " &7Event has gone on too long! Nobody beat it in time :("));
            Bukkit.broadcastMessage("");
        } else {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A &b" + formatName(runningEvent.getEventType())
                    + " &7Event has ended! &b&l" + winner.getName() + " &7has won!"));
            Bukkit.broadcastMessage("");
        }

        // null the running event last
        runningEvent = null;
    }

    public Event getRunningEvent() {
        return runningEvent;
    }

    public EventType getEventType() {
        return runningEvent.getEventType();
    }

    public boolean isEventRunning() {
        if (runningEvent != null)
            return true;
        return false;
    }

    /*
        Event Participant Section
     */
    public EventParticipant get(String UUID) {
        for (EventParticipant eventParticipant : participants)
            if (eventParticipant.getPlayer().getUniqueId().toString().equalsIgnoreCase(UUID))
                return eventParticipant;

        return null;
    }

    public void addParticipant(Player player) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // save checkpoint
        if (playerStats.getCheckpoint() != null)
            CheckpointDB.savePlayerAsync(player);

        EventParticipant eventParticipant = new EventParticipant(player, playerStats.getLevel());
        participants.add(eventParticipant);
        playerStats.setLevel(runningEvent.getLevel().getName());
        playerStats.joinedEvent();
        player.teleport(runningEvent.getLevel().getStartLocation());
    }

    public void removeParticipant(Player player) {
        EventParticipant eventParticipant = get(player.getUniqueId().toString());

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // reset the cache and teleport player back
        if (CheckpointDB.hasCheckpoint(player.getUniqueId(), eventParticipant.getOriginalLevel()))
            CheckpointDB.loadPlayer(player.getUniqueId(), eventParticipant.getOriginalLevel());

        // do all setting changes to revert back
        playerStats.setLevel(eventParticipant.getOriginalLevel());
        playerStats.leftEvent();
        player.teleport(eventParticipant.getOriginalLocation());
        player.setHealth(20.0);

        participants.remove(eventParticipant);
    }

    public List<EventParticipant> getParticipants() {
        return participants;
    }

    /*
        Misc Utilities
     */
    public String formatName(EventType eventType) {
        if (eventType == EventType.PVP)
            return "PvP";
        else if (eventType == EventType.HALF_HEART)
            return "Half a Heart";
        else if (eventType == EventType.RISING_WATER)
            return "Rising Water";

        return null;
    }
}
