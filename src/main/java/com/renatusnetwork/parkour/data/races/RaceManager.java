package com.renatusnetwork.parkour.data.races;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.*;
import com.renatusnetwork.parkour.data.stats.*;
import com.renatusnetwork.parkour.data.leaderboards.RaceLBPosition;
import com.renatusnetwork.parkour.storage.mysql.DatabaseManager;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RaceManager {

    private Set<Race> runningRaceList;
    private Set<RaceRequest> raceRequests;
    private HashMap<Integer, RaceLBPosition> raceLeaderboard;

    public RaceManager()
    {
        this.runningRaceList = new HashSet<>();
        this.raceRequests = new HashSet<>();
        this.raceLeaderboard = new HashMap<>(Parkour.getSettingsManager().max_race_leaderboard_size);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                loadLeaderboard();
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    /*
        Race Section
     */
    public void startRace(PlayerStats playerStats1, PlayerStats playerStats2, RaceLevel selectedLevel, boolean bet, double betAmount)
    {
        Player player1 = playerStats1.getPlayer();
        Player player2 = playerStats2.getPlayer();

        // make sure it is not an invalid level
        if (selectedLevel != null)
        {

            // create object for the race
            Race newRace = new Race(player1, player2, selectedLevel, bet, betAmount);
            runningRaceList.add(newRace);

            StatsManager statsManager = Parkour.getStatsManager();
            // toggle off elytra
            statsManager.toggleOffElytra(playerStats1);
            statsManager.toggleOffElytra(playerStats2);

            playerStats1.startedRace();
            playerStats2.startedRace();
            playerStats1.setLevel(selectedLevel);
            playerStats2.setLevel(selectedLevel);

            playerStats1.getPlayer().teleport(selectedLevel.getSpawnLocation1());
            playerStats2.getPlayer().teleport(selectedLevel.getSpawnLocation2());

            if (bet)
            {
                statsManager.removeCoins(playerStats1, betAmount);
                statsManager.removeCoins(playerStats2, betAmount);
            }

            // remove potion effects
            playerStats1.clearPotionEffects();
            playerStats2.clearPotionEffects();

            Location spawnLocation1 = selectedLevel.getSpawnLocation1();
            Location spawnLocation2 = selectedLevel.getSpawnLocation2();

            // freeze and do countdown
            new BukkitRunnable()
            {
                int runCycles = 0;
                public void run()
                {
                    if (!runningRaceList.contains(newRace))
                    {
                        cancel();
                        return;
                    }

                    // cancel, send last title and return
                    if (runCycles == 100)
                    {
                        cancel();
                        sendTitleAndPlaySound(player1, "&4RACE");
                        sendTitleAndPlaySound(player2, "&4RACE");
                        playerStats1.startedLevel();
                        playerStats2.startedLevel();
                        return;
                    }

                    // race location variables
                    double race1X = spawnLocation1.getX();
                    double race1Z = spawnLocation1.getZ();
                    double race2X = spawnLocation2.getX();
                    double race2Z = spawnLocation2.getZ();

                    // player location variables
                    double player1X = player1.getLocation().getX();
                    double player1Z = player1.getLocation().getZ();
                    double player2X = player2.getLocation().getX();
                    double player2Z = player2.getLocation().getZ();

                    // teleport back if moved
                    if (race1X != player1X || race1Z != player1Z)
                        tpBack(player1, spawnLocation1);

                    if (race2X != player2X || race2Z != player2Z)
                        tpBack(player2, spawnLocation2);

                    // countdown if-else
                    if (runCycles == 0)
                    {
                        sendTitleAndPlaySound(player1, "&25");
                        sendTitleAndPlaySound(player2, "&25");
                    }
                    else if (runCycles == 20)
                    {
                        sendTitleAndPlaySound(player1, "&a4");
                        sendTitleAndPlaySound(player2, "&a4");
                    }
                    else if (runCycles == 40)
                    {
                        sendTitleAndPlaySound(player1, "&e3");
                        sendTitleAndPlaySound(player2, "&e3");
                    }
                    else if (runCycles == 60)
                    {
                        sendTitleAndPlaySound(player1, "&62");
                        sendTitleAndPlaySound(player2, "&62");
                    }
                    else if (runCycles == 80)
                    {
                        sendTitleAndPlaySound(player1, "&c1");
                        sendTitleAndPlaySound(player2, "&c1");
                    }
                    runCycles++;
                }
            }.runTaskTimer(Parkour.getPlugin(), 1, 1);
        }
        else
        {
            player1.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
            player2.sendMessage(Utils.translate("&cInvalid level? Try again or contact an Admin"));
        }
    }

    private void sendTitleAndPlaySound(Player player, String title)
    {
        TitleAPI.sendTitle(player, 0, 20, 0, title, "");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HAT, 8F, 2F);
    }

    private void tpBack(Player player, Location location)
    {
        Location raceLoc = location.clone();
        raceLoc.setYaw(player.getLocation().getYaw());
        raceLoc.setPitch(player.getLocation().getPitch());
        player.teleport(raceLoc);
    }

    public void forceEndRace(Race endedRace, boolean shutdown) {

        StatsManager statsManager = Parkour.getStatsManager();
        Player player1 = endedRace.getPlayer1();
        Player player2 = endedRace.getPlayer2();

        // teleport back
        player1.teleport(endedRace.getOriginalPlayer1Loc());
        player2.teleport(endedRace.getOriginalPlayer2Loc());

        // if server is not shutting down
        if (!shutdown) {
            player1.sendMessage(Utils.translate("&7You ran out of time to complete the race!"));
            player2.sendMessage(Utils.translate("&7You ran out of time to complete the race!"));
            // send title
            String titleString = Utils.translate("&7Ran Out of Time in Your Race");
            TitleAPI.sendTitle(endedRace.getPlayer1(), 10, 60, 10, titleString, "");
            TitleAPI.sendTitle(endedRace.getPlayer2(), 10, 60, 10, titleString, "");
        }

        // if has bet, give bet back
        if (endedRace.hasBet())
        {
            PlayerStats player1Stats = statsManager.get(player1);
            PlayerStats player2Stats = statsManager.get(player2);

            statsManager.addCoins(player1Stats, endedRace.getBet());
            statsManager.addCoins(player2Stats, endedRace.getBet());
        }

        // set level in cache and toggle back on elytra
        ProtectedRegion player1Region = WorldGuard.getRegion(endedRace.getPlayer1().getLocation());
        ProtectedRegion player2Region = WorldGuard.getRegion(endedRace.getPlayer2().getLocation());
        if (player1Region != null)
        {
            Level level = Parkour.getLevelManager().get(player1Region.getId());
            PlayerStats playerStats = statsManager.get(player1);
            playerStats.endedRace();
            playerStats.disableLevelStartTime();
            playerStats.setLevel(level);

            // if elytra level, give elytra
            if (level != null && level.isElytra())
                statsManager.toggleOnElytra(playerStats);
        }

        if (player2Region != null)
        {
            Level level = Parkour.getLevelManager().get(player2Region.getId());
            PlayerStats playerStats = statsManager.get(endedRace.getPlayer2());
            playerStats.endedRace();
            playerStats.disableLevelStartTime();
            playerStats.setLevel(level);

            // if elytra level, give elytra
            if (level != null && level.isElytra())
                statsManager.toggleOnElytra(playerStats);
        }

        // remove from list
        runningRaceList.remove(endedRace);
    }

    public void endRace(Player winner, boolean disconnected)
    {

        Race raceObject = get(winner);

        if (raceObject != null)
        {
            Player loser = raceObject.getOpponent(winner);
            StatsManager statsManager = Parkour.getStatsManager();

            PlayerStats winnerStats = statsManager.get(winner);
            PlayerStats loserStats = statsManager.get(loser);

            // apply completion stats to level
            LevelManager levelManager = Parkour.getLevelManager();

            // get max timer and cancel right away
            raceObject.getMaxTimer().cancel();

            // if they have not completed this individual level, then add
            if (!winnerStats.hasCompleted(raceObject.getLevel()))
                winnerStats.setIndividualLevelsBeaten(winnerStats.getIndividualLevelsBeaten() + 1);

            long elapsedTime = (System.currentTimeMillis() - winnerStats.getLevelStartTime());
            if (disconnected)
                elapsedTime = 0;

            LevelCompletion levelCompletion = new LevelCompletion(
                    raceObject.getLevel().getName(),
                    winner.getUniqueId().toString(),
                    winner.getName(),
                    System.currentTimeMillis(),
                    elapsedTime
            );

            winnerStats.setTotalLevelCompletions(winnerStats.getTotalLevelCompletions() + 1);
            CompletionsDB.insertCompletion(levelCompletion, false);
            levelManager.addTotalLevelCompletion();
            levelManager.addCompletion(winnerStats, raceObject.getLevel(), levelCompletion);

            // Update player information
            winnerStats.levelCompletion(levelCompletion);

            if (raceObject.hasBet())
                Bukkit.broadcastMessage(Utils.translate("&4" + winner.getDisplayName() + " &7has beaten &4" + loser.getDisplayName()
                                        + " &7in a race for &6" + Utils.formatNumber(raceObject.getBet()) + " &eCoins &7on " + raceObject.getLevel().getFormattedTitle()));
            else
                Bukkit.broadcastMessage(Utils.translate("&4" + winner.getDisplayName() + " &7has beaten &4" + loser.getDisplayName()
                                        + " &7in a race on " + raceObject.getLevel().getFormattedTitle()));

            // give winner money and take from loser if betted on race
            if (raceObject.hasBet())
                statsManager.addCoins(winnerStats, (raceObject.getBet() * 2));

            // check if winner is player 1, then teleport accordingly, otherwise they are player 2
            if (raceObject.isPlayer1(winner)) {
                winner.teleport(raceObject.getOriginalPlayer1Loc());
                loser.teleport(raceObject.getOriginalPlayer2Loc());
            } else {
                loser.teleport(raceObject.getOriginalPlayer1Loc());
                winner.teleport(raceObject.getOriginalPlayer2Loc());
            }

            // send title to winner and loser
            String titleString = Utils.translate("&c" + winner.getDisplayName() + " &7has won the Race!");
            TitleAPI.sendTitle(winner, 10, 60, 10, titleString, "");
            TitleAPI.sendTitle(loser, 10, 60, 10, titleString, "");

            // update winner wins
            winnerStats.endedRace();
            winnerStats.disableLevelStartTime();
            winnerStats.setRaceWins(winnerStats.getRaceWins() + 1);
            RaceDB.updateRaceWins(winnerStats.getUUID(), winnerStats.getRaceWins());

            // set winner race win rate
            if (winnerStats.getRaceLosses() > 0)
                winnerStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) winnerStats.getRaceWins() / winnerStats.getRaceLosses())));
            else
                winnerStats.setRaceWinRate(winnerStats.getRaceWins());

            // update loser losses
            loserStats.endedRace();
            loserStats.disableLevelStartTime();
            loserStats.setRaceLosses(loserStats.getRaceLosses() + 1);
            RaceDB.updateRaceLosses(loserStats.getUUID(), loserStats.getRaceLosses());

            // set loser race win rate (will be > 0, so no need to check)
            loserStats.setRaceWinRate(Float.parseFloat(Utils.formatDecimal((double) loserStats.getRaceWins() / loserStats.getRaceLosses())));

            // load leaderboard if they will have a lb position
            if (scoreWillBeLB(winnerStats.getRaceWins()) ||
                raceLeaderboard.size() < Parkour.getSettingsManager().max_race_leaderboard_size) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        loadLeaderboard();
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }

            ProtectedRegion winnerRegion = WorldGuard.getRegion(winner.getLocation());
            ProtectedRegion loserRegion = WorldGuard.getRegion(loser.getLocation());
            if (winnerRegion != null)
            {
                Level winnerLevel = Parkour.getLevelManager().get(winnerRegion.getId());
                winnerStats.setLevel(winnerLevel);

                // if elytra level, give elytra
                if (winnerLevel != null && winnerLevel.isElytra())
                    Parkour.getStatsManager().toggleOnElytra(winnerStats);
            }

            if (loserRegion != null)
            {
                Level loserLevel = Parkour.getLevelManager().get(loserRegion.getId());
                loserStats.setLevel(loserLevel);

                // if elytra level, give elytra
                if (loserLevel != null && loserLevel.isElytra())
                    Parkour.getStatsManager().toggleOnElytra(loserStats);
            }

            // remove from list
            runningRaceList.remove(raceObject);

            Parkour.getStatsManager().runGGTimer();
        }
    }

    public boolean scoreWillBeLB(int score)
    {
        int lowestWins = 0;
        // gets lowest score
        for (RaceLBPosition raceLBPosition : raceLeaderboard.values())
            if (lowestWins == 0 || raceLBPosition.getWins() < lowestWins)
                lowestWins = raceLBPosition.getWins();

        return lowestWins <= score;
    }

    public Race get(Player player)
    {
        for (Race race : runningRaceList)
            if (race.getPlayer1().equals(player) || race.getPlayer2().equals(player))
                return race;

        return null;
    }

    /*
        Race Requests Section
     */
    public void sendRequest(PlayerStats player1, PlayerStats player2, boolean randomLevel, RaceLevel selectedLevel, boolean bet, double betAmount)
    {
        List<RaceLevel> temporaryLevelList;
        if (randomLevel)
        {
            temporaryLevelList = getNotInUseRaceLevels();

            // if there are no levels available
            if (temporaryLevelList.isEmpty())
            {
                player1.getPlayer().sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                player2.getPlayer().sendMessage(Utils.translate("&cNo maps available for use, try again later"));
                return;
            }
            else
            {
                // picks random map
                Random ran = new Random();
                selectedLevel = temporaryLevelList.get(ran.nextInt(temporaryLevelList.size()));
            }
        }

        if (selectedLevel == null)
        {
            player1.getPlayer().sendMessage(Utils.translate("&cInvalid level? Try again"));
            return;
        }

        // otherwise, put them in and ask them to confirm within 5 seconds
        String senderString;
        String opponentString;

        if (bet) {
            opponentString = Utils.translate(
                    "&4" + player1.getPlayer().getName() + " &7has sent you a race request with bet amount &6" + Utils.formatNumber(betAmount) + " &eCoins&7! &a&nClick here to accept the race&r"
            );
            senderString = Utils.translate("&7You sent &4" + player2.getPlayer().getName() + " &7a race request with bet amount &6" + Utils.formatNumber(betAmount) + " &eCoins");
        } else {
            opponentString = Utils.translate("&4" + player1.getPlayer().getName() + " &7has sent you a race request&7! &a&nClick here to accept the race&r");
            senderString = Utils.translate("&7You sent &4" + player2.getPlayer().getName() + " &7a race request");
        }

        if (!randomLevel) {
            senderString += Utils.translate(" &7on &c" + selectedLevel.getFormattedTitle());
            opponentString += Utils.translate(" &7on &c" + selectedLevel.getFormattedTitle());
        }

        TextComponent opponentComponent = new TextComponent(TextComponent.fromLegacyText(opponentString));
        opponentComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&aClick to accept!"))));
        opponentComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/race accept " + player1.getPlayer().getName()));

        // send made messages
        player1.getPlayer().sendMessage(senderString);
        player2.getPlayer().spigot().sendMessage(opponentComponent); // send clickable

        RaceRequest raceRequest = new RaceRequest(player1, player2);

        raceRequest.setSelectedLevel(selectedLevel);

        // set bet if doing bet
        if (bet)
            raceRequest.setBet(betAmount);

        raceRequests.add(raceRequest);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (getRequest(player1.getPlayer(), player2.getPlayer()) != null) {
                    removeRequest(raceRequest);
                    player1.getPlayer().sendMessage(Utils.translate("&4" + player2.getPlayer().getName() + " &cdid not accept your race request in time"));
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 30);
    }

    public void acceptRequest(PlayerStats player1, PlayerStats player2) {

        RaceRequest raceRequest = getRequest(player1.getPlayer(), player2.getPlayer());

        // request exists
        if (raceRequest != null) {

            boolean doingBet = raceRequest.hasBet();
            double betAmount = raceRequest.getBet();

            if (player1.isSpectating()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
                removeRequest(raceRequest);
                return;
            }

            if (player2.isSpectating()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while they are in spectator"));
                removeRequest(raceRequest);
                return;
            }

            if (player1.inPracticeMode()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                removeRequest(raceRequest);
                return;
            }

            if (player2.inPracticeMode()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot do this while they are in practice mode"));
                removeRequest(raceRequest);
                return;
            }

            // if accepting race while in race
            if (player1.inRace()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone else while in a race"));
                removeRequest(raceRequest);
                return;
            }

            // if accepting race while in race
            if (player2.inRace()) {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone else while they are in a race"));
                removeRequest(raceRequest);
                return;
            }

            // if other player is in an elytra level and not on the ground, do not continue
            if (player2.inLevel() && player2.getLevel().isElytra() && !player2.getPlayer().isOnGround())
            {
                player1.getPlayer().sendMessage(Utils.translate("&cYou cannot race someone when " + player2.getName() + " is not on the ground in an elytra level"));
                removeRequest(raceRequest);
                return;
            }

            // make sure they still have enough money for the bet
            double accepterBalance = player1.getCoins();
            double senderBalance = player2.getCoins();
            if (accepterBalance < betAmount) {
                player1.getPlayer().sendMessage(Utils.translate("&7You do not have enough money for this bet!" +
                        " Your Balance &6" + Utils.formatNumber(senderBalance) + " &eCoins"));
                removeRequest(raceRequest);
                return;
            }

            if (senderBalance < betAmount) {
                player1.getPlayer().sendMessage(Utils.translate("&c" + player2.getName() + " &7does not have enough to do this bet" +
                        " - &cTheir Balance &6" + Utils.formatNumber(senderBalance) + " &eCoins"));
                removeRequest(raceRequest);
                return;
            }

            // if in elytra and not on the ground, dont send
            if (player1.inLevel() && player1.getLevel().isElytra())
                Parkour.getStatsManager().toggleOffElytra(player1);

            // if in elytra and not on the ground, dont send
            if (player2.inLevel() && player2.getLevel().isElytra())
                Parkour.getStatsManager().toggleOffElytra(player2);

            // otherwise do race and disable any current time on levels
            player1.disableLevelStartTime();
            player2.disableLevelStartTime();

            RaceLevel chosenLevel = null;
            if (!raceRequest.randomLevel())
                chosenLevel = raceRequest.getSelectedLevel();

            startRace(player1, player2, chosenLevel, doingBet, betAmount);
            removeRequest(raceRequest);
        } else {
            player1.getPlayer().sendMessage(Utils.translate("&cYou do not have a request from &4" + player2.getName()));
        }
    }

    public RaceRequest getRequest(Player player1, Player player2) {
        for (RaceRequest raceRequest : raceRequests) {
            String requestPlayer1Name = raceRequest.getPlayer1().getName();
            String requestPlayer2Name = raceRequest.getPlayer2().getName();

            if ((requestPlayer1Name.equalsIgnoreCase(player1.getName()) &&
                requestPlayer2Name.equalsIgnoreCase(player2.getName())) ||
               (requestPlayer1Name.equalsIgnoreCase(player2.getName()) &&
                requestPlayer2Name.equalsIgnoreCase(player1.getName())))
                return raceRequest;
        }
        return null;
    }

    public void removeRequest(RaceRequest raceRequest) {
        raceRequests.remove(raceRequest);
    }

    public List<RaceLevel> getNotInUseRaceLevels()
    {
        List<RaceLevel> notInUseRaceLevels = Parkour.getLevelManager().getRaceLevels();

        // get in use race levels, to then remove from total race levels
        for (Race race : getRaces())
            notInUseRaceLevels.remove(race.getLevel());

        return notInUseRaceLevels;
    }

    public Set<Race> getRaces() {
        return runningRaceList;
    }

    public void shutdown() {
        for (Race race : runningRaceList)
            forceEndRace(race, true);
    }

    /*
        Leaderboard Section
     */
    public void loadLeaderboard()
    {
        try
        {
            raceLeaderboard.clear();

            List<Map<String, String>> scoreResults = DatabaseQueries.getResults(
                    DatabaseManager.PLAYERS_TABLE,
                    "name, race_wins, race_losses",
                    "WHERE race_wins > 0 " +
                            "ORDER BY race_wins DESC " +
                            "LIMIT " + Parkour.getSettingsManager().max_race_leaderboard_size);

            int leaderboardPos = 1;

            for (Map<String, String> scoreResult : scoreResults)
            {
                int wins = Integer.parseInt(scoreResult.get("race_wins"));
                int losses = Integer.parseInt(scoreResult.get("race_losses"));

                // avoid divided by 0 error
                float winRate;
                if (losses > 0)
                    winRate = Float.parseFloat(Utils.formatDecimal((double) wins / losses));
                else
                    winRate = wins;

                raceLeaderboard.put(leaderboardPos,
                        new RaceLBPosition(
                                scoreResult.get("name"),
                                wins,
                                winRate
                        )
                );
                leaderboardPos++;
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public HashMap<Integer, RaceLBPosition> getLeaderboard() { return raceLeaderboard; }
}
