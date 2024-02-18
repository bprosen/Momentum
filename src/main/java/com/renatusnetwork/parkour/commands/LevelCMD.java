package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.leaderboards.LevelLBPosition;
import com.renatusnetwork.parkour.data.levels.*;
import com.renatusnetwork.parkour.data.locations.LocationManager;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.saves.SavesDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.gameplay.handlers.LevelHandler;
import com.renatusnetwork.parkour.storage.mysql.DatabaseQueries;
import com.renatusnetwork.parkour.utils.Utils;
import jdk.internal.org.jline.reader.impl.UndoTree;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class LevelCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        LevelManager levelManager = Parkour.getLevelManager();
        StatsManager statsManager = Parkour.getStatsManager();

        if (a.length >= 2 && a[0].equalsIgnoreCase("buy"))
        {
            if (sender instanceof Player)
            {
                Player player = (Player) sender;
                PlayerStats playerStats = statsManager.get(player);

                String[] split = Arrays.copyOfRange(a, 1, a.length);
                String levelName = String.join(" ", split);

                Level level = levelManager.getNameThenTitle(levelName);

                // no level exists otherwise
                if (level != null)
                {
                    if (level.requiresBuying())
                    {
                        if (!playerStats.hasCompleted(level))
                        {
                            if (!playerStats.hasBoughtLevel(level))
                            {
                                int balance = playerStats.getCoins();
                                int price = level.getPrice();

                                if (balance >= price)
                                {
                                    statsManager.removeCoins(playerStats, price);
                                    statsManager.addBoughtLevel(playerStats, level);
                                    player.sendMessage(Utils.translate("&7You have bought &c" + level.getTitle()));
                                }
                                else
                                {
                                    int amountLeft = price - balance;
                                    player.sendMessage(Utils.translate(
                                            "&cYou cannot buy &4" + level.getTitle() + "&c, you need an additional &6" +
                                                    Utils.formatNumber(amountLeft) + " &eCoins"));

                                }
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou have already bought &4" + level.getTitle()));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou have already completed &4" + level.getTitle()));
                    }
                    else
                        player.sendMessage(Utils.translate("&c" + level.getTitle() + "&c does not require buying"));
                }
                else
                    sender.sendMessage(Utils.translate("&4'&c" + levelName + "&4' &cdoes not exist"));
            }
            else
                sender.sendMessage(Utils.translate("&cConsole cannot do this"));
        }
        else if (sender.isOp())
        {
            if (a.length >= 2 && a[0].equalsIgnoreCase("stuckurl"))
            {
                String levelName = a[1].toLowerCase();

                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    if (a.length > 2)
                    {
                        String url = a[2];
                        int maxURLLength = SettingsManager.STUCK_URL_LENGTH;

                        if (url.length() <= maxURLLength)
                        {
                            levelManager.updateStuckURL(level, url);
                            sender.sendMessage(Utils.translate("&7You have set &2" + level.getTitle() + "&7's stuck URL to &a" + url));
                        }
                        else
                            sender.sendMessage(Utils.translate(
                                    "&cYou cannot set the stuck URL to longer than &4" + maxURLLength +
                                         "&c characters, try shortening the link with TinyUrl, Bitly, etc"
                            ));
                    }
                    // if it has a stuck url, reset it
                    else if (level.hasStuckURL())
                    {
                        levelManager.resetStuckURL(level);
                        sender.sendMessage(Utils.translate("&7You have reset &2" + level.getTitle() + "&7's stuck URL"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&2" + level.getTitle() + " &cdoes not have a stuck URL to reset"));
                }
            }
            else if (a.length >= 2 && a[0].equalsIgnoreCase("reveal"))
            {
                String[] split = Arrays.copyOfRange(a, 1, a.length);
                String levelName = String.join(" ", split);

                Level level = levelManager.getNameThenTitle(levelName);

                if (level != null)
                    sender.sendMessage(Utils.translate("&a" + levelName + "&7 is level name &2" + level.getName()));
                else
                    sender.sendMessage(Utils.translate("&4'&c" + levelName + "&4' &cdoes not exist"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("show"))
            {
                if (sender instanceof Player)
                {

                    Player player = (Player) sender;
                    PlayerStats playerStats = statsManager.get(player);

                    if (playerStats.inLevel())
                        sender.sendMessage(Utils.translate("&7You are in &c" + playerStats.getLevel().getTitle()));
                    else
                        sender.sendMessage(Utils.translate("&7You are not in a level"));
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot run this"));
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
            {
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
                sender.sendMessage(Utils.translate("&7Loading levels..."));

                // load total completions and leaderboards in async
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        levelManager.load();
                        sender.sendMessage(Utils.translate("&7Loaded &2" + levelManager.numLevels() + " &7levels"));
                        CompletionsDB.loadTotalCompletions();
                        CompletionsDB.loadLeaderboards();
                        levelManager.loadGlobalLevelCompletions();
                        levelManager.loadRecordsLB();
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
                        sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
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
            else if (a.length == 3 && a[0].equalsIgnoreCase("removelbposition"))
            {
                if (Utils.isInteger(a[2]))
                {
                    int place = Integer.parseInt(a[2]);

                    String levelName = a[1].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null && level.hasLeaderboard())
                    {
                        List<LevelLBPosition> leaderboard = level.getLeaderboard();

                        if (place <= leaderboard.size() && place > 0)
                        {
                            LevelLBPosition oldHolder = leaderboard.get(place - 1);

                            // run it in async!
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    CompletionsDB.removeCompletionFromName(oldHolder.getPlayerName(), oldHolder.getLevelName(), oldHolder.getTimeTaken(), false);

                                    level.setTotalCompletionsCount(level.getTotalCompletionsCount() - 1);
                                    level.setLeaderboard(CompletionsDB.getLeaderboard(levelName));
                                    levelManager.removeTotalLevelCompletion();

                                    // if deleting record
                                    if (place == 1)
                                    {
                                        List<LevelLBPosition> newLeaderboard = level.getLeaderboard();

                                        if (!newLeaderboard.isEmpty())
                                        {
                                            // if the new leaderboard is no longer empty, add record for new holder
                                            LevelLBPosition newHolder = newLeaderboard.get(0);

                                            // if it is a diff person, need to update their in game stats
                                            if (!oldHolder.getPlayerName().equalsIgnoreCase(newHolder.getPlayerName())) {
                                                PlayerStats oldHolderStats = statsManager.getByName(oldHolder.getPlayerName());
                                                PlayerStats newHolderStats = statsManager.getByName(newHolder.getPlayerName());

                                                if (oldHolderStats != null)
                                                    oldHolderStats.removeRecord(level);

                                                if (newHolderStats != null)
                                                    newHolderStats.addRecord(level, newHolder.getTimeTaken());
                                            }
                                        }
                                    }

                                    PlayerStats targetStats = Parkour.getStatsManager().getByName(oldHolder.getPlayerName());

                                    if (targetStats != null)
                                        targetStats.removeCompletion(oldHolder.getLevelName(), oldHolder.getTimeTaken());

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
                        sender.sendMessage(Utils.translate("&c" + a[1] + " is not a level or has no leaderboard"));
                }
                else
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7is not an integer!"));
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
                                "&7You set &c" + level.getTitle() + "&7's respawn y to &c" + newY
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
                                            "&7You added a rating of &4" + rating + " &7to &cLevel &a" + level.getTitle()
                                    ));
                                }
                                else
                                    sender.sendMessage(Utils.translate("&cRating has to be between &4" + minRating + "-" + maxRating));
                            }
                            else
                                sender.sendMessage(Utils.translate("&c" + a[2] + " is not an integer"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&cYou have already rated &4" + level.getTitle()));
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
                        sender.sendMessage(Utils.translate("&cYou removed &4" + targetName + "&7's from &c" + level.getTitle()));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + targetName + " &chas not rated &4" + level.getTitle()));
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
                                "&c" + playerName + " &7has rated &c" + level.getTitle() + "&7 a &6" + rating
                        ));
                    else
                        sender.sendMessage(Utils.translate(
                                "&c" + playerName + " &7has not rated &c" + level.getTitle()
                        ));
                }
            }
            else if (a.length >= 2 && a[0].equalsIgnoreCase("listratings")) {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    if (a.length == 2)
                    {
                        if (level.getRatingsCount() > 0)
                        {
                            sender.sendMessage(Utils.translate("&2" + level.getTitle() + "&7's Ratings"));

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
                            sender.sendMessage(Utils.translate("&cNobody has rated &4" + level.getTitle()));
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
                                            "&7Players who rated &2" + level.getTitle() + "&7 a &a" + rating
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
                            "&7You toggled " + offOrOn + " &7liquid resetting players for level &c" + level.getTitle()
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
                        sender.sendMessage(Utils.translate("&7You have set &2" + level.getTitle() + "&7's type to &2" + type.name()));
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
                            "&7Any checkpoints stored for &c" + playerName + "&7 on level &2" + level.getTitle() + "&7 has been deleted"
                    ));
                }
            }
            else if (a.length == 4 && a[0].equalsIgnoreCase("totalcompletions"))
            {
                // asyncify since big db searching
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String levelName = a[1].toLowerCase();
                        String startDate = a[2];
                        String endDate = a[3];

                        Level level = getLevel(sender, levelName);

                        if (level != null)
                        {
                            long totalCompletions = LevelsDB.getCompletionsBetweenDates(level.getName(), startDate, endDate);

                            sender.sendMessage(Utils.translate(
                                    "&c" + level.getTitle() + "&7 between &a" + startDate + " &7and &a" + endDate +
                                            " &7has &a" + Utils.formatNumber(totalCompletions) + " &7Completions"
                            ));
                        }
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            } else if (a.length == 3 && a[0].equalsIgnoreCase("price"))
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
                                "&7You set the price of &c" + level.getTitle() + "&7 to &6" + Utils.formatNumber(price) + " &eCoins"
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
                    if (level.requiresBuying())
                    {
                        String playerName = a[1];
                        PlayerStats targetStats = statsManager.getByName(a[1]);

                        if (targetStats != null)
                        {
                            if (!targetStats.hasBoughtLevel(level))
                            {
                                statsManager.addBoughtLevel(targetStats, level);
                                sender.sendMessage(Utils.translate(
                                        "&7You have added &c" + level.getTitle() + "&7 to &4" + playerName + "&c's &7bought levels"
                                ));
                            }
                            else
                                sender.sendMessage(Utils.translate("&c'&4" + playerName + "&c' has already bought &4" + level.getTitle()));
                        }
                        else
                            sender.sendMessage(Utils.translate("&c'&4" + playerName + "&c' is not online"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&c'&4" + level.getTitle() + "&c' is not a buyable level"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("removeboughtlevel"))
            {
                Level level = getLevel(sender, a[2].toLowerCase());

                if (level != null)
                {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            String playerName = a[1];

                            // this also acts as a checker of if they have ever joined
                            if (StatsDB.hasBoughtLevel(playerName, level.getName()))
                            {
                                PlayerStats targetStats = statsManager.getByName(playerName);

                                if (targetStats != null)
                                    statsManager.removeBoughtLevel(targetStats, level);

                                StatsDB.removeBoughtLevelByName(playerName, level.getName());
                                sender.sendMessage(Utils.translate("&7You have removed &c" + level.getTitle() + "&7 from &4" + playerName + "&c's &7bought levels"));
                            }
                            else
                                sender.sendMessage(Utils.translate(
                                        "&c'&4" + playerName + "&c' has either never joined or not bought &4" + level.getTitle()
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
                            "&7You have set the new level value of &c" + level.getTitle() + "&7 to &c" + level.isNew()
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
                                "&7You have set the difficulty of &c" + level.getTitle() + "&7 to &c" + difficulty
                        ));
                    }
                    else
                        sender.sendMessage(Utils.translate("&c'&4" + a[2] + "&c' is not a valid integer"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("cooldown"))
            {
                Level level = getLevel(sender, a[1].toLowerCase());

                if (level != null)
                {
                    levelManager.toggleCooldown(level);
                    sender.sendMessage(Utils.translate("&7You have turned " + level.getTitle() + "&7's cooldown toggle to &2" + level.hasCooldown()));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("tc"))
            {
                Level level = getLevel(sender, a[1].toLowerCase());

                if (level != null)
                {
                    levelManager.toggleTC(level);
                    sender.sendMessage(Utils.translate("&7You have turned " + level.getTitle() + "&7's cooldown toggle to &2" + level.hasCooldown()));
                }
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("pickfeatured"))
            {
                levelManager.pickFeatured();
                sender.sendMessage(Utils.translate("&7You have set the new featured to &c" + levelManager.getFeaturedLevel().getTitle()));
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("resetsave"))
            {
                String playerName = a[1];
                String levelName = a[2].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    PlayerStats playerStats = statsManager.getByName(playerName);

                    if (playerStats != null)
                    {
                        // if they have save
                        if (playerStats.hasSave(level))
                        {
                            Parkour.getSavesManager().removeSave(playerStats, level);
                            sender.sendMessage(Utils.translate("&7You have reset &c" + playerName + "'s &7save on &a" + level.getTitle()));
                        } else
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a save for " + level.getTitle()));
                    }
                    else
                    {
                        SavesDB.removeSaveFromName(playerName, level.getName());
                        sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online but any record has been deleted from the database"));
                    }
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("mastery"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    levelManager.toggleHasMastery(level);
                    sender.sendMessage(Utils.translate("&7You have set &c" + level.getTitle() + "&7 having a mastery to &a" + level.hasMastery()));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("masterymultiplier"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String multiplier = a[2];

                    if (Utils.isFloat(multiplier))
                    {
                        float multiplierValue = Float.parseFloat(multiplier);
                        if (level.hasMastery())
                        {
                            float minValue = Parkour.getSettingsManager().min_mastery_multiplier;
                            float maxValue = Parkour.getSettingsManager().max_mastery_multiplier;

                            if (multiplierValue < minValue)
                                multiplierValue = minValue;

                            if (multiplierValue > maxValue)
                                multiplierValue = maxValue;

                            levelManager.setMasteryMultiplier(level, multiplierValue);
                            sender.sendMessage(Utils.translate("&7You have set &a" + level.getTitle() + "&7's mastery multiplifer to &a" + multiplierValue));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + levelName + " &cdoes not have a mastery version"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + multiplier + " &cis not a valid float"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("permission"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String permission = a[2].toLowerCase();
                    levelManager.setRequiredPermission(level, permission);
                    sender.sendMessage(Utils.translate("&7You have set &a" + level.getTitle() + "&7's required permission to enter to &a" + permission));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("removepermission"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    levelManager.removeRequiredPermission(level);
                    sender.sendMessage(Utils.translate("&7You have removed &a" + level.getTitle() + "&7's required permission"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("rank"))
            {
                String levelName = a[1].toLowerCase();
                Level level = getLevel(sender, levelName);

                if (level != null)
                {
                    String rankName = a[2].toLowerCase();
                    Rank rank = Parkour.getRanksManager().get(rankName);

                    if (rank != null)
                    {
                        levelManager.setRequiredRank(level, rank);
                        sender.sendMessage(Utils.translate("&7You have set &a" + level.getTitle() + "&7's required rank to enter to &a" + rank.getTitle()));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + rankName + " &cis not a rank"));
                }
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("makesign"))
            {
                if (sender instanceof Player)
                {
                    Player player = (Player) sender;
                    String levelName = a[1].toLowerCase();
                    Level level = getLevel(sender, levelName);

                    if (level != null)
                    {
                        List<Block> blocks = player.getLastTwoTargetBlocks(null, 5);

                        // ensure bocks are adequate and not too far away
                        if (blocks.size() == 2 && blocks.get(1).getType() != null && blocks.get(1).getType() != Material.AIR)
                        {
                            final int MAX_SIGN_LENGTH = 15;

                            Block adjacentBlock = blocks.get(0);
                            Block targetBlock = blocks.get(1);
                            Block signBlock;

                            BlockFace face = targetBlock.getFace(adjacentBlock);

                            // ensure no trying to place a sign on ground or roof
                            if (face != BlockFace.DOWN && face != BlockFace.UP)
                            {
                                boolean overwriteSign = false;

                                // if the block we are looking at is already a sign, we only want to replace the lines
                                if (targetBlock.getType() == Material.WALL_SIGN)
                                {
                                    signBlock = targetBlock;
                                    overwriteSign = true;
                                }
                                else
                                    signBlock = targetBlock.getRelative(face);

                                // unfortunately we need to seperate these two overwriteSign check as the type has to be set first, and we need the Sign class for both cases
                                if (!overwriteSign)
                                    signBlock.setType(Material.WALL_SIGN);

                                Sign sign = (Sign) signBlock.getState();

                                // create sign with direction
                                if (!overwriteSign)
                                {
                                    org.bukkit.material.Sign matSign = new org.bukkit.material.Sign(Material.WALL_SIGN);

                                    matSign.setFacingDirection(face);
                                    sign.setData(matSign);
                                }

                                // use config set
                                sign.setLine(0, Utils.translate("&1&l" + Parkour.getSettingsManager().signs_first_line));
                                sign.setLine(1, Utils.translate("&9" + Parkour.getSettingsManager().signs_second_line_completion));
                                sign.setLine(2, Utils.translate("&9the prize for"));

                                String levelTitle = level.getFormattedTitle();

                                // if the title goes beyond the max length, set it without colors
                                if (MAX_SIGN_LENGTH < level.getTitle().length())
                                {
                                    String noColorTitle = ChatColor.stripColor(levelTitle);
                                    // if the line is STILL too long with no color, substring it, otherwise just set it with no color
                                    levelTitle = MAX_SIGN_LENGTH < noColorTitle.length() ? noColorTitle.substring(0, MAX_SIGN_LENGTH) : noColorTitle;
                                }
                                sign.setLine(3, levelTitle);

                                sign.update();

                                player.sendMessage(Utils.translate("&7You have created the sign for &8" + level.getTitle()));
                            }
                            else
                                sender.sendMessage(Utils.translate("&cWall signs cannot go on the ground or roof, try the side of a block"));
                        }
                        else
                            sender.sendMessage(Utils.translate("&cNo target block found, make sure you are right in front of and looking at the block to place the sign on"));
                    }
                }
                else
                    sender.sendMessage(Utils.translate("&cConsole cannot do this"));
            }
            else
                sendHelp(sender);
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

    private static void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&aTo reload levels from database, use &2/level load"));
        sender.sendMessage(Utils.translate("&7Level names are all lowercase"));
        sender.sendMessage(Utils.translate("&a/level buy  &7Buys a level if it has a price"));
        sender.sendMessage(Utils.translate("&a/level stuckurl <level> [url]  &7Set a level's stuck URL. Resets if no URL is provided"));
        sender.sendMessage(Utils.translate("&a/level reveal <title>  &7Reveals a level name, will pull from name then title and send name"));
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
        sender.sendMessage(Utils.translate("&a/level addrequired/removerequired <level> <levelTheyNeed>  &7Add/Remove required level"));
        sender.sendMessage(Utils.translate("&a/level removelbposition <level> <leaderboardPlace>  &7Removes a player's time from a level's leaderboard"));
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
        sender.sendMessage(Utils.translate("&a/level tc <level>  &7Toggles if the level uses TC blocks"));
        sender.sendMessage(Utils.translate("&a/level pickfeatured  &7Picks a new featured level"));
        sender.sendMessage(Utils.translate("&a/level resetsave <player> <level>  &7Resets a player's save for a specific level"));
        sender.sendMessage(Utils.translate("&a/level mastery <level>  &7Toggles if a level has a mastery mode"));
        sender.sendMessage(Utils.translate("&a/level masterymultiplier <level> <multiplier>  &7Multiplier the level gives for its mastery version"));
        sender.sendMessage(Utils.translate("&a/level permission <level> <permission>  &7Sets the required permission to enter the level"));
        sender.sendMessage(Utils.translate("&a/level removepermission <level>  &7Resets the required permission to enter (to null)"));
        sender.sendMessage(Utils.translate("&a/level rank <level> <rank>  &7Sets the required rank for the level"));
        sender.sendMessage(Utils.translate("&a/level makesign <level> &7Places a wall sign on the block you are looking at with the default formatting"));
    }
}