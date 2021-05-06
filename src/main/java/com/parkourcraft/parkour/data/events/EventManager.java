package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EventManager {

    private Event runningEvent;

    public EventManager() {
        startScheduler();
    }

    // scheduler to handle the next event and reminder for running event
    public void startScheduler() {
        // run a timer scheduler for next event

        // run a timer scheduler for reminder to join running event
    }

    // method to start event
    public void startEvent(EventType eventType) {

    }

    // method to start the timer
    private void startTimer() {

    }

    // method to end event
    public void endEvent(boolean forceEnded) {
        // cancel scheduler first
        runningEvent.getScheduler().cancel();

        // then teleport all players back to where they entered
        for (String participant : runningEvent.getParticipants()) {
            Player participantPlayer = Bukkit.getPlayer(participant);
            removeParticipant(participantPlayer);
        }

        if (forceEnded) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(Utils.translate("&7An event has been started! &bType &l/event join &bto join!"));
            Bukkit.broadcastMessage("");
        } else {

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
}
