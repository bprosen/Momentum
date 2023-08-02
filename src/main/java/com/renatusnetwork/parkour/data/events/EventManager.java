package com.renatusnetwork.parkour.data.events;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.ParkourEventEndEvent;
import com.renatusnetwork.parkour.data.events.types.*;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.modifiers.ModifierTypes;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
    private Player winner = null;
    private BukkitTask maxRunTimer;
    private BukkitTask reminderTimer;
    private HashMap<String, EventParticipant> participants = new HashMap<>();
    private Set<String> eliminated = new HashSet<>();

    // start millis according to system
    private long startTime = 0L;
    private HashMap<Integer, EventLBPosition> eventLeaderboard = new HashMap<>(Parkour.getSettingsManager().max_event_leaderboard_size);

    public EventManager() {
        startScheduler();
    }

    // method to start event
    public void startEvent(Event event)
    {
        runningEvent = event;
        startTime = System.currentTimeMillis();

        broadcastComponent(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " Event &7has begun! &cClick here to join!"));

        // start max time timer
        startTimers();
    }

    // method to end event
    public void endEvent(Player winner, boolean forceEnded, boolean ranOutOfTime)
    {
        PlayerStats playerStats = Parkour.getStatsManager().get(winner);

        ParkourEventEndEvent parkourEventEndEvent = new ParkourEventEndEvent(playerStats, runningEvent.getLevel().getReward());
        Bukkit.getPluginManager().callEvent(parkourEventEndEvent);

        if (!parkourEventEndEvent.isCancelled())
        {
            this.winner = winner;

            // cancel schedulers first
            runningEvent.end();
            maxRunTimer.cancel();
            reminderTimer.cancel();

            // then remove all participants
            removeAllParticipants(false);
            // clear eliminated list
            eliminated.clear();

            if (winner != null)
            {
                // give higher reward if prestiged
                int prestiges = playerStats.getPrestiges();
                int reward = parkourEventEndEvent.getReward();

                if (playerStats.hasModifier(ModifierTypes.EVENT_BOOSTER))
                {
                    Booster booster = (Booster) playerStats.getModifier(ModifierTypes.EVENT_BOOSTER);
                    reward *= booster.getMultiplier();
                }

                if (prestiges > 0 && reward > 0)
                    reward *= playerStats.getPrestigeMultiplier();

                Parkour.getStatsManager().addCoins(playerStats, reward);
                Parkour.getStatsManager().runGGTimer();
                
                playerStats.getPlayer().sendMessage(Utils.translate("&7You have been rewarded " + Utils.getCoinFormat(runningEvent.getLevel().getReward(), reward) + " &eCoins"));

                // update wins
                playerStats.addEventWin();
                Parkour.getDatabaseManager().add("UPDATE players SET event_wins=" + playerStats.getEventWins() + " WHERE uuid='" + playerStats.getUUID() + "'");
            }

            if (forceEnded)
                Bukkit.broadcastMessage(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " &7Event has been force ended!"));
            else if (ranOutOfTime)
                Bukkit.broadcastMessage(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " &7Event has gone on too long! Nobody beat it in time :("));
            else {
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
                if (runningEvent == null && Bukkit.getOnlinePlayers().size() >= Parkour.getSettingsManager().min_players_online)
                {

                    // get random type from list
                    EventType[] eventTypes = EventType.values();
                    Random ran = new Random();
                    EventType eventType = eventTypes[ran.nextInt(eventTypes.length)];
                    List<Level> eventLevels = Parkour.getLevelManager().getEventLevelsFromType(eventType);
                    Level eventLevel = eventLevels.get(ran.nextInt(eventLevels.size()));

                    switch (eventType)
                    {
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
        }.runTaskTimer(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().check_next_event_delay,
                20 * Parkour.getSettingsManager().check_next_event_delay);

        // update global event wins every 3 mins
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                loadLeaderboard();
            }
        }.runTaskTimerAsynchronously(Parkour.getPlugin(), 20 * 60, 20 * 180);
    }

    // method to start the timer
    private void startTimers() {
        maxRunTimer = new BukkitRunnable() {
            @Override
            public void run() {

                if (runningEvent != null)
                    endEvent(null,false, true);

            }
        }.runTaskLater(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().max_event_run_time);

        // run a timer scheduler for reminder to join running event
        reminderTimer = new BukkitRunnable() {
            @Override
            public void run() {
                long endTimeMillis = startTime + (Parkour.getSettingsManager().max_event_run_time * 1000);

                // if the event is running and the end time will be in sync to when the reminder broadcast is, dont do it
                if (runningEvent != null &&
                   (endTimeMillis - (20 * 1000 * Parkour.getSettingsManager().event_reminder_delay)) < System.currentTimeMillis())

                    broadcastComponent(Utils.translate("&7A &b" + runningEvent.getFormattedName() + " Event &7is still running! &cClick here to join!"));
            }
        }.runTaskTimer(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().event_reminder_delay,
                                           20 * Parkour.getSettingsManager().event_reminder_delay);
    }

    public void broadcastComponent(String message)
    {
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

    public void addParticipant(Player player)
    {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        // save checkpoint
        playerStats.resetCurrentCheckpoint();

        // toggle off if saved
        Parkour.getStatsManager().toggleOffElytra(playerStats);

        EventParticipant eventParticipant = new EventParticipant(player, playerStats.getLevel());
        participants.put(player.getName(), eventParticipant);
        playerStats.setLevel(runningEvent.getLevel());
        playerStats.disableLevelStartTime();
        playerStats.joinedEvent();

        // add to map
        if (isAscentEvent())
            ((AscentEvent) runningEvent).add(player);

        player.teleport(runningEvent.getLevel().getStartLocation());

        // remove active effects
        playerStats.clearPotionEffects();
    }

    public void removeParticipant(Player player, boolean disconnected)
    {
        EventParticipant eventParticipant = get(player);

        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (!disconnected && eventParticipant.getOriginalLevel() != null)
        {
            Location location = playerStats.getCheckpoint(eventParticipant.getOriginalLevel().getName());

            // reset the cache and teleport player back
            if (location != null)
                playerStats.setCurrentCheckpoint(location);
        }

        // if their original level is not null, then set it, if it is, do region lookup of their original location jic
        if (eventParticipant.getOriginalLevel() != null)
            playerStats.setLevel(eventParticipant.getOriginalLevel());
        else {
            // region lookup here
            ProtectedRegion region = WorldGuard.getRegion(eventParticipant.getOriginalLocation());
            if (region != null) {

                // level lookup here
                Level level = Parkour.getLevelManager().get(region.getId());

                // make sure the area they are spawning in is a level
                if (level != null)
                    playerStats.setLevel(level);
                else
                    playerStats.resetLevel();
            } else {
                playerStats.resetLevel();
            }
        }

        playerStats.leftEvent();
        player.teleport(eventParticipant.getOriginalLocation());
        player.setHealth(20.0);

        if (isAscentEvent())
            ((AscentEvent) runningEvent).remove(player);

        if (!disconnected && winner != null)
            TitleAPI.sendTitle(eventParticipant.getPlayer(), 10, 80, 10,
                    Utils.translate("&c" + winner.getDisplayName() + " &7has won the &2&l" + runningEvent.getFormattedName() + " &7Event")
            );

        // set back if they came from elytra level
        if (playerStats.inLevel() && playerStats.getLevel().isElytraLevel())
            Parkour.getStatsManager().toggleOnElytra(playerStats);

        participants.remove(player.getName());
    }

    public void removeAllParticipants(boolean shutdown)
    {

        Set<EventParticipant> tempList = new HashSet<>();

        // create a DEEP copy of the list so no concurrent errors
        for (EventParticipant eventParticipant : participants.values())
            tempList.add(eventParticipant);

        // now remove so theres no concurrency problem
        for (EventParticipant participant : tempList)
            removeParticipant(participant.getPlayer(), shutdown);

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

    public long getTimeLeftMillis()
    {
        return (startTime + (Parkour.getSettingsManager().max_event_run_time * 1000)) - System.currentTimeMillis();
    }

    public void shutdown()
    {
        if (isEventRunning())
        {
            runningEvent.end();
            removeAllParticipants(true);
            runningEvent = null;
        }
    }

    public void loadLeaderboard() {
        try {

            HashMap<Integer, EventLBPosition> leaderboard = eventLeaderboard;
            leaderboard.clear();

            List<Map<String, String>> winResults = DatabaseQueries.getResults(
                    "players",
                    "uuid, player_name, event_wins",
                    " WHERE event_wins > 0" +
                            " ORDER BY event_wins DESC" +
                            " LIMIT " + Parkour.getSettingsManager().max_event_leaderboard_size);

            int leaderboardPos = 1;

            for (Map<String, String> winResult : winResults) {
                leaderboard.put(leaderboardPos,
                        new EventLBPosition(
                                winResult.get("uuid"), winResult.get("player_name"), Integer.parseInt(winResult.get("event_wins"))
                        ));

                leaderboardPos++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<Integer, EventLBPosition> getEventLeaderboard()
    {
        return eventLeaderboard;
    }

    // easy methods
    public boolean isPvPEvent()
    {
        return runningEvent instanceof PvPEvent;
    }
    public boolean isRisingWaterEvent()
    {
        return runningEvent instanceof RisingWaterEvent;
    }
    public boolean isFallingAnvilEvent()
    {
        return runningEvent instanceof FallingAnvilEvent;
    }

    public boolean isAscentEvent()
    {
        return runningEvent instanceof AscentEvent;
    }
    public boolean isMazeEvent()
    {
        return runningEvent instanceof MazeEvent;
    }
}
