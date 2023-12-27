package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.levels.*;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.gameplay.handlers.LevelHandler;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class LevelCMD implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender.isOp() || !(sender instanceof Player)) {

            LevelManager levelManager = Parkour.getLevelManager();

            if (a.length == 1 && a[0].equalsIgnoreCase("show")) { // subcommand: show
                if (sender instanceof Player)
                {

                    Player player = (Player) sender;
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);

                    if (playerStats.inLevel())
                        sender.sendMessage(Utils.translate("&7You are in &c" + playerStats.getLevel().getFormattedTitle()));
                    else
                        sender.sendMessage(Utils.translate("&7You are not in a level"));
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cConsole cannot run this"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("create"))
            {
                String levelName = a[1].toLowerCase();

                if (levelName.contains("'"))
                    sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                else if (levelManager.exists(levelName))
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7already exists"));
                else
                {
                    levelManager.create(levelName);
                    sender.sendMessage(Utils.translate("&7Created level &2" + levelName));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("delete"))
            { // subcommand: delete
                String levelName = a[1];

                if (!levelManager.exists(levelName))
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                else
                {
                    levelManager.remove(levelName);
                    sender.sendMessage(Utils.translate("&7Deleted level &2" + levelName));
                }
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("load"))
            {
                levelManager.load();
                sender.sendMessage(Utils.translate("&7Loaded &2" + levelManager.numLevels() + " &7levels"));

                // load total completions and leaderboards in async
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        CompletionsDB.loadTotalCompletions();
                        CompletionsDB.loadLeaderboards();
                        levelManager.loadGlobalLevelCompletions();
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());

            }
            else if (a.length > 2 && a[0].equalsIgnoreCase("title"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String title = String.join(" ", split);

                    levelManager.setTitle(level, title);

                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's title to &2" + title));
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("reward"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int reward = Integer.parseInt(a[2]);

                        levelManager.setReward(level, reward);
                        sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's reward to &6" + reward));
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
                    }
                } else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("startloc"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (sender instanceof Player)
                    {
                        String spawnPositionName = SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", levelName);
                        levelManager.setStartLocation(level, spawnPositionName, ((Player) sender).getLocation());

                        sender.sendMessage(Utils.translate("&7Location saved as &2" + spawnPositionName + " &7for &2" + levelName));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cConsole cannot run this"));
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("completionloc"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (sender instanceof Player)
                    {
                        String spawnPositionName = SettingsManager.LEVEL_COMPLETION_FORMAT.replace("%level%", levelName);
                        levelManager.setCompletionLocation(level, spawnPositionName, ((Player) sender).getLocation());

                        sender.sendMessage(Utils.translate("&7Location saved as &2" + spawnPositionName + " &7for &2" + levelName));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cConsole cannot do this"));
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length > 1 && a[0].equalsIgnoreCase("maxcompletions"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (a.length == 3)
                    {
                        if (Utils.isInteger(a[2]))
                        {
                            int maxCompletions = Integer.parseInt(a[2]);

                            levelManager.setMaxCompletions(level, maxCompletions);
                            sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's max completions to &2" + maxCompletions));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
                    }
                    else if (a.length == 2)
                        sender.sendMessage(Utils.translate("&2" + levelName + "&7's current max completions: &2" + level.getMaxCompletions()));
                    else
                        sendHelp(sender);
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + "&7 does not exist"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("broadcast"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    levelManager.toggleBroadcastCompletion(level);
                    sender.sendMessage(Utils.translate("&7Broadcast for &2" + levelName + " &7 set to " + level.isBroadcasting()));
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length == 3 && (a[0].equalsIgnoreCase("addrequired") || a[0].equalsIgnoreCase("removerequired")))
            {
                String levelName = a[1].toLowerCase();
                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null)
                {
                    String requiredLevelName = a[2].toLowerCase();
                    Level requiredLevel = levelManager.get(requiredLevelName);

                    if (requiredLevel != null)
                    {
                        if (level.isRequiredLevel(requiredLevelName))
                        {
                            levelManager.removeRequiredLevel(level, requiredLevelName);

                            sender.sendMessage(Utils.translate(
                                    "&7Removed &c" + requiredLevelName + " &7from &2" + levelName + "&7's required levels"
                            ));
                        }
                        else
                        {
                            levelManager.addRequiredLevel(level, requiredLevelName);

                            sender.sendMessage(Utils.translate(
                                    "&7Added &2" + requiredLevelName + " &7to &2" + levelName + "&7's required levels"
                            ));
                        }
                    }
                    else
                        sender.sendMessage(Utils.translate("&7Level &2" + requiredLevelName + " &7does not exist"));
                }
                else
                    sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removetime"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (Utils.isInteger(a[2]))
                {
                    int row = Integer.parseInt(a[2]);

                    if (level != null)
                    {
                        List<LevelCompletion> leaderboard = level.getLeaderboard();

                        if (row <= leaderboard.size() && row > 0)
                        {
                            int name = row - 1;

                            LevelCompletion oldHolder = leaderboard.get(name);

                            // run it in async!
                            new BukkitRunnable()
                            {
                                @Override
                                public void run()
                                {
                                    CompletionsDB.removeCompletion(oldHolder, false);

                                    level.setTotalCompletionsCount(level.getTotalCompletionsCount() - 1);

                                    CompletionsDB.loadLeaderboard(level);

                                    // if deleting record
                                    if (name == 0)
                                    {
                                        List<LevelCompletion> newLeaderbaord = level.getLeaderboard();

                                        if (!newLeaderbaord.isEmpty())
                                        {
                                            // if the new leaderboard is no longer empty, add record for new holder
                                            LevelCompletion newHolder = leaderboard.get(0);

                                            // if it is a diff person, need to update their in game stats
                                            if (!oldHolder.getUUID().equalsIgnoreCase(newHolder.getUUID()))
                                            {
                                                PlayerStats oldHolderStats = Parkour.getStatsManager().get(oldHolder.getUUID());
                                                PlayerStats newHolderStats = Parkour.getStatsManager().get(newHolder.getUUID());

                                                if (oldHolderStats != null)
                                                    oldHolderStats.removeRecord();

                                                if (newHolderStats != null)
                                                    newHolderStats.addRecord();
                                            }
                                            // update both in db
                                            CompletionsDB.updateRecord(oldHolder);
                                            CompletionsDB.updateRecord(newHolder);
                                        }
                                    }

                                    sender.sendMessage(Utils.translate(
                                            "&4" + oldHolder.getPlayerName() + "'s &ctime has been removed succesfully from &4" + levelName
                                    ));
                                }
                            }.runTaskAsynchronously(Parkour.getPlugin());
                        }
                        else
                            sender.sendMessage(Utils.translate("&cYou are entering an integer above 9"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&7No level named '&c" + levelName + "&7' exists"));
                }
                else
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7is not an integer!"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("raceset"))
            {

                if (sender instanceof Player)
                {
                    Player player = (Player) sender;

                    if (a[2].equalsIgnoreCase("1") || a[2].equalsIgnoreCase("2"))
                    {
                        String levelName = a[1].toLowerCase();

                        if (levelManager.exists(levelName))
                        {
                            String locationName = SettingsManager.RACE_LEVEL_SPAWN_FORMAT
                                    .replace("%level%", levelName)
                                    .replace("%spawn%", a[2]);

                            Parkour.getLocationManager().set(locationName, player.getLocation());
                            player.sendMessage(Utils.translate("&cYou set the location for player &4" + a[2] + " &con level &4" + levelName));
                        }
                        else
                            sender.sendMessage(Utils.translate("&7The level &c" + levelName + " &7does not exist"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cArgument must be 1 or 2"));
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot run this"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("forcecompletion"))
            {
                Player target = Bukkit.getPlayer(a[1]);
                String levelName = a[2].toLowerCase();

                if (target != null)
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(target);

                    if (levelManager.exists(levelName))
                    {

                        Level level = levelManager.get(levelName);

                        LevelHandler.dolevelCompletion(playerStats, target, level, levelName, true);
                        sender.sendMessage(Utils.translate("&7You forced a &c" + level.getFormattedTitle() + " &7Completion for &a" + target.getName()));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cLevel &4" + levelName + " &cdoes not exist"));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));

            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("respawny"))
            {
                String levelName = a[1].toLowerCase();

                if (Utils.isInteger(a[2]))
                {
                    Level level = levelManager.get(levelName);

                    // get new y
                    int newY = Integer.parseInt(a[2]);

                    if (level != null)
                    {
                        levelManager.setRespawnY(level, newY);
                        sender.sendMessage(Utils.translate(
                                "&7You set &c" + level.getFormattedTitle() + "&7's respawn y to &c" + newY
                        ));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cLevel &4" + levelName + " &cdoes not exist"));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("addrating"))
            {
                if (sender instanceof Player)
                {
                    Player player = (Player) sender;

                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null)
                    {
                        if (!level.hasRated(player.getName()))
                        {
                            if (Utils.isInteger(a[2]))
                            {
                                int rating = Integer.parseInt(a[2]);
                                int minRating = Parkour.getSettingsManager().min_rating;
                                int maxRating = Parkour.getSettingsManager().max_rating;

                                if (rating >= minRating && rating <= maxRating)
                                {
                                    levelManager.addRating(player, level, rating);
                                    sender.sendMessage(Utils.translate(
                                            "&7You added a rating of &4" + rating + " &7to &cLevel &a" + level.getFormattedTitle()
                                    ));
                                }
                                else
                                {
                                    sender.sendMessage(Utils.translate("&cRating has to be between &4" + minRating + "-" + maxRating));
                                }
                            }
                            else
                                sender.sendMessage(Utils.translate("&c" + a[2] + " is not an integer"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&cYou have already rated &4" + level.getFormattedTitle()));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot do this"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removerating"))
            {
                String levelName = a[1].toLowerCase();
                String targetName = a[2];
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (level.hasRated(targetName))
                    {
                        levelManager.removeRating(targetName, level);
                        sender.sendMessage(Utils.translate("&cYou removed &4" + targetName + "&7's from &c" + level.getFormattedTitle()));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + targetName + " &chas not rated &4" + level.getFormattedTitle()));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("hasrated"))
            {
                String levelName = a[1].toLowerCase();
                String playerName = a[2];
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    int rating = level.getRating(playerName);

                    if (rating > -1)
                        sender.sendMessage(Utils.translate(
                                "&c" + playerName + " &7has rated &c" + level.getFormattedTitle() + " &7 &6" + rating
                        ));
                    else
                        sender.sendMessage(Utils.translate(
                                "&c" + playerName + " &7has not rated &c" + level.getFormattedTitle()
                        ));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
            }
            else if (a.length >= 2 && a[0].equalsIgnoreCase("listratings"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (a.length == 2)
                    {
                        if (level.getRatingsCount() > 0)
                        {
                            sender.sendMessage(Utils.translate("&2" + level.getFormattedTitle() + "&7's Ratings"));

                            // loop through list
                            for (int i = Parkour.getSettingsManager().max_rating; i >= Parkour.getSettingsManager().min_rating; i--)
                            {
                                String msg = " &2" + i + " &7-";

                                List<String> names = level.getUsersWhoRated(i);

                                for (String name : names)
                                    msg += " &a" + name;

                                sender.sendMessage(Utils.translate(msg));
                            }
                            sender.sendMessage(Utils.translate("&a" + level.getRatingsCount() + " &7ratings"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&cNobody has rated &4" + level.getFormattedTitle()));
                    // if they put the optional specification arg
                    }
                    else if (a.length == 3)
                    {
                        if (Utils.isInteger(a[2]))
                        {
                            int rating = Integer.parseInt(a[2]);
                            // make sure it is between 0 and 5
                            if (rating >= Parkour.getSettingsManager().min_rating && rating <= Parkour.getSettingsManager().max_rating)
                            {

                                List<String> names = level.getUsersWhoRated(rating);

                                // if it is not empty
                                if (!names.isEmpty())
                                {
                                    sender.sendMessage(Utils.translate(
                                            "&7Players who rated &2" + level.getFormattedTitle() + " &7a &a" + rating
                                    ));

                                    String msg = " &2" + rating + " &7-";

                                    for (String playerName : names)
                                        msg += " &a" + playerName;

                                    sender.sendMessage(Utils.translate(msg));
                                    sender.sendMessage(Utils.translate("&a" + names.size() + " &7ratings"));
                                }
                                else
                                    sender.sendMessage(Utils.translate("&cNobody has rated this level a " + rating));
                            }
                            else
                                sender.sendMessage(Utils.translate("&cYour rating has to be anywhere from 0 to 5!"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an Integer"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cInvalid arguments"));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("togglewater"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    levelManager.toggleLiquidReset(level);

                    String offOrOn = "&cOff";
                    if (level.doesLiquidResetPlayer())
                        offOrOn = "&aOn";

                    sender.sendMessage(Utils.translate(
                            "&7You toggled " + offOrOn + " &7liquid resetting players for level &c" + level.getFormattedTitle()
                    ));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("rename"))
            {
                String levelName = a[1].toLowerCase();
                String newLevelName = a[2].toLowerCase();

                Level level = levelManager.get(levelName);

                if (level != null)
                {
                    if (!levelManager.exists(newLevelName))
                    {
                        if (!newLevelName.contains("'"))
                        {
                            // update in yaml and db
                            LevelsDB.updateName(levelName, newLevelName);

                            // update in level cache
                            level.setName(newLevelName);
                            levelManager.add(level);

                            // remove and add from location cache
                            LocationManager locationManager = Parkour.getLocationManager();
                            String oldSpawnLocation = SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", newLevelName);
                            String oldCompletionLocation = SettingsManager.LEVEL_COMPLETION_FORMAT.replace("%level%", newLevelName);

                            String newSpawnLocation = SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", newLevelName);
                            String newCompletionLocation = SettingsManager.LEVEL_COMPLETION_FORMAT.replace("%level%", newLevelName);

                            locationManager.updateName(oldSpawnLocation, newSpawnLocation);
                            locationManager.updateName(oldCompletionLocation, newCompletionLocation);

                            // run this in async, heavy task and can be in async
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Parkour.getMenuManager().renameLevel(levelName, newLevelName);
                                }
                            }.runTaskAsynchronously(Parkour.getPlugin());

                            sender.sendMessage(Utils.translate("&cYou have renamed &4" + levelName + " &cto &4" + newLevelName));
                        }
                        else
                            sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + newLevelName + " &calready exists"));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("elytra"))
            {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null) {
                    LevelsYAML.toggleElytraLevel(levelName);
                    sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                            "&7's Elytra to " + LevelsYAML.isElytraLevel(levelName)));
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("dropper")) {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null) {
                    LevelsYAML.toggleDropperLevel(levelName);
                    sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                            "&7 as a dropper level to &c" + LevelsYAML.isDropperLevel(levelName)));
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("tc")) {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null) {
                    LevelsYAML.toggleTCLevel(levelName);
                    sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                            "&7 as a tc level to &c" + LevelsYAML.isTCLevel(levelName)));
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("ascendance")) {
                String levelName = a[1].toLowerCase();
                Level level = levelManager.get(levelName);

                if (level != null) {
                    LevelsYAML.toggleAscendanceLevel(levelName);
                    sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                            "&7 as a ascendance level to &c" + LevelsYAML.isAscendanceLevel(levelName)));
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("delcompletions")) {
                String playerName = a[1];
                String levelName = a[2].toLowerCase();

                Level level = levelManager.get(levelName);

                if (level != null) {
                    int playerID = StatsDB.getPlayerID(playerName);
                    if (playerID > -1) {
                        if (StatsDB.hasCompleted(playerID, level.getID())) {

                            List<LevelCompletion> leaderboard = level.getLeaderboard();

                            // if deleting record
                            if (!leaderboard.isEmpty() && leaderboard.get(0).getPlayerName().equalsIgnoreCase(playerName))
                            {
                                // update records
                                PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

                                if (playerStats != null)
                                    Parkour.getStatsManager().removeRecord(playerStats, playerStats.getRecords());
                                else
                                    CompletionsDB.removeRecordsName(playerName);
                            }

                            StatsDB.removeCompletions(playerID, level.getID());

                            PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);
                            if (playerStats != null) {
                                // remove completion from stats and other personalized stat
                                playerStats.getLevelCompletionsMap().remove(levelName);
                                playerStats.setTotalLevelCompletions(playerStats.getTotalLevelCompletions() - 1);
                            }

                            // remove completion on level basis
                            levelManager.removeTotalLevelCompletion();
                            level.setTotalCompletionsCount(level.getTotalCompletionsCount() - 1);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    StatsDB.loadLeaderboard(level);

                                    if (!level.getLeaderboard().isEmpty())
                                    {
                                        String newFirstPlace = level.getLeaderboard().get(0).getPlayerName();
                                        PlayerStats playerStats = Parkour.getStatsManager().getByName(newFirstPlace);

                                        // if not null, use stats manager
                                        if (playerStats != null)
                                            playerStats.setRecords(playerStats.getRecords() + 1);

                                        StatsDB.addRecordsName(newFirstPlace);
                                    }
                                }
                            }.runTaskLaterAsynchronously(Parkour.getPlugin(), 5);

                            sender.sendMessage(Utils.translate("&cYou removed all of &4" + playerName + "&c's completions for &4" + levelName));
                        } else {
                            sender.sendMessage(Utils.translate("&4" + playerName + " &chas yet to complete &4" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + playerName + " &chas not joined the server"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("resetcheckpoints")) {
                String levelName = a[1].toLowerCase();
                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null) {
                    // reset from cache
                    for (PlayerStats playerStats : Parkour.getStatsManager().getPlayerStats().values())
                        if (playerStats.hasCurrentCheckpoint() && playerStats.getLevel() != null &&
                                playerStats.getLevel().getName().equalsIgnoreCase(levelName))
                        {
                            playerStats.resetCurrentCheckpoint();
                            playerStats.removeCheckpoint(levelName);
                        }

                    // delete from db
                    DatabaseQueries.runAsyncQuery("DELETE FROM checkpoints WHERE level_name=?", levelName);

                    sender.sendMessage(Utils.translate("&cYou have deleted all checkpoints for level &4" + levelName));
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("resetcheckpoint")) {
                String levelName = a[1].toLowerCase();
                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null) {
                    String playerName = a[2].toLowerCase();
                    Player target = Bukkit.getPlayer(playerName);

                    if (target != null) {
                        PlayerStats playerStats = Parkour.getStatsManager().get(target);

                        // if they have checkpoint loaded
                        if (playerStats.hasCurrentCheckpoint()) {
                            // if they are in level and their level is the same as the target level
                            if (playerStats.getLevel() != null && playerStats.getLevel().getName().equalsIgnoreCase(levelName)) {
                                Parkour.getCheckpointManager().deleteCheckpoint(playerStats, level);
                                sender.sendMessage(Utils.translate("&cYou deleted &4" + playerName + "&c's checkpoint for &4" + levelName));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + playerName + " &cis not in " + levelName));
                            }
                        } else {
                            DatabaseQueries.runAsyncQuery("DELETE FROM checkpoints WHERE level_name=? AND player_name=?", levelName, playerName);
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a loaded checkpoint, but any database record was deleted"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("cleanleveldatadb")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        HashMap<String, LevelData> levelCache = levelManager.getLevelDataCache();
                        HashMap<String, Level> levels = levelManager.getLevels();

                        Set<String> levelsToRemove = new HashSet<>();

                        for (String levelName : levelCache.keySet())
                            if (!levels.containsKey(levelName))
                                levelsToRemove.add(levelName); // add to levels to remove

                        // now remove from level cache and db
                        for (String levelName : levelsToRemove) {
                            levelCache.remove(levelName);
                            DatabaseQueries.runQuery("DELETE FROM levels WHERE level_name='" + levelName + "'");
                        }

                        sender.sendMessage(Utils.translate("&2" + levelsToRemove.size() + " &7levels cleaned from the database (already removed levels, etc)"));
                        Parkour.getPluginLogger().info("Levels in data cache: " + levelCache.size());
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }
            else if (a.length == 4 && a[0].equalsIgnoreCase("totalcompletions"))
            {
                // asyncify since big db searching
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        String levelName = a[1];
                        String startDate = a[2];
                        String endDate = a[3];

                        Level level = Parkour.getLevelManager().get(levelName);

                        if (level != null)
                        {
                            long totalCompletions = LevelsDB.getCompletionsBetweenDates(level.getID(), startDate, endDate);

                            sender.sendMessage(Utils.translate(
                                    "&c" + level.getFormattedTitle() + " &7between &a" + startDate + " &7and &a" + endDate +
                                            " &7has &a" + Utils.formatNumber(totalCompletions) + " &7Completions"));
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&cThe level &4" + levelName + " &cdoes not exist"));
                        }
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("syncrecords"))
            {
                // asyncify since big iteration
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        StatsDB.syncRecords();
                        sender.sendMessage(Utils.translate("&7Synced level records"));
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("setprice"))
            {
                String levelName = a[1];

                if (Utils.isInteger(a[2]))
                {
                    int price = Integer.parseInt(a[2]);
                    Level level = levelManager.get(levelName);

                    if (level != null)
                    {
                        level.setPrice(price);
                        LevelsYAML.setPrice(levelName, price);
                        sender.sendMessage(Utils.translate("&7You set the price of &c" + level.getFormattedTitle() + " &7to &6" + Utils.formatNumber(price) + " &e&lCoins"));
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&c'&4" + levelName + "&c' is not a valid level"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid integer"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("addboughtlevel"))
            {
                Level level = levelManager.get(a[2]);

                if (level != null)
                {
                    if (level.getPrice() > 0)
                    {
                        Player target = Bukkit.getPlayer(a[1]);

                        if (target != null)
                        {
                            PlayerStats targetStats = Parkour.getStatsManager().get(target);

                            if (!targetStats.hasBoughtLevel(level.getName()))
                            {
                                targetStats.buyLevel(level.getName());
                                StatsDB.addBoughtLevel(targetStats, level.getName());

                                sender.sendMessage(Utils.translate("&7You have added &c" + level.getFormattedTitle() + " &7to &4" + a[1] + "&c's &7bought levels"));
                            }
                            else
                            {
                                sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' has already bought &4" + level.getFormattedTitle() + "&c!"));
                            }
                        }
                        else
                        {
                            sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' is not online"));
                        }
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&c'&4" + level.getFormattedTitle() + "&c' is not a buyable level"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid level"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removeboughtlevel"))
            {
                Level level = levelManager.get(a[2]);

                if (level != null)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            if (StatsDB.isPlayerInDatabase(a[1]))
                            {
                                if (StatsDB.hasBoughtLevel(a[1], level.getName()))
                                {
                                    PlayerStats targetStats = Parkour.getStatsManager().getByName(a[1]);

                                    // if non null, remove bought level
                                    if (targetStats != null)
                                        targetStats.removeBoughtLevel(level.getName());

                                    StatsDB.removeBoughtLevel(a[1], level.getName());
                                    sender.sendMessage(Utils.translate("&7You have removed &c" + level.getFormattedTitle() + " &7from &4" + a[1] + "&c's &7bought levels"));
                                }
                                else
                                {
                                    sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' has not bought &4" + level.getFormattedTitle() + "&c!"));
                                }
                            }
                            else
                            {
                                sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' has not joined the server!"));
                            }
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid level"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("togglenew"))
            {
                Level level = levelManager.get(a[1]);

                if (level != null)
                {
                    level.toggleNewLevel();
                    LevelsYAML.setNewLevel(level.getName(), level.isNewLevel());
                    sender.sendMessage(Utils.translate("&7You have set the new level value of &c" + level.getFormattedTitle() + " &7to: &c" + level.isNewLevel()));
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' is not a valid level"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("setdifficulty"))
            {
                Level level = levelManager.get(a[1]);

                if (level != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int difficulty = Integer.parseInt(a[2]);

                        if (difficulty > 10)
                            difficulty = 10;

                        if (difficulty < 1)
                            difficulty = 1;

                        LevelsYAML.setDifficulty(level.getName(), difficulty);
                        sender.sendMessage(Utils.translate("&7You have set the difficulty of &c" + level.getFormattedTitle() + " &7to &c" + difficulty));
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid integer"));
                    }
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' is not a valid level"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("togglecooldown"))
            {
                Level level = levelManager.get(a[1]);

                if (level != null)
                {
                    LevelsYAML.toggleCooldown(level.getName());
                    sender.sendMessage(Utils.translate("&7You have set the level " + level.getFormattedTitle() + " &7cooldown to &c" + !level.hasCooldown()));
                }
                else
                {
                    sender.sendMessage(Utils.translate("&c'&4" + a[1] + "&c' is not a valid level"));
                }
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("pickfeatured"))
            {
               levelManager.pickFeatured();
               sender.sendMessage(Utils.translate("&7You have set the new featured to &c" + levelManager.getFeaturedLevel().getFormattedTitle()));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("resetsave"))
            {
                String playerName = a[1];
                Player target = Bukkit.getPlayer(playerName);
                String levelName = a[2].toLowerCase();
                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null)
                {
                    PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

                    if (playerStats != null)
                    {
                        // if they have save
                        if (playerStats.hasSave(levelName))
                        {
                            Parkour.getSavesManager().removeSave(playerStats, level);
                            sender.sendMessage(Utils.translate("&7You have reset &c" + playerName + "'s &7save on &a" + level.getFormattedTitle()));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a save for " + level.getFormattedTitle()));
                    }
                    else
                    {
                        DatabaseQueries.runAsyncQuery("DELETE FROM saves WHERE player_name=? AND level_name=?", playerName, levelName);
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online but any record has been deleted from the database"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));
                }
            }
            else
            {
                sender.sendMessage(Utils.translate("&c'&4" + a[0] + "&c' is not a valid parameter"));
                sendHelp(sender);
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to run that command"));
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&aTo reload levels from database, use &2/level load"));
        sender.sendMessage(Utils.translate("&7Level names are all lowercase"));
        sender.sendMessage(Utils.translate("&a/level show  &7Show level information"));
        sender.sendMessage(Utils.translate("&a/level create <level>  &7Create a level"));
        sender.sendMessage(Utils.translate("&a/level load  &7Loads levels.yml then levels"));
        sender.sendMessage(Utils.translate("&a/level delete <level>  &7Delete a level"));
        sender.sendMessage(Utils.translate("&a/level title <level> <title>  &7Set a level's title"));
        sender.sendMessage(Utils.translate("&a/level reward <level> <reward>  &7Set a level's reward"));
        sender.sendMessage(Utils.translate("&a/level startloc <level>  &7Sets the start to your location"));
        sender.sendMessage(Utils.translate("&a/level completionloc <level>  &7Sets the completion to your location"));
        sender.sendMessage(Utils.translate("&a/level maxcompletions <level> [completions]  &7View/Set max completions"));
        sender.sendMessage(Utils.translate("&a/level broadcast <level>  &7Toggled broadcast completion"));
        sender.sendMessage(Utils.translate("&a/level requires <level> <levelTheyNeed>  &7Add/Remove required level"));
        sender.sendMessage(Utils.translate("&a/level removetime <level> <leaderboardPlace>  &7Removes a player's time from a level's leaderboard"));
        sender.sendMessage(Utils.translate("&a/level raceset <level> <1/2>  &7Sets the race location for player 1 or 2"));
        sender.sendMessage(Utils.translate("&a/level forcecompletion <player> <level>  &7Force completion for player"));
        sender.sendMessage(Utils.translate("&a/level addrating <level> <rating (0-5)>  &7Adds a rating to a level (ADMIN WAY NOT /rate)"));
        sender.sendMessage(Utils.translate("&a/level removerating <level> <playerName>  &7Removes a rating from a level by player name"));
        sender.sendMessage(Utils.translate("&a/level hasrated <level> <playerName>  &7Tells you if someone has rated it and with what rating"));
        sender.sendMessage(Utils.translate("&a/level listratings <level> [rating (0-5)] &7Tells you all the ratings for a level with optional 0-5 specification"));
        sender.sendMessage(Utils.translate("&a/level togglewater <level>  &7Toggles the water from respawning you in a level"));
        sender.sendMessage(Utils.translate("&a/level rename <level> <newLevelName>  &7Renames a level's name to a new name"));
        sender.sendMessage(Utils.translate("&a/level delcompletions <player> <levelName>  &7Deletes ALL the completions of a player for a level"));
        sender.sendMessage(Utils.translate("&a/level respawny <level> <respawnY>  &7Sets level respawn y"));
        sender.sendMessage(Utils.translate("&a/level elytra <level>  &7Sets level elytra"));
        sender.sendMessage(Utils.translate("&a/level dropper <level>  &7Sets level as dropper"));
        sender.sendMessage(Utils.translate("&a/level ascendance <level>  &7Sets level as ascendance level"));
        sender.sendMessage(Utils.translate("&a/level race <level>  &7Sets level as race level"));
        sender.sendMessage(Utils.translate("&a/level resetcheckpoint <level> <player>  &7Resets level checkpoint for single player"));
        sender.sendMessage(Utils.translate("&a/level resetcheckpoints <level>  &7Resets ALL checkpoints for specific level"));
        sender.sendMessage(Utils.translate("&a/level cleanleveldatadb  &7Cleans invalid data"));
        sender.sendMessage(Utils.translate("&a/level totalcompletions <level> <startDate> <endDate>  &7Gets total number of completions between two dates (YYYY-MM-DD)"));
        sender.sendMessage(Utils.translate("&a/level setprice <level> <price>  &7Sets the level's price"));
        sender.sendMessage(Utils.translate("&a/level addboughtlevel <player> <level>  &7Add bought level to player"));
        sender.sendMessage(Utils.translate("&a/level removeboughtlevel <player> <level>  &7Remove bought level from player"));
        sender.sendMessage(Utils.translate("&a/level togglenew <level>  &7Toggles if the level is new (for menu and future updates)"));
        sender.sendMessage(Utils.translate("&a/level setdifficulty <level> <difficulty>  &7Sets the difficulty of the level"));
        sender.sendMessage(Utils.translate("&a/level togglecooldown <level>  &7Toggles if the level has a cooldown"));
        sender.sendMessage(Utils.translate("&a/level pickfeatured  &7Picks a new featured level"));
        sender.sendMessage(Utils.translate("&a/level resetsave <player> <level>  &7Resets a player's save for a specific level"));
        sender.sendMessage(Utils.translate("&a/level toggletc <level>  &7Sets level as tc"));
    }
}
