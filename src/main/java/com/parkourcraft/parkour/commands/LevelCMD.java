package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.*;
import com.parkourcraft.parkour.data.locations.LocationManager;
import com.parkourcraft.parkour.data.locations.LocationsYAML;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.StatsDB;
import com.parkourcraft.parkour.gameplay.LevelHandler;
import com.parkourcraft.parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.parkour.storage.mysql.DatabaseQueries;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp() || !(sender instanceof Player)) {

            LevelManager levelManager = Parkour.getLevelManager();

            if (a.length == 0) {
                sendHelp(sender);
            } else {
                if (a[0].equalsIgnoreCase("show")) { // subcommand: show
                    if (a.length == 2) {
                        sender.sendMessage("unfinished");
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("show"));
                    }
                } else if (a[0].equalsIgnoreCase("list")) { // subcommand: list
                    sender.sendMessage(Utils.translate("&7Levels loaded in: &2" + String.join("&7, &2",
                            levelManager.getNames())));
                } else if (a[0].equalsIgnoreCase("create")) { // subcommand: create
                    if (a.length == 2) {
                        String levelName = a[1].toLowerCase();

                        if (levelName.contains("'"))
                            sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                        else if (levelManager.exists(levelName))
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7already exists"));
                        else {
                            LevelsYAML.create(levelName);
                            sender.sendMessage(Utils.translate("&7Created level &2" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("create"));
                    }
                } else if (a[0].equalsIgnoreCase("delete")) { // subcommand: delete
                    if (a.length == 2) {
                        String levelName = a[1];

                        if (!levelManager.exists(levelName))
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                        else {
                            levelManager.remove(levelName);
                            sender.sendMessage(Utils.translate("&7Deleted level &2" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("delete"));
                    }
                } else if (a[0].equalsIgnoreCase("load")) { // subcommand: load
                    Parkour.getConfigManager().load("levels");
                    sender.sendMessage(Utils.translate("&7Loaded &2levels.yml &7from disk"));
                    levelManager.load();
                    sender.sendMessage(Utils.translate("&7Loaded levels from &2levels.yml&7, &a" +
                                       Parkour.getLevelManager().getNames().size() + " &7total"));

                    // load total completions and leaderboards in async
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            StatsDB.loadTotalCompletions();
                            StatsDB.loadLeaderboards();
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());

                } else if (a[0].equalsIgnoreCase("title")) { //subcommand: title
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();

                        if (levelManager.exists(levelName)) {
                            if (a.length > 2) {
                                String title = "";
                                for (int i = 2; i < a.length; i++)
                                    title = title + " " + a[i];
                                title = title.trim();

                                LevelsYAML.setTitle(levelName, title);
                                sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's title to &2" + title));
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current title: &2" +
                                                   LevelsYAML.getTitle(levelName)));
                                sender.sendMessage(getHelp("title"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("title"));
                    }
                } else if (a[0].equalsIgnoreCase("reward")) { //subcommand: reward
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();
                        Level level = levelManager.get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setReward(Integer.parseInt(a[2]));
                                    LevelsDB.updateReward(level);
                                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's reward to &6" + a[2]));
                                } else {
                                    sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
                                    sender.sendMessage(getHelp("reward"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current reward: &6" + level.getReward()));
                                sender.sendMessage(getHelp("reward"));
                            }
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("reward"));
                    }
                } else if (a[0].equalsIgnoreCase("startloc")) { //subcommand: startloc
                    if (a.length < 2) {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("startloc"));
                    } else {
                        String levelName = a[1].toLowerCase();

                        if (Parkour.getLevelManager().exists(levelName)) {
                            if (sender instanceof Player) {
                                Player player = ((Player) sender).getPlayer();

                                Location playerLocation = player.getLocation();
                                String spawnPositionName = levelName + "-spawn";

                                Parkour.getLocationManager().save(spawnPositionName, playerLocation);
                                LevelsYAML.commit(levelName);

                                sender.sendMessage(Utils.translate("&7Location saved as &2" + spawnPositionName +
                                                   " &7for &2" + levelName));
                            } else {
                                sender.sendMessage(Utils.translate("&4This command can only be run in-game"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                        }
                    }
                } else if (a[0].equalsIgnoreCase("completionloc")) { //subcommand: completionloc
                    if (a.length < 2) {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("completionloc"));
                    } else {
                        String levelName = a[1].toLowerCase();

                        if (Parkour.getLevelManager().exists(levelName)) {
                            if (a.length > 2) {
                                if (a[2].equalsIgnoreCase("default")) {
                                    String completionLocationName = levelName + "-completion";

                                    Parkour.getLocationManager().remove(completionLocationName);
                                    Parkour.getLevelManager().load(levelName);

                                    sender.sendMessage(Utils.translate("&7The completion location for &2" +
                                                       levelName + " &7has been reset to default"));
                                } else {
                                    sender.sendMessage(Utils.translate("&cThe only parameter that can be used is &4default"));
                                    sender.sendMessage(Utils.translate("&7In order to reset the completion " +
                                                       "location, use &/levels completionloc " + levelName + " default"));
                                }
                            } else {
                                if (sender instanceof Player) {
                                    Player player = ((Player) sender).getPlayer();

                                    Location playerLocation = player.getLocation();
                                    String completionLocationName = levelName + "-completion";

                                    Parkour.getLocationManager().save(completionLocationName, playerLocation);
                                    levelManager.load(levelName);

                                    sender.sendMessage(Utils.translate("&7Location saved as &2" +
                                                       completionLocationName + " &7for &2" + levelName));
                                    sender.sendMessage(Utils.translate("&7In order to reset the completion " +
                                                       "location, use &2/levels completionloc " + levelName + "default"));
                                } else {
                                    sender.sendMessage(Utils.translate("&cThis command can only be run in-game"));
                                }
                            }
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                    }
                } else if (a[0].equalsIgnoreCase("message")) { //subcommand: message
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();

                        if (Parkour.getLocationManager().exists(levelName)) {
                            if (a.length > 2) {
                                String message = "";
                                for (int i = 2; i < a.length; i++)
                                    message = message + " " + a[i];
                                message = message.trim();

                                LevelsYAML.setMessage(levelName, message);
                                sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's completion message" +
                                                   " to &2" + message));
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current message: "
                                                  + LevelsYAML.getMessage(levelName)));
                                sender.sendMessage(getHelp("message"));
                            }
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + "&7does not exist"));
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("message"));
                    }
                } else if (a[0].equalsIgnoreCase("completions")) { //subcommand: completions
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();

                        if (levelManager.exists(levelName)) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    LevelsYAML.setMaxCompletions(levelName, Integer.parseInt(a[2]));
                                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's max completions to &2" + a[2]));
                                } else {
                                    sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
                                    sender.sendMessage(getHelp("completions"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current max " +
                                                   "completions: &2" + LevelsYAML.getMaxCompletions(levelName)));
                                sender.sendMessage(getHelp("completions"));
                            }
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + "&7 does not exist"));
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("completions"));
                    }
                } else if (a[0].equalsIgnoreCase("broadcast")) { //subcommand: broadcast
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();

                        if (levelManager.exists(levelName)) {
                            boolean broadcastSetting = LevelsYAML.getBroadcastSetting(levelName);

                            LevelsYAML.setBroadcast(levelName, !broadcastSetting);

                            sender.sendMessage(Utils.translate("&7Broadcast completion for &2" + levelName +
                                               " &7was set to " + (!broadcastSetting)));
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("broadcast"));
                    }
                } else if (a[0].equalsIgnoreCase("requires")) { //subcommand: requires
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();
                        Level level = Parkour.getLevelManager().get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                List<String> requiredLevels = level.getRequiredLevels();

                                if (requiredLevels.contains(a[2])) {
                                    requiredLevels.remove(a[2]);
                                    LevelsYAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(Utils.translate("&7Removed &c" + a[2] + " &7from required" +
                                                       " levels"));
                                } else {
                                    requiredLevels.add(a[2]);
                                    LevelsYAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(Utils.translate("&7Added &2" + a[2] + " &7to required levels"));
                                }
                            }
                            level = levelManager.get(levelName);
                            sender.sendMessage(Utils.translate("&2" + levelName + "&7'srequired levels: &2" +
                                               String.join("&7, &2", level.getRequiredLevels())));

                            if (a.length != 3)
                                sender.sendMessage(getHelp("requires"));
                        } else {
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                        }
                    } else {
                        sender.sendMessage(getHelp("requires"));
                    }
                } else if (a[0].equalsIgnoreCase("removetime") && a.length == 3) {

                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (Utils.isInteger(a[2])) {

                        int row = Integer.parseInt(a[2]);

                        if (level != null) {

                            List<LevelCompletion> completions = level.getLeaderboard();

                            if (row <= completions.size() && row > 0) {
                                int name = row - 1;
                                LevelCompletion levelCompletion = completions.get(name);
                                int time = (int) levelCompletion.getCompletionTimeElapsed();
                                Parkour.getDatabaseManager().add("DELETE FROM completions WHERE level_id=" +
                                        level.getID() + " AND time_taken=" + time);
                                completions.remove(levelCompletion);
                                level.setTotalCompletionsCount(level.getTotalCompletionsCount() - 1);
                                sender.sendMessage(Utils.translate("&4" + levelCompletion.getPlayerName() + "'s" +
                                                   " &ctime has been removed succesfully from &4" + levelName));
                                // run it async 0.25 seconds later so it does it when database has updated
                                new BukkitRunnable() {
                                    public void run() {
                                        StatsDB.loadLeaderboard(level);
                                    }
                                }.runTaskLaterAsynchronously(Parkour.getPlugin(), 5);
                            } else {
                                sender.sendMessage(Utils.translate("&cYou are entering an integer above 9"));
                            }
                        } else {
                                sender.sendMessage(Utils.translate("&7No level named '&c" + levelName + "&7' exists"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&c" + a[2] + " &7is not an integer!"));
                    }
                } else if (a[0].equalsIgnoreCase("modifier")) { //subcommand: modifier
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();
                        Level level = Parkour.getLevelManager().get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setScoreModifier(Integer.parseInt(a[2]));
                                    LevelsDB.updateScoreModifier(level);

                                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's " +
                                                       "score modifier to &6" + a[2]));
                                } else {
                                    sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
                                    sender.sendMessage(getHelp("modifier"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current score modifier:" +
                                                   " &6" + level.getScoreModifier()));
                                sender.sendMessage(getHelp("modifier"));
                            }
                        } else
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("modifier"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("raceset")) {

                    if (!(sender instanceof Player)) {
                        return true;
                    }

                    Player player = (Player) sender;

                    if (a[2].equalsIgnoreCase("player1") || a[2].equalsIgnoreCase("player2")) {
                        String levelName = a[1].toLowerCase();

                        if (levelManager.exists(levelName)) {
                            LevelsYAML.setPlayerRaceLocation(a[2], levelName, player.getLocation());
                            player.sendMessage(Utils.translate("&cYou set the location for &4" + a[2] + " &con level &4" + levelName));
                        } else {
                            sender.sendMessage(Utils.translate("&7The level &c" + levelName + " &7does not exist"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cPlayer must be player1 or player2"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("forcecompletion")) {
                    Player target = Bukkit.getPlayer(a[1]);
                    String levelName = a[2].toLowerCase();

                    if (target == null) {
                        sender.sendMessage(Utils.translate("&4" + target + " &cis not online!"));
                        return true;
                    }

                    PlayerStats playerStats = Parkour.getStatsManager().get(target);

                    if (levelManager.exists(levelName)) {

                        Level level = levelManager.get(levelName);
                        boolean isRankUpLevel = false;

                        if (level.isRankUpLevel())
                            isRankUpLevel = true;

                        LevelHandler.dolevelCompletion(playerStats, target, level, levelName, isRankUpLevel, true);
                        sender.sendMessage(Utils.translate("&7You forced a &c" + level.getFormattedTitle() + " &7Completion for &a" + target.getName()));
                    } else {
                        sender.sendMessage(Utils.translate("&cLevel &4" + levelName + " &cdoes not exist"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("setrespawny")) {
                    String levelName = a[1].toLowerCase();

                    if (Utils.isInteger(a[2])) {
                        // get new y
                        int newY = Integer.parseInt(a[2]);

                        if (levelManager.exists(levelName)) {
                            LevelsYAML.setRespawnY(levelName, newY);
                            sender.sendMessage(Utils.translate("&7You have set &c" +
                                    Parkour.getLevelManager().get(levelName).getFormattedTitle() +
                                    "&7's respawn y to &c" + newY));
                        } else {
                            sender.sendMessage(Utils.translate("&cLevel &4" + levelName + " &cdoes not exist"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("addrating")) {

                    if (!(sender instanceof Player)) {
                        return true;
                    }

                    Player player = (Player) sender;

                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        if (Utils.isInteger(a[2])) {

                            int rating = Integer.parseInt(a[2]);

                            if (rating <= 5 && rating >= 0) {
                                level.addRatingAndCalc(rating);
                                RatingDB.addRating(player, level, rating);
                                sender.sendMessage(Utils.translate("&7You added a rating of &4" + rating
                                        + " &7to &cLevel &a" + level.getFormattedTitle()));
                            } else {
                                sender.sendMessage(Utils.translate("&cRating has to be between &40-5"));
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("removerating")) {

                    String levelName = a[1].toLowerCase();
                    String playerName = a[2];
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        if (RatingDB.hasRatedLevelFromName(playerName, level.getID())) {

                            List<Map<String, String>> ratingResults = DatabaseQueries.getResults(
                                    "ratings",
                                    "rating",
                                    " WHERE player_name='" + playerName + "' AND level_id=" + level.getID()
                            );

                            // loop through and remove, then remove all from database
                            for (Map<String, String> ratingResult : ratingResults)
                                level.removeRatingAndCalc(Integer.parseInt(ratingResult.get("rating")));

                            Parkour.getDatabaseManager().add("DELETE FROM ratings WHERE level_id=" + level.getID()
                                    + " AND player_name='" + playerName + "'");

                            sender.sendMessage(Utils.translate("&cYou removed &4" + playerName + " &crating from &7" + level.getFormattedTitle()));

                        } else {
                            sender.sendMessage(Utils.translate("&4" + playerName + " &chas not rated &4" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("hasrated")) {
                    String levelName = a[1].toLowerCase();
                    String playerName = a[2];
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        if (RatingDB.hasRatedLevelFromName(playerName, level.getID()))

                            sender.sendMessage(Utils.translate("&c" + playerName + " &7has rated &c" +
                                    level.getFormattedTitle() + " &7with a &6" +
                                    RatingDB.getRatingFromName(playerName, level.getID())));

                        else

                            sender.sendMessage(Utils.translate("&c" + playerName + " &7has not rated &c" +
                                    level.getFormattedTitle()));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length >= 2 && a[0].equalsIgnoreCase("listratings")) {
                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        if (a.length == 2) {
                            HashMap<Integer, List<String>> ratings = RatingDB.getAllLevelRaters(level.getID());

                            if (!ratings.isEmpty()) {

                                sender.sendMessage(Utils.translate("&2" + level.getFormattedTitle() + "&7's Ratings"));
                                int totalRatings = 0;

                                // loop through list
                                for (Map.Entry<Integer, List<String>> entry : ratings.entrySet()) {
                                    String msg = " &2" + entry.getKey() + " &7-";
                                    // loop through names
                                    for (String playerName : entry.getValue()) {
                                        if (!entry.getValue().get(entry.getValue().size() - 1).equalsIgnoreCase(playerName))
                                            msg += " &a" + playerName + "&7,";
                                        else
                                            // no comma if last one
                                            msg += " &a" + playerName;

                                        totalRatings++;
                                    }
                                    sender.sendMessage(Utils.translate(msg));
                                }
                                sender.sendMessage(Utils.translate("&a" + totalRatings + " &7Ratings"));
                            } else {
                                sender.sendMessage(Utils.translate("&cNobody has rated this level"));
                            }
                        // if they put the optional specification arg
                        } else if (a.length == 3) {
                            if (Utils.isInteger(a[2])) {
                                int rating = Integer.parseInt(a[2]);
                                // make sure it is between 0 and 5
                                if (rating >= 0 && rating <= 5) {

                                    List<String> ratings = RatingDB.getSpecificLevelRaters(level.getID(), rating);
                                    // if it is empty
                                    if (!ratings.isEmpty()) {

                                        sender.sendMessage(Utils.translate("&7Users who rated &2" +
                                                                level.getFormattedTitle() + " &7a &a" + rating));

                                        String msg = " &2" + rating + " &7-";

                                        for (String playerName : ratings) {
                                            if (!ratings.get(ratings.size() - 1).equalsIgnoreCase(playerName))
                                                msg += " &a" + playerName + "&7,";
                                            else
                                                // no comma if last one
                                                msg += " &a" + playerName;
                                        }
                                        sender.sendMessage(Utils.translate(msg));
                                        sender.sendMessage(Utils.translate("&a" + ratings.size() + " &7Ratings"));
                                    } else {
                                        sender.sendMessage(Utils.translate("&cNobody has rated this level a " + rating));
                                    }
                                } else {
                                    sender.sendMessage(Utils.translate("&cYour rating has to be anywhere from 0 to 5!"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an Integer"));
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("togglewater")) {
                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {

                        if (level.doesLiquidResetPlayer())
                            sender.sendMessage(Utils.translate("&7You toggled off liquid resetting players for level &c" + level.getFormattedTitle()));
                        else
                            sender.sendMessage(Utils.translate("&7You have toggled on liquid resetting players for level &c" + level.getFormattedTitle()));

                        LevelsYAML.toggleWaterReset(levelName);
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("rename")) {
                    String levelName = a[1];
                    String newLevelName = a[2].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        if (!levelManager.exists(newLevelName)) {
                            if (!newLevelName.contains("'")) {
                                // update in yaml and db
                                LevelsDB.updateName(levelName, newLevelName);
                                LevelsYAML.renameLevel(levelName, newLevelName);
                                LocationsYAML.renameLocation(levelName, newLevelName);

                                // update in level cache
                                level.setName(newLevelName);
                                levelManager.getLevels().remove(levelName);
                                levelManager.getLevels().put(newLevelName, level);

                                // update in leveldata cache
                                LevelData levelData = levelManager.getLevelDataCache().get(levelName);
                                levelManager.getLevelDataCache().remove(levelName);
                                levelManager.getLevelDataCache().put(newLevelName, levelData);

                                // remove and add from location cache
                                LocationManager locationManager = Parkour.getLocationManager();
                                if (locationManager.hasCompletionLocation(levelName)) {
                                    locationManager.getLocations().remove(levelName + "-completion");
                                    locationManager.load(newLevelName + "-completion");
                                }
                                if (locationManager.hasSpawnLocation(levelName)) {
                                    locationManager.getLocations().remove(levelName + "-spawn");
                                    locationManager.load(newLevelName + "-spawn");
                                }

                                // run this in async, heavy task and can be in async
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        Parkour.getMenuManager().renameLevel(levelName, newLevelName);
                                    }
                                }.runTaskAsynchronously(Parkour.getPlugin());

                                sender.sendMessage(Utils.translate("&cYou have renamed &4" + levelName + " &cto &4" + newLevelName));
                            } else {
                                sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&4" + newLevelName + " &calready exists"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("toggleelytra")) {
                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        LevelsYAML.toggleElytraLevel(levelName);
                        sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                                "&7's Elytra to " + LevelsYAML.isElytraLevel(levelName)));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("toggledropper")) {
                    String levelName = a[1].toLowerCase();
                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        LevelsYAML.toggleDropperLevel(levelName);
                        sender.sendMessage(Utils.translate("&7You have set &c" + level.getFormattedTitle() +
                                "&7 as a dropper level to &c" + LevelsYAML.isDropperLevel(levelName)));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level name"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("setdropperrespawny")) {
                    String levelName = a[1].toLowerCase();

                    if (Utils.isInteger(a[2])) {
                        // get new y
                        int newY = Integer.parseInt(a[2]);

                        if (levelManager.exists(levelName)) {
                            LevelsYAML.setDropperRespawnY(levelName, newY);
                            sender.sendMessage(Utils.translate("&7You have set &c" +
                                    Parkour.getLevelManager().get(levelName).getFormattedTitle() +
                                    "&7's dropper respawn y to &c" + newY));
                        } else {
                            sender.sendMessage(Utils.translate("&cLevel &4" + levelName + " &cdoes not exist"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
                    }
                } else if (a.length == 3 && a[0].equalsIgnoreCase("delcompletion")) {
                    String playerName = a[1];
                    String levelName = a[2].toLowerCase();

                    Level level = levelManager.get(levelName);

                    if (level != null) {
                        int playerID = StatsDB.getPlayerID(playerName);
                        if (playerID > -1) {
                            PlayerStats playerStats = Parkour.getStatsManager().getByNameIgnoreCase(playerName);

                            if (playerStats.getLevelCompletionsCount(levelName) > 0) {
                                StatsDB.removeCompletions(playerID, level.getID());
                                playerStats.getLevelCompletionsMap().remove(levelName);
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
                            if (playerStats.getCheckpoint() != null && playerStats.getLevel() != null &&
                                    playerStats.getLevel().getName().equalsIgnoreCase(levelName)) {
                                playerStats.resetCheckpoint();
                            }

                        // delete from db
                        Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE level_name='" + levelName + "'");

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
                            if (playerStats.getCheckpoint() != null) {
                                // if they are in level and their level is the same as the target level
                                if (playerStats.getLevel() != null && playerStats.getLevel().getName().equalsIgnoreCase(levelName)) {
                                    playerStats.resetCheckpoint();
                                    Parkour.getDatabaseManager().add("DELETE FROM checkpoints WHERE level_name='" + levelName + "'" +
                                            " AND player_name='" + playerName + "'");
                                    sender.sendMessage(Utils.translate("&cYou deleted &4" + playerName + "&c's checkpoint for &4" + levelName));
                                } else {
                                    sender.sendMessage(Utils.translate("&4" + playerName + " &cis not in " + levelName));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a checkpoint"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&c'&4" + a[0] + "&c' is not a valid parameter"));
                    sendHelp(sender);
                }
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to run that command"));
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&aTo apply changes use &2/level load"));
        sender.sendMessage(Utils.translate("&7Level names are all lowercase"));
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("load"));
        sender.sendMessage(getHelp("remove"));
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("delete"));
        sender.sendMessage(getHelp("title"));
        sender.sendMessage(getHelp("reward"));
        sender.sendMessage(getHelp("startloc"));
        sender.sendMessage(getHelp("completionloc"));
        sender.sendMessage(getHelp("message"));
        sender.sendMessage(getHelp("completions"));
        sender.sendMessage(getHelp("broadcast"));
        sender.sendMessage(getHelp("requires"));
        sender.sendMessage(getHelp("modifier"));
        sender.sendMessage(getHelp("removetime"));
        sender.sendMessage(getHelp("raceset"));
        sender.sendMessage(getHelp("forcecompletion"));
        sender.sendMessage(getHelp("addrating"));
        sender.sendMessage(getHelp("removerating"));
        sender.sendMessage(getHelp("hasrated"));
        sender.sendMessage(getHelp("listratings"));
        sender.sendMessage(getHelp("togglewater"));
        sender.sendMessage(getHelp("rename"));
        sender.sendMessage(getHelp("delcompletion"));
        sender.sendMessage(getHelp("setrespawny"));
        sender.sendMessage(getHelp("setdropperrespawny"));
        sender.sendMessage(getHelp("toggleelytra"));
        sender.sendMessage(getHelp("toggledropper"));
        sender.sendMessage(getHelp("resetcheckpoint"));
        sender.sendMessage(getHelp("resetcheckpoints"));
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "show":
                return Utils.translate("&a/level show <level>  &7Show level information");
            case "list":
                return Utils.translate("&a/level list  &7List levels loaded in memory");
            case "create":
                return Utils.translate("&a/level create <level>  &7Create a level");
            case "load":
                return Utils.translate("&a/level load  &7Loads levels.yml then levels");
            case "delete":
                return Utils.translate("&a/level delete <level>  &7Delete a level");
            case "title":
                return Utils.translate("&a/level title <level> [title]  &7View/Set a level's title");
            case "reward":
                return Utils.translate("&a/level reward <level> [reward]  &7View/Set a level's reward");
            case "startloc":
                return Utils.translate("&a/level startloc <level>  &7Sets the start to your location");
            case "completionloc":
                return Utils.translate("&a/level completionloc <leveL>  &7Sets the completion to your location");
            case "message":
                return Utils.translate("&a/level message <level> [message]  &7View/Set completion message");
            case "completions":
                return Utils.translate("&a/level completions <level> [completions]  &7View/Set max completions");
            case "broadcast":
                return Utils.translate("&a/level broadcast <level>  &7Toggled broadcast completion");
            case "requires":
                return Utils.translate("&a/level requires <level> <level>  &7Add/Remove required level");
            case "modifier":
                return Utils.translate("&a/level modifier <level> [modifier]  &7View/Set Score Modifier");
            case "removetime":
                return Utils.translate("&a/level removetime <level> <leaderboardPlace>  &7Removes a player's time " +
                                      "from a level's leaderboard");
            case "raceset":
                return Utils.translate("&a/level raceset <level> <player1/player2>  &7Sets the race location for player 1 or 2");
            case "forcecompletion":
                return Utils.translate("&a/level forcecompletion <player> <level>  &7Force completion for player");
            case "addrating":
                return Utils.translate("&a/level addrating <level> <rating (0-5)>  &7Adds a rating to a level (ADMIN WAY NOT /rate)");
            case "removerating":
                return Utils.translate("&a/level removerating <level> <playerName>  &7Removes a rating from a level by player name");
            case "hasrated":
                return Utils.translate("&a/level hasrated <level> <playerName>  &7Tells you if someone has rated it and with what rating");
            case "listratings":
                return Utils.translate("&a/level listratings <level> [rating (0-5)] &7Tells you all the ratings for a level with optional 0-5 specification");
            case "togglewater":
                return Utils.translate("&a/level togglewater <level>  &7Toggles the water from respawning you in a level");
            case "rename":
                return Utils.translate("&a/level rename <level> <newLevelName>  &7Renames a level's name to a new name");
            case "delcompletion":
                return Utils.translate("&a/level delcompletion <player> <levelName>  &7Deletes ALL the completions of a player for a level");
            case "setrespawny":
                return Utils.translate("&a/level setrespawny <level> <respawnY>  &7Sets level respawn y");
            case "toggleelytra":
                return Utils.translate("&a/level toggleelytra <level>  &7Sets level elytra");
            case "toggledropper":
                return Utils.translate("&a/level toggledropper <level>  &7Sets level as dropper");
            case "setdropperrespawny":
                return Utils.translate("&a/level setdropperrespawny <level>  &7Sets dropper level respawn y");
            case "resetcheckpoint":
                return Utils.translate("&a/level resetcheckpoint <level> <player>  &7Resets level checkpoint for single player");
            case "resetcheckpoints":
                return Utils.translate("&a/level resetcheckpoints <level>  &7Resets ALL checkpoints for specific level");
        }
        return "";
    }
}
