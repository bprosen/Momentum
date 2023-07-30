package com.renatusnetwork.parkour.gameplay.handlers;

import com.connorlinfoot.titleapi.TitleAPI;
import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.api.LevelCompletionEvent;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCooldown;
import com.renatusnetwork.parkour.data.levels.LevelManager;
import com.renatusnetwork.parkour.data.stats.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

public class LevelHandler {

    public static void levelCompletion(Player player, String levelName) {

        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        EventManager eventManager = Parkour.getEventManager();
        Level level = Parkour.getLevelManager().get(levelName);

        // if playerstats and level exists
        if (playerStats != null && !playerStats.isSpectating() && level != null) {
            // if they do have the required level
            if (level.hasRequiredLevels(playerStats)) {
                // if does not have a practice location
                if (!playerStats.inPracticeMode()) {

                    int playerLevelCompletions = playerStats.getLevelCompletionsCount(levelName);

                    if (level.getMaxCompletions() == -1 || playerLevelCompletions < level.getMaxCompletions()) {
                        // if it is a race completion, end it
                        if (!playerStats.inRace()) {
                            // if level is not a rankup level, continue
                            if (!level.isRankUpLevel()) {
                                // if level is not an event level, it is guaranteed normal completion
                                if (!level.isEventLevel())
                                    dolevelCompletion(playerStats, player, level, levelName, false, false);
                                // otherwise, if there is an event running, end!
                                else if (eventManager.isEventRunning())
                                    eventManager.endEvent(player, false, false);
                                // otherwise, they are clicking the sign when the event is not running
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot do this when an Event is not running!"));

                            } else
                                dolevelCompletion(playerStats, player, level, levelName, true, false);
                        } else {
                            // if in race
                            Parkour.getRaceManager().endRace(player);
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou've reached the maximum number of completions"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot complete a level in practice mode"));
                }
            } else {
                    player.sendMessage(Utils.translate("&cYou do not have the required levels to complete this level"));
            }
        }
    }

    public static void dolevelCompletion(PlayerStats playerStats, Player player, Level level, String levelName, boolean rankUpLevel, boolean forcedCompletion) {

        LevelCompletionEvent event = new LevelCompletionEvent(playerStats, level);
        Bukkit.getPluginManager().callEvent(event);

        // continue if not cancelled
        if (!event.isCancelled())
        {
            LevelManager levelManager = Parkour.getLevelManager();

            // if they have not completed this individual level, then add
            if (playerStats.getLevelCompletionsCount(levelName) < 1)
                playerStats.setIndividualLevelsBeaten(playerStats.getIndividualLevelsBeaten() + 1);

            Long elapsedTime = (System.currentTimeMillis() - playerStats.getLevelStartTime());
            String time = (((double) elapsedTime) / 1000) + "s";
            LevelCompletion levelCompletion = new LevelCompletion(
                    System.currentTimeMillis(),
                    elapsedTime
            );

            // disable when complete
            if (level.getName().equalsIgnoreCase(Parkour.getLevelManager().getTutorialLevel().getName()))
                playerStats.setTutorial(false);

            levelCompletion.setPlayerName(player.getName());
            playerStats.setTotalLevelCompletions(playerStats.getTotalLevelCompletions() + 1);

            // small microoptimization running it in async
            new BukkitRunnable() {
                @Override
                public void run() {
                    StatsDB.insertCompletion(playerStats, level, levelCompletion);
                }
            }.runTaskAsynchronously(Parkour.getPlugin());

            levelManager.addTotalLevelCompletion();

            level.addCompletion(player.getName(), levelCompletion); // Update totalLevelCompletionsCount

            // run commands if there is any
            if (level.hasCommands()) {
                for (String commandString : level.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            commandString.replace("%player%", player.getName()));
                }
            }

            // Update player information
            playerStats.levelCompletion(levelName, levelCompletion);

            // used for playing sound!
            int beforeClanLevel = -1;

            if (rankUpLevel)
                Parkour.getRanksManager().doRankUp(player);
            else
                // only broadcast and give xp/coins if it is not a forced completion
                if (!forcedCompletion) {

                    BankManager bankManager = Parkour.getBankManager();

                    // give higher reward if prestiged
                    int prestiges = playerStats.getPrestiges();
                    int reward = event.getReward();
                    boolean isJackpotReward = false;

                    // if featured, set reward!
                    if (level.isFeaturedLevel())
                        reward *= Parkour.getSettingsManager().featured_level_reward_multiplier;
                    // jackpot section
                    else if (bankManager.isJackpotRunning() &&
                            bankManager.getJackpot().getLevelName().equalsIgnoreCase(level.getName()) &&
                            !bankManager.getJackpot().hasCompleted(playerStats.getPlayerName()))
                    {
                        Jackpot jackpot = bankManager.getJackpot();
                        isJackpotReward = true;

                        // add coins and add to completed, as well as broadcast completion
                        Parkour.getStatsManager().addCoins(playerStats, jackpot.getBonus());
                        jackpot.addCompleted(player.getName());
                        jackpot.broadcastCompletion(player);
                    }
                    // modifier section
                    else
                    {
                        if (prestiges > 0 && level.getReward() > 0)
                            reward *= playerStats.getPrestigeMultiplier();

                        // set cooldown modifier last!
                        if (level.hasCooldown() && levelManager.inCooldownMap(playerStats.getPlayerName()))
                            reward *= levelManager.getLevelCooldown(playerStats.getPlayerName()).getModifier();
                    }
                    Parkour.getStatsManager().addCoins(playerStats, reward);

                    String messageFormatted = level.getFormattedMessage(playerStats, isJackpotReward);
                    if (elapsedTime > 0L && elapsedTime < 8388607L)
                        messageFormatted = messageFormatted.replace("%time%", time);
                    else
                        messageFormatted = messageFormatted.replace("%time%", "-");

                    player.sendMessage(messageFormatted);
                    player.sendMessage(Utils.translate("&7Rate &e" + level.getFormattedTitle() + " &7with &6/rate "
                            + ChatColor.stripColor(level.getFormattedTitle())));

                    // broadcast completed if it the featured level
                    if (level.isFeaturedLevel()) {
                        Bukkit.broadcastMessage(Utils.translate(
                                "&c" + player.getDisplayName() + " &7has completed the &6Featured Level &4" + level.getFormattedTitle()
                        ));
                    } else if (level.getBroadcastCompletion()) {
                        String broadcastMessage = Utils.translate(Parkour.getSettingsManager().levels_message_broadcast);

                        broadcastMessage = broadcastMessage.replace("%player%", player.getDisplayName());
                        broadcastMessage = broadcastMessage.replace("%title%", level.getFormattedTitle());

                        Bukkit.broadcastMessage(broadcastMessage);
                    }

                    if (playerStats.getClan() != null) {
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

                    Parkour.getPluginLogger().info(playerStats.getPlayerName() + " beat " + ChatColor.stripColor(level.getFormattedTitle())); // log to console

                    // reset cp and saves before teleport
                    Parkour.getCheckpointManager().deleteCheckpoint(playerStats, level);
                    Parkour.getSavesManager().removeSave(playerStats, level); // safety removal (likely will never actually execute)

                    // clear potion effects
                    playerStats.clearPotionEffects();

                    // send title and sound if not rankup level
                    if (!rankUpLevel) {
                        String titleMessage = Utils.translate("&7You beat " + level.getFormattedTitle());
                        if (elapsedTime > 0L && elapsedTime < 8388607L)
                            titleMessage += Utils.translate("&7 in &2" + time);

                        String subTitleMessage = Utils.translate("&7Rate &e" + level.getFormattedTitle() + " &7with &6/rate "
                                + ChatColor.stripColor(level.getFormattedTitle()));

                        TitleAPI.sendTitle(
                                player, 10, 60, 10,
                                titleMessage,
                                subTitleMessage
                        );

                        // play sound if they did not level up their clan
                        if (!(beforeClanLevel > -1 && beforeClanLevel < playerStats.getClan().getLevel()))
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0f);
                    }

                    Location locationTo = level.getRespawnLocation();

                    // If not rank up level or has a start location and is grinding, set to start loc
                    if (!rankUpLevel && level.getStartLocation() != Parkour.getLocationManager().get("spawn") && playerStats.isGrinding())
                    {
                        locationTo = level.getStartLocation();
                        playerStats.resetFails(); // reset fails in grinding
                    }

                    ProtectedRegion getToRegion = WorldGuard.getRegion(locationTo);
                    Level newLevel = Parkour.getLevelManager().get(getToRegion.getId());

                    // if area they are teleporting to is empty
                    // if not empty, make sure it is a level
                    // if not a level (like spawn), reset level
                    if (getToRegion == null)
                        playerStats.resetLevel();
                    else if (newLevel != null) {
                        playerStats.setLevel(newLevel);

                        // apply potion effects if any exist
                        if (!newLevel.getPotionEffects().isEmpty())
                            for (PotionEffect potionEffect : newLevel.getPotionEffects())
                                player.addPotionEffect(potionEffect);
                    } else
                        playerStats.resetLevel();

                    // teleport
                    player.teleport(locationTo);
                    playerStats.disableLevelStartTime();

                } else {
                    player.sendMessage(Utils.translate("&7You have been given a completion for &c" + level.getFormattedTitle()));

                    if (!level.hasRequiredLevels(playerStats) && !level.getRequiredLevels().isEmpty()) {

                        for (String requiredLevelName : level.getRequiredLevels()) {

                            if (playerStats.getLevelCompletionsCount(requiredLevelName) < 1) {
                                Level requiredLevel = Parkour.getLevelManager().get(requiredLevelName);

                                dolevelCompletion(playerStats, player, requiredLevel, requiredLevelName, false, true);
                            }
                        }
                    }
                }
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