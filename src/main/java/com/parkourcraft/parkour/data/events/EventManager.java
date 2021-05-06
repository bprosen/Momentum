package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class EventManager {

    private Event runningEvent = null;
    private BukkitTask maxRunTimer;

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
                    Bukkit.broadcastMessage(Utils.translate("&7An &b" + formatName(runningEvent.getEventType()) +
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
        Bukkit.broadcastMessage(Utils.translate("&7An &b" + formatName(runningEvent.getEventType()) +
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
        for (String participant : runningEvent.getParticipants()) {
            Player participantPlayer = Bukkit.getPlayer(participant);
            removeParticipant(participantPlayer);
        }

        if (forceEnded) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A " + formatName(runningEvent.getEventType())
                    + " &7Event has been force ended!"));
            Bukkit.broadcastMessage("");
        } else if (ranOutOfTime) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A " + formatName(runningEvent.getEventType())
                    + " &7Event has gone on too long! Nobody beat it in time :("));
            Bukkit.broadcastMessage("");
        } else {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7A " + formatName(runningEvent.getEventType())
                    + " &7Event has ended! &b&l" + winner.getName() + " &7has won!"));
            Bukkit.broadcastMessage("");
        }

        // null the running event last
        runningEvent = null;
    }

    public Event getRunningEvent() {
        return runningEvent;
    }

    public boolean isEventRunning() {
        if (runningEvent != null)
            return true;
        return false;
    }

    public boolean isParticipant(Player player) {
        boolean participant = false;

        if (isEventRunning() && runningEvent.isParticipant(player))
            participant = true;

        return participant;
    }
    public void addParticipant(Player player) {
        if (!runningEvent.isParticipant(player)) {
            runningEvent.addParticipant(player);
            player.teleport(runningEvent.getLevel().getStartLocation());
        }
    }

    public void removeParticipant(Player player) {
        if (runningEvent.isParticipant(player)) {
            player.teleport(runningEvent.getOriginalLoc(player));
            runningEvent.removeParticipant(player);
        }
    }

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
