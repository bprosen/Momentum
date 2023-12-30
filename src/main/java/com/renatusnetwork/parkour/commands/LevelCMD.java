package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.levels.*;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.saves.SavesDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.gameplay.handlers.LevelHandler;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import jdk.internal.org.jline.reader.impl.UndoTree;
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

            if (a.length == 1 && a[0].equalsIgnoreCase("show"))
            { // subcommand: show
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
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String title = String.join(" ", split);

                    levelManager.setTitle(level, title);

                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's title to &2" + title));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("reward"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("startloc"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("completionloc"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length > 1 && a[0].equalsIgnoreCase("maxcompletions"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("broadcast"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    levelManager.toggleBroadcastCompletion(level);
                    sender.sendMessage(Utils.translate("&7Broadcast for &2" + levelName + " &7 set to " + level.isBroadcasting()));
                }
            }
            else if (a.length == 3 && (a[0].equalsIgnoreCase("addrequired") || a[0].equalsIgnoreCase("removerequired")))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String requiredLevelName = a[2].toLowerCase();
                    Level requiredLevel = getLevel(sender, requiredLevelName);

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
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removetime"))
            {

                if (Utils.isInteger(a[2]))
                {
                    int place = Integer.parseInt(a[2]);

                    String levelName = a[1].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null)
                    {
                        HashMap<Integer, LevelCompletion> leaderboard = level.getLeaderboard();

                        if (place <= leaderboard.size() && place > 0)
                        {
                            LevelCompletion oldHolder = leaderboard.get(place);

                            // run it in async!
                            new BukkitRunnable()
                            {
                                @Override
                                public void run()
                                {
                                    CompletionsDB.removeCompletion(oldHolder, false);

                                    level.setTotalCompletionsCount(level.getTotalCompletionsCount() - 1);
                                    level.setLeaderboard(CompletionsDB.getLeaderboard(levelName));
                                    levelManager.removeTotalLevelCompletion();

                                    // if deleting record
                                    if (place == 1)
                                    {
                                        HashMap<Integer, LevelCompletion> newLeaderbaord = level.getLeaderboard();

                                        if (!newLeaderbaord.isEmpty())
                                        {
                                            // if the new leaderboard is no longer empty, add record for new holder
                                            LevelCompletion newHolder = level.getRecordCompletion();

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
                                            "&4" + oldHolder.getName() + "'s &ctime has been removed succesfully from &4" + levelName
                                    ));
                                }
                            }.runTaskAsynchronously(Parkour.getPlugin());
                        }
                        else
                            sender.sendMessage(Utils.translate("&cYou are entering an integer above 9"));
                    }
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
                        Level level = getLevel(sender, levelName);

                        if (level != null)
                        {
                            if (level.isRaceLevel())
                            {
                                String locationName = SettingsManager.RACE_LEVEL_SPAWN_FORMAT
                                        .replace("%level%", level.getName())
                                        .replace("%spawn%", a[2]);

                                Parkour.getLocationManager().set(locationName, player.getLocation());
                                player.sendMessage(Utils.translate("&cYou set the location for player &4" + a[2] + " &con level &4" + level.getFormattedTitle()));
                            }
                            else
                                sender.sendMessage(Utils.translate("&cYou cannot set the spawn for a non-race level. Do /level type (levelName) RACE"));
                        }
                    }
                    else
                        sender.sendMessage(Utils.translate("&cArgument must be 1 or 2"));
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot run this"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("forcecompletion"))
            {
                PlayerStats playerStats = Parkour.getStatsManager().getByName(a[1]);

                if (playerStats != null)
                {

                    String levelName = a[2].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null)
                    {
                        LevelHandler.dolevelCompletion(playerStats, playerStats.getPlayer(), level, levelName, true);
                        sender.sendMessage(Utils.translate("&7You forced a &c" + level.getFormattedTitle() + " &7Completion for &a" + playerStats.getName()));
                    }
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));

            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("respawny"))
            {
                if (Utils.isInteger(a[2]))
                {
                    // get new y
                    int newY = Integer.parseInt(a[2]);

                    String levelName = a[1].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null)
                    {
                        levelManager.setRespawnY(level, newY);
                        sender.sendMessage(Utils.translate(
                                "&7You set &c" + level.getFormattedTitle() + "&7's respawn y to &c" + newY
                        ));
                    }
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
                    Level level = getLevel(sender, levelName);

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
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot do this"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removerating"))
            {
                String levelName = a[1].toLowerCase();
                String targetName = a[2];
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("hasrated"))
            {
                String levelName = a[1].toLowerCase();
                String playerName = a[2];
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length >= 2 && a[0].equalsIgnoreCase("listratings"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("togglewater"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

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
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("rename"))
            {
                String levelName = a[1].toLowerCase();
                String newLevelName = a[2].toLowerCase();

                Level level = getLevel(sender, levelName);

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

                            sender.sendMessage(Utils.translate("&7You have renamed &c" + levelName + " &7to &a" + newLevelName));
                        }
                        else
                            sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + newLevelName + " &calready exists"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("type"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String levelType = a[2].toUpperCase();

                    try
                    {
                        LevelType type = LevelType.valueOf(levelType);
                        levelManager.setLevelType(level, type);
                        sender.sendMessage(Utils.translate("&7You have set &2" + level.getFormattedTitle() + "&7's type to &2" + type.name()));
                    }
                    catch (IllegalArgumentException exception)
                    {
                        sender.sendMessage(Utils.translate("&cInvalid level type, options are: " + Arrays.toString(LevelType.values())));
                    }
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("resetcheckpoint"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String playerName = a[2].toLowerCase();

                    Parkour.getCheckpointManager().deleteCheckpoint(playerName, level);
                    sender.sendMessage(Utils.translate(
                            "&7Any checkpoints stored for &c" + playerName + "&7 on level &2" + level.getFormattedTitle() + " &7has been deleted"
                    ));
                }
            }
            else if (a.length == 4 && a[0].equalsIgnoreCase("totalcompletions"))
            {
                // asyncify since big db searching
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        String levelName = a[1].toLowerCase();
                        String startDate = a[2];
                        String endDate = a[3];

                        Level level = getLevel(sender, levelName);

                        if (level != null)
                        {
                            long totalCompletions = LevelsDB.getCompletionsBetweenDates(level.getName(), startDate, endDate);

                            sender.sendMessage(Utils.translate(
                                    "&c" + level.getFormattedTitle() + " &7between &a" + startDate + " &7and &a" + endDate +
                                         " &7has &a" + Utils.formatNumber(totalCompletions) + " &7Completions"
                            ));
                        }
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("price"))
            {
                if (Utils.isInteger(a[2]))
                {
                    int price = Integer.parseInt(a[2]);
                    String levelName = a[1].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null)
                    {
                        levelManager.setPrice(level, price);
                        sender.sendMessage(Utils.translate(
                                "&7You set the price of &c" + level.getFormattedTitle() + " &7to &6" + Utils.formatNumber(price) + " &e&lCoins"
                        ));
                    }
                }
                else
                    sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid integer"));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("addboughtlevel"))
            {
                Level level = getLevel(sender, a[2].toLowerCase());

                if (level != null)
                {
                    if (level.isBuyable())
                    {
                        String playerName = a[1];
                        PlayerStats targetStats = Parkour.getStatsManager().getByName(a[1]);

                        if (targetStats != null)
                        {
                            if (!targetStats.hasBoughtLevel(level))
                            {
                                Parkour.getStatsManager().addBoughtLevel(targetStats, level);
                                sender.sendMessage(Utils.translate(
                                        "&7You have added &c" + level.getFormattedTitle() + " &7to &4" + playerName + "&c's &7bought levels"
                                ));
                            }
                            else
                                sender.sendMessage(Utils.translate("&c'&4" + playerName + "&c' has already bought &4" + level.getFormattedTitle()));
                        }
                        else
                            sender.sendMessage(Utils.translate("&c'&4" + playerName + "&c' is not online"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&c'&4" + level.getFormattedTitle() + "&c' is not a buyable level"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removeboughtlevel"))
            {
                Level level = getLevel(sender, a[2].toLowerCase());

                if (level != null)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            String playerName = a[1];

                            // this also acts as a checker of if they have ever joined
                            if (StatsDB.hasBoughtLevel(playerName, level.getName()))
                            {
                                PlayerStats targetStats = Parkour.getStatsManager().getByName(playerName);

                                if (targetStats != null)
                                    Parkour.getStatsManager().removeBoughtLevel(targetStats, level);

                                StatsDB.removeBoughtLevelByName(playerName, level.getName());
                                sender.sendMessage(Utils.translate("&7You have removed &c" + level.getFormattedTitle() + " &7from &4" + playerName + "&c's &7bought levels"));
                            }
                            else
                                sender.sendMessage(Utils.translate(
                                        "&c'&4" + playerName + "&c' has either never joined or not bought &4" + level.getFormattedTitle()
                                ));
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("new"))
            {
                Level level = getLevel(sender, a[1].toLowerCase());

                if (level != null)
                {
                    levelManager.toggleNew(level);
                    sender.sendMessage(Utils.translate(
                            "&7You have set the new level value of &c" + level.getFormattedTitle() + " &7to &c" + level.isNew()
                    ));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("difficulty"))
            {
                Level level = getLevel(sender, a[1].toLowerCase());

                if (level != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int difficulty = Integer.parseInt(a[2]);
                        int maxDifficulty = Parkour.getSettingsManager().max_difficulty;
                        int minDifficulty = Parkour.getSettingsManager().min_difficulty;

                        if (difficulty > maxDifficulty)
                            difficulty = maxDifficulty;

                        if (difficulty < minDifficulty)
                            difficulty = minDifficulty;

                        levelManager.setDifficulty(level, difficulty);
                        sender.sendMessage(Utils.translate(
                                "&7You have set the difficulty of &c" + level.getFormattedTitle() + " &7to &c" + difficulty
                        ));
                    }
                    else
                    {
                        sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid integer"));
                    }
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("cooldown"))
            {
                Level level = getLevel(sender, a[1].toLowerCase());

                if (level != null)
                {
                    levelManager.toggleCooldown(level);
                    sender.sendMessage(Utils.translate("&7You have turned " + level.getFormattedTitle() + "&7's cooldown toggle to &2" + level.hasCooldown()));
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
                String levelName = a[2].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    PlayerStats playerStats = Parkour.getStatsManager().getByName(playerName);

                    if (playerStats != null)
                    {
                        // if they have save
                        if (playerStats.hasSave(level))
                        {
                            Parkour.getSavesManager().removeSave(playerStats, level);
                            sender.sendMessage(Utils.translate("&7You have reset &c" + playerName + "'s &7save on &a" + level.getFormattedTitle()));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a save for " + level.getFormattedTitle()));
                    }
                    else
                    {
                        SavesDB.removeSaveFromName(playerName, level.getName());
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online but any record has been deleted from the database"));
                    }
                }
            }
            else
            {
                sender.sendMessage(Utils.translate("&c'&4" + a[0] + "&c' is not a valid parameter"));
                sendHelp(sender);
            }
        }
        return true;
    }

    private static Level getLevel(CommandSender sender, String levelName)
    {
        Level level = Parkour.getLevelManager().get(levelName);

        if (level == null)
            sender.sendMessage(Utils.translate("&4'&c" + levelName + "&4' &cdoes not exist"));

        return level;
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
        sender.sendMessage(Utils.translate("&a/level type <level> <elytra, dropper, tc, ascendance, race, [event types]>  &7Sets level type"));
        sender.sendMessage(Utils.translate("&a/level resetcheckpoint <level> <player>  &7Resets level checkpoint for single player"));
        sender.sendMessage(Utils.translate("&a/level totalcompletions <level> <startDate> <endDate>  &7Gets total number of completions between two dates (YYYY-MM-DD)"));
        sender.sendMessage(Utils.translate("&a/level price <level> <price>  &7Sets the level's price"));
        sender.sendMessage(Utils.translate("&a/level addboughtlevel <player> <level>  &7Add bought level to player"));
        sender.sendMessage(Utils.translate("&a/level removeboughtlevel <player> <level>  &7Remove bought level from player"));
        sender.sendMessage(Utils.translate("&a/level new <level>  &7Toggles if the level is new (for menu and future updates)"));
        sender.sendMessage(Utils.translate("&a/level difficulty <level> <difficulty>  &7Sets the difficulty of the level"));
        sender.sendMessage(Utils.translate("&a/level cooldown <level>  &7Toggles if the level has a cooldown"));
        sender.sendMessage(Utils.translate("&a/level pickfeatured  &7Picks a new featured level"));
        sender.sendMessage(Utils.translate("&a/level resetsave <player> <level>  &7Resets a player's save for a specific level"));
    }
}