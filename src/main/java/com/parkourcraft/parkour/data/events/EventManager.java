package com.parkourcraft.parkour.data.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventManager {

    private Event runningEvent;

    public void startEvent(EventType eventType) {

    }

    public void endEvent(boolean forceEnded) {
        // cancel scheduler first
        runningEvent.getScheduler().cancel();

        // then teleport all players back to where they entered
        for (String participant : runningEvent.getParticipants()) {
            Player participantPlayer = Bukkit.getPlayer(participant);
            removeParticipant(participantPlayer);
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
