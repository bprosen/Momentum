package com.renatusnetwork.momentum.data.events;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.api.ParkourEventEndEvent;
import com.renatusnetwork.momentum.data.events.types.*;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.leaderboards.EventLBPosition;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
import com.renatusnetwork.momentum.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EventManager {

    private Event runningEvent = null;
    private Player winner = null;
    private BukkitTask maxRunTimer;
    private BukkitTask reminderTimer;
    private long startTime;

    private HashMap<String, EventParticipant> participants;
    private Set<String> eliminated;

    private ArrayList<EventLBPosition> eventLB;

    public EventManager() {
        this.participants = new HashMap<>();
        this.eliminated = new HashSet<>();
        this.eventLB = new ArrayList<>(Momentum.getSettingsManager().max_event_leaderboard_size);

        startScheduler();
    }

    // method to start event
    public void startEvent(Event event) {
        runningEvent = event;
        startTime = System.currentTimeMillis();

        broadcastComponent(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " Event &7has begun! &cClick here to join"));

        // start max time timer
        startTimers();
    }

    // method to end event
    public void endEvent(Player winner, boolean forceEnded, boolean ranOutOfTime) {
        PlayerStats playerStats = null;
        if (winner != null) {
            playerStats = Momentum.getStatsManager().get(winner);
        }

        ParkourEventEndEvent parkourEventEndEvent = new ParkourEventEndEvent(playerStats, runningEvent.getLevel().getReward());
        Bukkit.getPluginManager().callEvent(parkourEventEndEvent);

        if (!parkourEventEndEvent.isCancelled()) {
            this.winner = winner;

            // cancel schedulers first
            runningEvent.end();
            maxRunTimer.cancel();
            reminderTimer.cancel();

            // then remove all participants
            removeAllParticipants(false);
            // clear eliminated list
            eliminated.clear();

            if (winner != null) {
                // give higher reward if prestiged
                int prestiges = playerStats.getPrestiges();
                int reward = parkourEventEndEvent.getReward();

                if (playerStats.hasModifier(ModifierType.EVENT_BOOSTER)) {
                    Booster booster = (Booster) playerStats.getModifier(ModifierType.EVENT_BOOSTER);
                    reward *= booster.getMultiplier();
                }

                if (prestiges > 0 && reward > 0) {
                    reward *= playerStats.getPrestigeMultiplier();
                }

                Momentum.getStatsManager().addCoins(playerStats, reward);
                Momentum.getStatsManager().runGGTimer();

                playerStats.getPlayer().sendMessage(Utils.translate("&7You have been rewarded " + Utils.getCoinFormat(runningEvent.getLevel().getReward(), reward) + " &eCoins"));

                Momentum.getStatsManager().addEventWin(playerStats);
            }

            if (forceEnded) {
                Bukkit.broadcastMessage(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " &7Event has been force ended!"));
            } else if (ranOutOfTime) {
                Bukkit.broadcastMessage(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " &7Event has gone on too long! Nobody beat it in time :("));
            } else {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " &7Event has ended! &b&l" + winner.getDisplayName() + " &7has won!"));
                Bukkit.broadcastMessage("");
            }

            // null the running event last
            runningEvent = null;
        }
    }

    // scheduler to handle the next event and reminder for running event
    public void startScheduler() {
        // run a timer scheduler for next event
        new BukkitRunnable() {
            @Override
            public void run() {
                // check if there is enough people online and an event isnt running
                if (runningEvent == null && Bukkit.getOnlinePlayers().size() >= Momentum.getSettingsManager().min_players_online) {

                    // get random type from list
                    EventType[] eventTypes = EventType.values();
                    Random ran = new Random();
                    EventType eventType = eventTypes[ran.nextInt(eventTypes.length)];
                    List<Level> eventLevels = Momentum.getLevelManager().getEventLevelsFromType(eventType);

                    if (!eventLevels.isEmpty()) {
                        Level eventLevel = eventLevels.get(ran.nextInt(eventLevels.size()));

                        switch (eventType) {
                            case PVP:
                                startEvent(new PvPEvent(eventLevel));
                                break;
                            case RISING_WATER:
                                startEvent(new RisingWaterEvent(eventLevel));
                                break;
                            case FALLING_ANVIL:
                                startEvent(new FallingAnvilEvent(eventLevel));
                                break;
                            case ASCENT:
                                startEvent(new AscentEvent(eventLevel));
                                break;
                            case MAZE:
                                startEvent(new MazeEvent(eventLevel));
                                break;
                        }
                    }
                }
            }
        }.runTaskTimer(Momentum.getPlugin(), 20 * Momentum.getSettingsManager().check_next_event_delay,
                       20 * Momentum.getSettingsManager().check_next_event_delay);

        // update global event wins every 3 mins
        new BukkitRunnable() {
            @Override
            public void run() {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 10, 20 * 180);
    }

    // method to start the timer
    private void startTimers() {
        maxRunTimer = new BukkitRunnable() {
            @Override
            public void run() {

                if (runningEvent != null) {
                    endEvent(null, false, true);
                }

            }
        }.runTaskLater(Momentum.getPlugin(), 20 * Momentum.getSettingsManager().max_event_run_time);

        // run a timer scheduler for reminder to join running event
        reminderTimer = new BukkitRunnable() {
            @Override
            public void run() {
                long endTimeMillis = startTime + (Momentum.getSettingsManager().max_event_run_time * 1000);

                // if the event is running and the end time will be in sync to when the reminder broadcast is, dont do it
                if (runningEvent != null &&
                    (endTimeMillis - (20 * 1000 * Momentum.getSettingsManager().event_reminder_delay)) < System.currentTimeMillis()) {
                    broadcastComponent(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " Event &7is still running! &cClick here to join!"));
                }
            }
        }.runTaskTimer(Momentum.getPlugin(), 20 * Momentum.getSettingsManager().event_reminder_delay,
                       20 * Momentum.getSettingsManager().event_reminder_delay);
    }

    public void broadcastComponent(String message) {
        Bukkit.broadcastMessage("");
        Utils.broadcastClickableHoverableCMD(message, "&bClick to join!", "/event join");
        Bukkit.broadcastMessage("");
    }

    public Event getRunningEvent() {
        return runningEvent;
    }

    public boolean isEventRunning() {
        return runningEvent != null;
    }

    /*
        Event Participant Section
     */
    public EventParticipant get(Player player) {
        return participants.get(player.getName());
    }

    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getName());
    }

    public void addParticipant(Player player) {

        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        // save checkpoint
        playerStats.resetCurrentCheckpoint();

        // toggle off if saved
        Momentum.getStatsManager().toggleOffElytra(playerStats);

        EventParticipant eventParticipant = new EventParticipant(player, playerStats.getLevel());
        participants.put(player.getName(), eventParticipant);
        playerStats.setLevel(runningEvent.getLevel());
        playerStats.disableLevelStartTime();
        playerStats.joinedEvent();

        // add to map
        if (isAscentEvent()) {
            ((AscentEvent) runningEvent).add(player);
        } else if (isMazeEvent()) {
            ((MazeEvent) runningEvent).respawn(player);
        } else {
            playerStats.teleport(runningEvent.getLevel().getStartLocation(), true);
        }

        // remove active effects
        playerStats.clearPotionEffects();
    }

    public void removeParticipant(Player player, boolean disconnected) {
        EventParticipant eventParticipant = get(player);

        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        if (!disconnected && eventParticipant.getOriginalLevel() != null) {
            Location location = playerStats.getCheckpoint(eventParticipant.getOriginalLevel());

            // reset the cache and teleport player back
            if (location != null) {
                playerStats.setCurrentCheckpoint(location);
            }
        }

        // if their original level is not null, then set it, if it is, do region lookup of their original location jic
        if (eventParticipant.getOriginalLevel() != null) {
            playerStats.setLevel(eventParticipant.getOriginalLevel());
        } else {
            // region lookup here
            ProtectedRegion region = WorldGuard.getRegion(eventParticipant.getOriginalLocation());
            if (region != null) {

                // level lookup here
                Level level = Momentum.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level
                if (level != null) {
                    playerStats.setLevel(level);
                } else {
                    playerStats.resetLevel();
                }
            } else {
                playerStats.resetLevel();
            }
        }

        playerStats.leftEvent();
        playerStats.teleport(eventParticipant.getOriginalLocation(), true);
        Utils.applySlowness(player);
        player.setHealth(20.0);

        if (isAscentEvent()) {
            ((AscentEvent) runningEvent).remove(player);
        }

        if (!disconnected && winner != null) {
            playerStats.sendTitle("&c" + winner.getDisplayName() + " &7has won the &2&l" + runningEvent.getFormattedName() + " &7Event", "", 10, 80, 10);
        }

        // set back if they came from elytra level
        if (playerStats.inLevel() && playerStats.getLevel().isElytra()) {
            Momentum.getStatsManager().toggleOnElytra(playerStats);
        }

        participants.remove(player.getName());
    }

    public void removeAllParticipants(boolean shutdown) {

        Set<EventParticipant> tempList = new HashSet<>();

        // create a DEEP copy of the list so no concurrent errors
        for (EventParticipant eventParticipant : participants.values()) {
            tempList.add(eventParticipant);
        }

        // now remove so theres no concurrency problem
        for (EventParticipant participant : tempList) {
            removeParticipant(participant.getPlayer(), shutdown);
        }

        // null winner once all participants have been handled
        winner = null;
    }

    public HashMap<String, EventParticipant> getParticipants() {
        return participants;
    }

    public boolean isEliminated(Player player) {
        return eliminated.contains(player.getName());
    }

    public void addEliminated(Player player) {
        eliminated.add(player.getName());
    }

    public long getTimeLeftMillis() {
        return (startTime + (Momentum.getSettingsManager().max_event_run_time * 1000)) - System.currentTimeMillis();
    }

    public void shutdown() {
        if (isEventRunning()) {
            runningEvent.end();
            removeAllParticipants(true);
            runningEvent = null;
        }
    }

    public void loadLeaderboard() {
        try {
            ArrayList<EventLBPosition> leaderboard = eventLB;
            leaderboard.clear();

            List<Map<String, String>> winResults = DatabaseQueries.getResults(
                    "players",
                    "uuid, name, event_wins",
                    "WHERE event_wins > 0" +
                    " ORDER BY event_wins DESC" +
                    " LIMIT " + Momentum.getSettingsManager().max_event_leaderboard_size);

            for (Map<String, String> winResult : winResults) {
                leaderboard.add(
                        new EventLBPosition(
                                winResult.get("uuid"), winResult.get("name"), Integer.parseInt(winResult.get("event_wins"))
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<EventLBPosition> getEventLeaderboard() {
        return eventLB;
    }

    // easy methods
    public boolean isPvPEvent() {
        return runningEvent instanceof PvPEvent;
    }

    public boolean isRisingWaterEvent() {
        return runningEvent instanceof RisingWaterEvent;
    }

    public boolean isFallingAnvilEvent() {
        return runningEvent instanceof FallingAnvilEvent;
    }

    public boolean isAscentEvent() {
        return runningEvent instanceof AscentEvent;
    }

    public boolean isMazeEvent() {
        return runningEvent instanceof MazeEvent;
    }
}
