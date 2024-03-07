package com.renatusnetwork.parkour.gameplay.handlers;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.JackpotRewardEvent;
import com.renatusnetwork.parkour.api.LevelCompletionEvent;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.leaderboards.LevelLBPosition;
import com.renatusnetwork.parkour.data.levels.CompletionsDB;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.races.gamemode.RaceEndReason;
import com.renatusnetwork.parkour.data.races.gamemode.RacePlayer;
import com.renatusnetwork.parkour.data.races.gamemode.RaceRequest;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class LevelHandler
{

    public static void levelCompletion(PlayerStats playerStats, Level level)
    {
        Player player = playerStats.getPlayer();
        EventManager eventManager = Parkour.getEventManager();

        // if playerstats and level exists
        if (level != null)
        {
            // if they are not spectating
            if (!playerStats.isSpectating())
            {
                // if they are not previewing
                if (!playerStats.isPreviewingLevel())
                {
                    // if they do have the required level
                    if (level.playerHasRequiredLevels(playerStats))
                    {
                        // if does not have a practice location
                        if (!playerStats.inPracticeMode())
                        {
                            int playerLevelCompletions = playerStats.getLevelCompletionsCount(level);

                            if (!level.hasMaxCompletions() || playerLevelCompletions < level.getMaxCompletions())
                            {
                                // if level is not an event level, it is guaranteed normal completion
                                if (!level.isEventLevel())
                                    dolevelCompletion(playerStats, level);
                                    // otherwise, if there is an event running, end!
                                else if (eventManager.isEventRunning())
                                    eventManager.endEvent(player, false, false);
                                    // otherwise, they are clicking the sign when the event is not running
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot do this when an Event is not running!"));
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou've reached the maximum number of completions"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot complete a level in practice mode"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou do not have the required levels to complete this level"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot do this while previewing"));
            }
            else
                player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
        }
    }

    public static void dolevelCompletion(PlayerStats playerStats, Level level)
    {
        LevelCompletionEvent event = new LevelCompletionEvent(playerStats, level);
        Bukkit.getPluginManager().callEvent(event);
        Player player = playerStats.getPlayer();

        // continue if not cancelled
        if (!event.isCancelled())
        {
            LevelManager levelManager = Parkour.getLevelManager();
            LevelLBPosition oldRecord = level.getRecordCompletion();
            RacePlayer race = playerStats.getRace();
            boolean inRace = race != null;
            boolean runGG = inRace;

            levelManager.addTotalLevelCompletion();

            // if they have not completed this individual level, then add and add to level stats
            if (!playerStats.hasCompleted(level))
            {
                playerStats.addIndividualLevelsBeaten();
                level.addTotalUniqueCompletionsCount();
            }

            long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());
            String time = Utils.formatDecimal(elapsedTime / 1000d) + "s";

            // create level completion with appropriate timing
            LevelCompletion levelCompletion;
            if (playerStats.isLevelBeingTimed())
                levelCompletion = levelManager.createLevelCompletion(
                    playerStats.getUUID(), playerStats.getName(), level.getName(), System.currentTimeMillis(), elapsedTime
                );
            else
                levelCompletion = levelManager.createLevelCompletion(
                        playerStats.getUUID(), playerStats.getName(), level.getName(), System.currentTimeMillis(), -1
                );

            // disable when complete
            if (level.equals(Parkour.getLevelManager().getTutorialLevel()))
                playerStats.setTutorial(false);

            playerStats.addTotalLevelCompletions();

            boolean completedMastery = level.hasMastery() && playerStats.isAttemptingMastery();

            levelManager.addCompletion(playerStats, level, levelCompletion); // Update totalLevelCompletionsCount

            // run commands if there is any
            for (String commandString : level.getCommands())
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandString.replace("%player%", player.getName()));

            // Update player information
            playerStats.levelCompletion(levelCompletion);

            // add mastery
            if (completedMastery)
            {
                playerStats.addMasteryCompletion(level.getName());
                Parkour.getStatsManager().leftMastery(playerStats);
            }
            BankManager bankManager = Parkour.getBankManager();

            // give higher reward if prestiged
            int reward = event.getReward();

            // level booster
            if (playerStats.hasModifier(ModifierType.LEVEL_BOOSTER))
            {
                Booster booster = (Booster) playerStats.getModifier(ModifierType.LEVEL_BOOSTER);
                reward *= booster.getMultiplier();
            }

            // if mastery, boost it
            if (completedMastery)
                reward *= level.getMasteryMultiplier();
            // if featured, set reward!
            else if (level.isFeaturedLevel())
                reward *= Parkour.getSettingsManager().featured_level_reward_multiplier;
            // jackpot section
            else if (bankManager.isJackpotRunning() &&
                    bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                    !bankManager.getJackpot().hasCompleted(playerStats.getName()))
            {
                Jackpot jackpot = bankManager.getJackpot();

                JackpotRewardEvent jackpotEvent = new JackpotRewardEvent(playerStats, jackpot.getLevel(), jackpot.getBonus());
                Bukkit.getPluginManager().callEvent(jackpotEvent);

                if (!jackpotEvent.isCancelled())
                {
                    int bonus = jackpotEvent.getBonus();

                    // jackpot booster
                    if (playerStats.hasModifier(ModifierType.JACKPOT_BOOSTER))
                    {
                        Booster booster = (Booster) playerStats.getModifier(ModifierType.JACKPOT_BOOSTER);
                        bonus *= booster.getMultiplier();
                    }

                    // add coins and add to completed, as well as broadcast completion
                    jackpot.addCompleted(player.getName());
                    jackpot.broadcastCompletion(player);
                    reward += bonus;
                }
            }
            // prestige/cooldown section
            else
            {
                if (playerStats.hasPrestiges() && level.hasReward())
                    reward *= playerStats.getPrestigeMultiplier();

                // set cooldown modifier last!
                if (level.hasCooldown() && levelManager.inCooldownMap(playerStats.getName()))
                    reward *= levelManager.getLevelCooldown(playerStats.getName()).getModifier();
            }

            Parkour.getStatsManager().addCoins(playerStats, reward);

            String completionMessage = "";

            if (levelCompletion.wasTimed())
                completionMessage = "&7 in &a" + time;

            String completion = "&7Rewarded &6" + Utils.getCoinFormat(level.getReward(), reward) +
                    " &eCoins &7for " + level.getTitle() + completionMessage +
                    "&a (" + Utils.shortStyleNumber(playerStats.getLevelCompletionsCount(level)) +
                    ")";

            if (playerStats.inFailMode())
                completion += " &7in &6" + playerStats.getFails() + " fails";

            player.sendMessage(Utils.translate(completion));
            player.sendMessage(Utils.translate("&7Rate &e" + level.getTitle() + "&7 with &6/rate "
                    + ChatColor.stripColor(level.getFormattedTitle())));

            // get new PB
            LevelCompletion bestCompletion = playerStats.getQuickestCompletion(level);

            // if new pb, send message to player
            if (levelCompletion.wasTimed() && bestCompletion != null && bestCompletion.getCompletionTimeElapsedMillis() > elapsedTime)
            {
                String oldTimeString = Utils.formatDecimal(bestCompletion.getCompletionTimeElapsedSeconds()) + "s"; // need to format the long
                player.sendMessage(Utils.translate("&7You have broken your personal best &c(" + oldTimeString + ")&7 with &a" + time));
            }

            // broadcast completed if it is the featured level
            if (level.isFeaturedLevel())
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + player.getDisplayName() + " &7has completed the &6Featured Level &4" + level.getTitle()
                ));
            else if (completedMastery)
            {
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + playerStats.getDisplayName() + "&7 has completed the &5&lMastery &7for &2" + level.getTitle()
                ));
                runGG = true;
            }
            else if (level.isBroadcasting())
            {
                Bukkit.broadcastMessage(Utils.translate("&a" + player.getDisplayName() + "&7 completed " + level.getTitle()));
                runGG = true;
            }

            // used for playing sound!
            int beforeClanLevel = -1;

            if (playerStats.inClan())
            {
                beforeClanLevel = playerStats.getClan().getLevel();

                // do clan xp algorithm if they are in clan and level has higher reward than configurable amount
                if (level.getReward() > Parkour.getSettingsManager().clan_calc_level_reward_needed)
                    Parkour.getClansManager().doClanXPCalc(playerStats.getClan(), playerStats, reward);

                // do clan reward split algorithm if they are in clan and level has higher reward than configurable amount
                if (level.getReward() > Parkour.getSettingsManager().clan_split_reward_min_needed)
                {
                    // async for database querying
                    int finalReward = reward;

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            Parkour.getClansManager().doSplitClanReward(playerStats.getClan(), player, level, finalReward);
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
            }

            if (!playerStats.isGrinding())
                Parkour.getStatsManager().toggleOffElytra(playerStats);

            Parkour.getPluginLogger().info(playerStats.getName() + " beat " + ChatColor.stripColor(level.getFormattedTitle())); // log to console

            // reset cp and saves before teleport
            if (!inRace)
            {
                Parkour.getCheckpointManager().deleteCheckpoint(playerStats, level);
                Parkour.getSavesManager().removeSave(playerStats, level); // safety removal (likely will never actually execute)
            }

            // clear potion effects
            playerStats.clearPotionEffects();

            if (!inRace)
            {
                String titleMessage = Utils.translate("&7You beat " + level.getTitle());
                if (levelCompletion.wasTimed())
                    titleMessage += Utils.translate("&7 in &2" + time);

                String subTitleMessage = Utils.translate("&7Rate &e" + level.getTitle() + "&7 with &6/rate "
                        + ChatColor.stripColor(level.getFormattedTitle()));

                TitleAPI.sendTitle(
                        player, 10, 60, 10,
                        titleMessage,
                        subTitleMessage
                );
            }

            // play sound if they did not level up their clan
            if (!(beforeClanLevel > -1 && beforeClanLevel < playerStats.getClan().getLevel()))
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0f);

            Location locationTo = level.getCompletionLocation();

            if (inRace)
            {
                locationTo = race.getOriginalLocation();
                RacePlayer opponent = race.getOpponent();
                PlayerStats opponentStats = opponent.getPlayerStats();

                setLevelInfoOnTeleport(opponentStats, opponent.getOriginalLocation());
                opponentStats.disableLevelStartTime();
                opponentStats.teleport(opponent.getOriginalLocation());

                playerStats.endRace(race, RaceEndReason.WON);
            }
            else
            // If not rank up level or has a start location and is grinding, set to start loc
            if (!playerStats.isAttemptingMastery() && !playerStats.isAttemptingRankup() && level.getStartLocation() != Parkour.getLocationManager().get("spawn") && playerStats.isGrinding())
            {
                locationTo = level.getStartLocation();
                playerStats.resetFails(); // reset fails in grinding
            }

            // rank them up!
            if (level.isRankUpLevel() && playerStats.isAttemptingRankup())
            {
                Parkour.getRanksManager().doRankUp(player);
                Parkour.getStatsManager().leftRankup(playerStats);
            }

            // add cooldown
            levelManager.addLevelCooldown(playerStats.getName(), level);

            setLevelInfoOnTeleport(playerStats, locationTo);

            // teleport
            player.teleport(locationTo);

            LevelLBPosition recordCompletion = level.getRecordCompletion();

            boolean isRecord =
                    level.hasLeaderboard() &&
                    recordCompletion.getPlayerName().equalsIgnoreCase(levelCompletion.getName()) &&
                    recordCompletion.getTimeTaken() == levelCompletion.getCompletionTimeElapsedMillis();

            if (isRecord)
            {
                // update new #1 records
                playerStats.addRecord(level, levelCompletion.getCompletionTimeElapsedMillis());
                String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

                // update old record
                if (oldRecord != null)
                {
                    PlayerStats previousStats = Parkour.getStatsManager().getByName(oldRecord.getPlayerName());

                    if (previousStats != null && !playerStats.equals(previousStats))
                        previousStats.removeRecord(level);
                }
                else
                    brokenRecord = "&e✦ &d&lRECORD SET &e✦";

                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate(brokenRecord));
                Bukkit.broadcastMessage(Utils.translate("&d" + playerStats.getDisplayName() +
                        " &7has the new &8" + level.getTitle() +
                        " &7record with &a" + Utils.formatDecimal(levelCompletion.getCompletionTimeElapsedSeconds()) + "s"));
                Bukkit.broadcastMessage("");

                Utils.spawnFirework(level.getCompletionLocation(), Color.PURPLE, Color.FUCHSIA, true);

                if (playerStats.hasModifier(ModifierType.RECORD_BONUS))
                {
                    Bonus bonus = (Bonus) playerStats.getModifier(ModifierType.RECORD_BONUS);

                    // add coins
                    Parkour.getStatsManager().addCoins(playerStats, bonus.getBonus());
                    playerStats.getPlayer().sendMessage(Utils.translate("&7You got &6" + Utils.formatNumber(bonus.getBonus()) + " &eCoins &7for getting the record!"));
                }
                // do gg run if it wasnt a race completion (gg already runs)
                runGG = true;
            }

            // run gg in specific cases
            if (runGG)
                Parkour.getStatsManager().runGGTimer();

            CompletionsDB.insertCompletion(levelCompletion, completedMastery);
        }
    }

    // Respawn player if checkpoint isn't there
    public static void respawnPlayer(PlayerStats playerStats, Level level) {
        // make sure the water reset is toggled on
        if (level != null) {
            Location loc = level.getStartLocation();

            if (loc != null)
            {
                playerStats.getPlayer().teleport(loc);
                playerStats.addFail(); // used in multiple areas
            }
        }
    }

    public static void setLevelInfoOnTeleport(PlayerStats playerStats, Location location)
    {
        ProtectedRegion getToRegion = WorldGuard.getRegion(location);
        Player player = playerStats.getPlayer();
        playerStats.disableLevelStartTime();

        // if area they are teleporting to is empty
        // if not empty, make sure it is a level
        // if not a level (like spawn), reset level
        if (getToRegion == null)
            playerStats.resetLevel();
        else
        {
            Level newLevel = Parkour.getLevelManager().get(getToRegion.getId());

            if (newLevel != null)
            {
                // apply potion effects if any exist
                for (PotionEffect potionEffect : newLevel.getPotionEffects())
                    player.addPotionEffect(potionEffect);

                // if elytra level, give elytra
                if (newLevel.isElytra() && !playerStats.getLevel().isElytra())
                    Parkour.getStatsManager().toggleOnElytra(playerStats);

                playerStats.setLevel(newLevel);

                if (playerStats.hasCheckpoint(newLevel))
                    playerStats.setCurrentCheckpoint(playerStats.getCheckpoint(newLevel));
            }
            else
                playerStats.resetLevel();
        }
    }
}
