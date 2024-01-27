package com.renatusnetwork.parkour.gameplay.handlers;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.JackpotRewardEvent;
import com.renatusnetwork.parkour.api.LevelCompletionEvent;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.levels.CompletionsDB;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class LevelHandler {

    public static void levelCompletion(Player player, Level level)
    {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        EventManager eventManager = Parkour.getEventManager();

        // if playerstats and level exists
        if (level != null && playerStats != null && !playerStats.isSpectating())
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
                        // if it is a race completion, end it
                        if (!playerStats.inRace())
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
                            // if in race
                            Parkour.getRaceManager().endRace(player, false);
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
            LevelCompletion oldRecord = level.getRecordCompletion();

            levelManager.addTotalLevelCompletion();

            // if they have not completed this individual level, then add
            if (!playerStats.hasCompleted(level))
                playerStats.setIndividualLevelsBeaten(playerStats.getIndividualLevelsBeaten() + 1);

            long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());

            String time = (((double) elapsedTime) / 1000) + "s";

            LevelCompletion levelCompletion = levelManager.createLevelCompletion(
                    playerStats.getUUID(), playerStats.getName(), level.getName(), System.currentTimeMillis(), elapsedTime
            );

            // get new PB
            LevelCompletion bestCompletion = playerStats.getQuickestCompletion(level);
            boolean newPB = false;

            if (bestCompletion != null)
                newPB = bestCompletion.getCompletionTimeElapsedMillis() > elapsedTime; // if this completion will be a PB!

            // disable when complete
            if (level.getName().equalsIgnoreCase(Parkour.getLevelManager().getTutorialLevel().getName()))
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

            // used for playing sound!
            int beforeClanLevel = -1;
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
                completionMessage = "&7 in &a" + (elapsedTime / 1000f) + "s";

            String completion = "&7Rewarded &6" + Utils.getCoinFormat(level.getReward(), reward) +
                    " &eCoins &7for " + level.getTitle() + completionMessage +
                    "&a (" + Utils.shortStyleNumber(playerStats.getLevelCompletionsCount(level)) +
                    ")";

            if (playerStats.inFailMode())
                completion += " &7in &6" + playerStats.getFails() + " fails";

            player.sendMessage(Utils.translate(completion));
            player.sendMessage(Utils.translate("&7Rate &e" + level.getTitle() + "&7 with &6/rate "
                    + ChatColor.stripColor(level.getFormattedTitle())));

            // if new pb, send message to player
            if (newPB)
            {
                String oldTimeString = bestCompletion.getCompletionTimeElapsedSeconds() + "s"; // need to format the long

                player.sendMessage(Utils.translate("&7You have broken your personal best &c(" + oldTimeString + ")&7 with &a" + time));
            }

            // broadcast completed if it the featured level
            if (level.isFeaturedLevel())
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + player.getDisplayName() + " &7has completed the &6Featured Level &4" + level.getTitle()
                ));
            else if (completedMastery)
                Bukkit.broadcastMessage(Utils.translate(
                        "&c" + playerStats.getDisplayName() + "&7 has completed the &2" + level.getTitle() + "&7 (&5&lMastery&7)"
                ));
            else if (level.isBroadcasting())
                Bukkit.broadcastMessage(Utils.translate("&a" + player.getDisplayName() + "&7 has beaten " + level.getTitle()));

            if (playerStats.getClan() != null)
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
            Parkour.getCheckpointManager().deleteCheckpoint(playerStats, level);
            Parkour.getSavesManager().removeSave(playerStats, level); // safety removal (likely will never actually execute)

            // clear potion effects
            playerStats.clearPotionEffects();

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

            // play sound if they did not level up their clan
            if (!(beforeClanLevel > -1 && beforeClanLevel < playerStats.getClan().getLevel()))
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0f);

            Location locationTo = level.getCompletionLocation();

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

            ProtectedRegion getToRegion = WorldGuard.getRegion(locationTo);

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
                    playerStats.setLevel(newLevel);

                    // apply potion effects if any exist
                    for (PotionEffect potionEffect : newLevel.getPotionEffects())
                        player.addPotionEffect(potionEffect);
                }
                else
                    playerStats.resetLevel();
            }

            // teleport
            player.teleport(locationTo);
            playerStats.disableLevelStartTime();

            boolean isRecord = level.hasLeaderboard() && level.getRecordCompletion().equals(levelCompletion);
            if (isRecord)
            {
                // update new #1 records
                playerStats.addRecord(level, levelCompletion);
                String brokenRecord = "&e✦ &d&lRECORD BROKEN &e✦";

                // update old record
                if (oldRecord != null)
                {
                    PlayerStats previousStats = Parkour.getStatsManager().get(oldRecord.getUUID());

                    if (previousStats != null)
                        previousStats.removeRecord(level);
                }
                else
                    brokenRecord = "&e✦ &d&lRECORD SET &e✦";

                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate(brokenRecord));
                Bukkit.broadcastMessage(Utils.translate("&d" + playerStats.getDisplayName() +
                        " &7has the new &8" + level.getTitle() +
                        " &7record with &a" + levelCompletion.getCompletionTimeElapsedSeconds() + "s"));
                Bukkit.broadcastMessage("");

                Utils.spawnFirework(level.getCompletionLocation(), Color.PURPLE, Color.FUCHSIA, true);

                if (playerStats.hasModifier(ModifierType.RECORD_BONUS))
                {
                    Bonus bonus = (Bonus) playerStats.getModifier(ModifierType.RECORD_BONUS);

                    // add coins
                    Parkour.getStatsManager().addCoins(playerStats, bonus.getBonus());
                    playerStats.getPlayer().sendMessage(Utils.translate("&7You got &6" + Utils.formatNumber(bonus.getBonus()) + " &eCoins &7for getting the record!"));
                }
                // do gg run
                Parkour.getStatsManager().runGGTimer();
            }

            // small microoptimization running it in async. if it is a record, it will be updated when we do the modifications
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    CompletionsDB.insertCompletion(levelCompletion, completedMastery);
                }
            }.runTaskAsynchronously(Parkour.getPlugin());
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
}
