package com.parkourcraft.parkour.data.events;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class EventManager {

    private Event runningEvent = null;
    private BukkitTask maxRunTimer;
    private Set<EventParticipant> participants = new HashSet<>();
    private Set<String> eliminated = new HashSet<>();

    public EventManager() {
        startScheduler();
    }

    // scheduler to handle the next event and reminder for running event
    public void startScheduler() {
        // run a timer scheduler for next event
        new BukkitRunnable() {
            @Override
            public void run() {
                // check if there is enough people online and an event isnt running
                if (runningEvent == null &&
                    Bukkit.getOnlinePlayers().size() >= Parkour.getSettingsManager().min_players_online) {

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
                    endEvent(null,false, true);
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().max_event_run_time);
    }

    // method to end event
    public void endEvent(Player winner, boolean forceEnded, boolean ranOutOfTime) {
        // cancel schedulers first
        runningEvent.getScheduler().cancel();
        maxRunTimer.cancel();

        // then remove all participants
        removeAllParticipants(false);
        // clear eliminated list
        eliminated.clear();
        // clear all water if it was the rising water event
        clearWater();

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
        if (playerStats.getCheckpoint() != null) {
            CheckpointDB.savePlayerAsync(player);
            playerStats.resetCheckpoint();
        }

        EventParticipant eventParticipant = new EventParticipant(player, playerStats.getLevel());
        participants.add(eventParticipant);
        playerStats.setLevel(runningEvent.getLevel().getName());
        playerStats.disableLevelStartTime();
        playerStats.joinedEvent();
        player.teleport(runningEvent.getLevel().getStartLocation());
    }

    public void removeParticipant(Player player, boolean disconnected) {
        EventParticipant eventParticipant = get(player.getUniqueId().toString());

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // reset the cache and teleport player back
        if (!disconnected && CheckpointDB.hasCheckpoint(player.getUniqueId(), eventParticipant.getOriginalLevel()))
            CheckpointDB.loadPlayer(player.getUniqueId(), eventParticipant.getOriginalLevel());

        // do all setting changes to revert back
        playerStats.setLevel(eventParticipant.getOriginalLevel());
        playerStats.leftEvent();
        player.teleport(eventParticipant.getOriginalLocation());
        player.setHealth(20.0);

        participants.remove(eventParticipant);
    }

    public void removeAllParticipants(boolean shutdown) {

        List<EventParticipant> tempList = new ArrayList<>();

        // create a DEEP copy of the list so no concurrent errors
        for (EventParticipant eventParticipant : participants)
            tempList.add(eventParticipant);

        // now remove so theres no concurrency problem
        for (EventParticipant participant : tempList)
            removeParticipant(participant.getPlayer(), shutdown);
    }

    public Set<EventParticipant> getParticipants() {
        return participants;
    }

    public Set<String> getEliminated() {
        return eliminated;
    }

    public boolean isEliminated(Player player) {
        if (eliminated.contains(player.getName()))
            return true;
        return false;
    }

    public void addEliminated(Player player) {
        eliminated.add(player.getName());
    }

    public void removeEliminated(Player player) {
        eliminated.remove(player.getName());
    }

    public void doFireworkExplosion(Location location) {

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.clearEffects();

        // build the firework and then set the new one
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(false)
                .trail(false)
                .with(FireworkEffect.Type.BURST)
                .withColor(Color.RED)
                .withFade(Color.RED)
                .build();

        meta.addEffect(effect);
        firework.setFireworkMeta(meta);

        new BukkitRunnable() {
            public void run() {
                firework.detonate();
            }
        }.runTaskLater(Parkour.getPlugin(), 1);
    }

    private void clearWater() {

        if (isEventRunning() &&
            runningEvent.getLevel() != null &&
            runningEvent.getLevelRegion() != null &&
            runningEvent.getEventType() == EventType.RISING_WATER) {

            BlockVector maxPoint = runningEvent.getLevelRegion().getMaximumPoint().toBlockPoint();
            BlockVector minPoint = runningEvent.getLevelRegion().getMinimumPoint().toBlockPoint();
            int minX = minPoint.getBlockX();
            int maxX = maxPoint.getBlockX();
            int minZ = minPoint.getBlockZ();
            int maxZ = maxPoint.getBlockZ();

            WorldEdit FAWEAPI = WorldEdit.getInstance();

            if (FAWEAPI != null) {

                LocalWorld world = new BukkitWorld(runningEvent.getLevel().getStartLocation().getWorld());
                Vector pos1 = new Vector(minX, 0, minZ);
                Vector pos2 = new Vector(maxX, 255, maxZ);
                CuboidRegion selection = new CuboidRegion(world, pos1, pos2);

                try {
                    // enable fast mode to do it w/o lag, then quickly disable fast mode once queue flushed
                    EditSession editSession = FAWEAPI.getInstance().getEditSessionFactory().getEditSession(world, -1);
                    editSession.setFastMode(true);

                    // create single base block set for replace
                    Set<BaseBlock> baseBlockSet = new HashSet<BaseBlock>() {{ add(new BaseBlock(Material.WATER.getId())); }};

                    editSession.replaceBlocks(selection, baseBlockSet, new BaseBlock(Material.AIR.getId()));
                    editSession.flushQueue();
                    editSession.setFastMode(false);
                } catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }
            } else {
                Parkour.getPluginLogger().info("FAWE API found null in Event clearWater()");
            }
        }
    }

    public boolean isStartCoveredInWater() {

        boolean isCovered = false;

        if (isEventRunning() &&
            runningEvent.getLevel() != null &&
            runningEvent.getEventType() == EventType.RISING_WATER) {

            Location startLoc = getRunningEvent().getLevel().getStartLocation();

            if (startLoc.clone().getBlock().getType() == Material.WATER ||
                startLoc.clone().add(0, 1, 0).getBlock().getType() == Material.WATER)
                isCovered = true;
        }
        return isCovered;
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

    public void shutdown() {
        if (isEventRunning()) {
            runningEvent.getScheduler().cancel();
            removeAllParticipants(true);
            clearWater();
            runningEvent = null;
        }
    }
}
