package com.renatusnetwork.momentum.data.races.gamemode;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.elo.ELOOutcomeTypes;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
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
    private boolean disabledPlayers;

    public RacePlayer(PlayerStats playerStats, Race race, Location originalLocation)
    {
        this.playerStats = playerStats;
        this.race = race;
        this.originalLocation = originalLocation;
    }

    public void start()
    {
        StatsManager statsManager = Momentum.getStatsManager();
        // toggle off elytra
        statsManager.toggleOffElytra(playerStats);

        Level raceLevel = race.getLevel();
        Player player = playerStats.getPlayer();

        // hide player
        if (!statsManager.containsHiddenPlayer(player))
        {
            statsManager.togglePlayerHiderOn(player, false);
            disabledPlayers = true;
        }

        playerStats.setLevel(raceLevel);
        playerStats.resetFails();
        playerStats.resetCurrentCheckpoint();
        playerStats.disableLevelStartTime();
        playerStats.teleport(raceLevel.getStartLocation(), false);

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
                            sendTitleAndPlaySound(playerStats, "&45");
                            break;
                        case 20:
                            sendTitleAndPlaySound(playerStats, "&c4");
                            break;
                        case 40:
                            sendTitleAndPlaySound(playerStats, "&63");
                            break;
                        case 60:
                            sendTitleAndPlaySound(playerStats, "&e2");
                            break;
                        case 80:
                            sendTitleAndPlaySound(playerStats, "&a1");
                            break;
                        case 100:
                            cancel();
                            sendTitleAndPlaySound(playerStats, "&2RACE");
                            break;
                    }
                    runCycles++;
                }
            }
        }.runTaskTimer(Momentum.getPlugin(), 1, 1);
    }

    public void win()
    {
        StatsManager statsManager = Momentum.getStatsManager();

        playerStats.resetRace();
        statsManager.updateRaceWins(playerStats, playerStats.getRaceWins() + 1);

        if (hasBet())
            statsManager.addCoins(playerStats, (getBet() * 2));

        statsManager.calculateNewELO(playerStats, opponent.getPlayerStats(), ELOOutcomeTypes.WIN);
    }

    public void loss()
    {
        StatsManager statsManager = Momentum.getStatsManager();

        playerStats.resetRace();
        statsManager.updateRaceLosses(playerStats, playerStats.getRaceLosses() + 1);
        statsManager.calculateNewELO(playerStats, opponent.getPlayerStats(), ELOOutcomeTypes.LOSS);
    }

    public void resetLevelAndTeleport()
    {
        playerStats.teleport(originalLocation, false);
        Momentum.getLevelManager().setLevelInfoOnTeleport(playerStats, originalLocation);
    }

    public Race getRace()
    {
        return race;
    }

    private void sendTitleAndPlaySound(PlayerStats playerStats, String title)
    {
        Player player = playerStats.getPlayer();

        playerStats.sendTitle(title, "", 0, 15, 5);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
    }

    public void sendEndTitle(PlayerStats winner)
    {
        String titleMessage = Utils.translate("&c" + winner.getDisplayName() + "&7 won the race");
        String subTitleMessage = Utils.translate("&7On &c" + getLevel().getTitle());

        if (race.hasBet())
            subTitleMessage += "&7 for &6" + Utils.formatNumber(race.getBet()) + " &eCoins";

        playerStats.sendTitle(titleMessage, subTitleMessage, 10, 60, 10);
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

    public void resetPracAndCP()
    {
        Momentum.getStatsManager().resetPracticeDataOnly(playerStats);
        playerStats.resetCurrentCheckpoint();
    }

    public void showPlayersIfDisabled()
    {
        StatsManager statsManager = Momentum.getStatsManager();

        if (disabledPlayers && statsManager.containsHiddenPlayer(playerStats.getPlayer()))
            statsManager.togglePlayerHiderOff(playerStats.getPlayer(), false);
    }
}
