package com.renatusnetwork.parkour.data.races.gamemode;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.gameplay.handlers.LevelHandler;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RacePlayer
{
    private PlayerStats playerStats;
    private Race race;
    private Location originalLocation;
    private RacePlayer opponent;

    public RacePlayer(PlayerStats playerStats, Race race, Location originalLocation)
    {
        this.playerStats = playerStats;
        this.race = race;
        this.originalLocation = originalLocation;
    }

    public void start()
    {
        StatsManager statsManager = Parkour.getStatsManager();
        // toggle off elytra
        statsManager.toggleOffElytra(playerStats);

        Level raceLevel = race.getLevel();

        // hide player
        Parkour.getPlayerHiderManager().toggleOn(playerStats.getPlayer(), Utils.getSlotFromHotbarInventory(Utils.translate("&2Players &7» &2Enabled")));

        playerStats.setLevel(raceLevel);
        playerStats.disableLevelStartTime();
        playerStats.teleport(raceLevel.getStartLocation());

        if (race.hasBet())
            statsManager.removeCoins(playerStats, getBet());

        // remove potion effects
        playerStats.clearPotionEffects();

        // freeze and do countdown
        new BukkitRunnable()
        {
            int runCycles = 0;
            public void run()
            {
                // other left mid countdown
                if (!playerStats.inRace())
                    cancel();
                else
                {
                    Location spawnLocation = raceLevel.getStartLocation();

                    // race location variables
                    double raceX = spawnLocation.getX();
                    double raceZ = spawnLocation.getZ();

                    // player location variables
                    double playerX = playerStats.getPlayer().getLocation().getX();
                    double playerZ = playerStats.getPlayer().getLocation().getZ();

                    // teleport back if moved
                    if (raceX != playerX || raceZ != playerZ)
                        tpBack(playerStats, spawnLocation);

                    switch (runCycles)
                    {
                        case 0:
                            sendTitleAndPlaySound(playerStats, "&25");
                            break;
                        case 20:
                            sendTitleAndPlaySound(playerStats, "&a4");
                            break;
                        case 40:
                            sendTitleAndPlaySound(playerStats, "&e3");
                            break;
                        case 60:
                            sendTitleAndPlaySound(playerStats, "&62");
                            break;
                        case 80:
                            sendTitleAndPlaySound(playerStats, "&c1");
                            break;
                        case 100:
                            cancel();
                            sendTitleAndPlaySound(playerStats, "&4RACE");
                            break;
                    }
                    runCycles++;
                }
            }
        }.runTaskTimer(Parkour.getPlugin(), 1, 1);
    }

    public void end()
    {
        race.end(this, RaceEndReason.COMPLETED);
    }

    public void end(RaceEndReason raceEndReason)
    {
        race.end(this, getOpponent(), raceEndReason);
    }

    public void end(RacePlayer winner, RaceEndReason endReason)
    {
        race.end(winner, winner.getOpponent(), endReason);
    }

    public void win()
    {
        playerStats.endRace();
        Parkour.getStatsManager().updateRaceWins(playerStats, playerStats.getRaceWins() + 1);

        if (hasBet())
            Parkour.getStatsManager().addCoins(playerStats, (getBet() * 2));
    }

    public void loss()
    {
        playerStats.endRace();
        Parkour.getStatsManager().updateRaceWins(playerStats, playerStats.getRaceWins() + 1);
    }

    public void resetLevelAndTeleport()
    {
        playerStats.teleport(originalLocation);
        LevelHandler.setLevelInfoOnTeleport(playerStats, originalLocation);
    }

    private void sendTitleAndPlaySound(PlayerStats playerStats, String title)
    {
        Player player = playerStats.getPlayer();

        TitleAPI.sendTitle(player, 0, 20, 0, title, "");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
    }

    private void tpBack(PlayerStats playerStats, Location location)
    {
        Player player = playerStats.getPlayer();

        Location raceLoc = location.clone();
        raceLoc.setYaw(player.getLocation().getYaw());
        raceLoc.setPitch(player.getLocation().getPitch());
        player.teleport(raceLoc);
    }

    public PlayerStats getPlayerStats()
    {
        return playerStats;
    }

    public Level getLevel()
    {
        return race.getLevel();
    }

    public int getBet()
    {
        return race.getBet();
    }

    public boolean hasBet() { return race.hasBet(); }

    public Location getOriginalLocation()
    {
        return originalLocation;
    }

    public void setOpponent(RacePlayer opponent)
    {
        this.opponent = opponent;
    }

    public RacePlayer getOpponent()
    {
        return opponent;
    }
}
