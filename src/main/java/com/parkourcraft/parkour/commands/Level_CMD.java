package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.LevelObject;
import com.parkourcraft.parkour.data.levels.Levels_DB;
import com.parkourcraft.parkour.data.levels.Levels_YAML;
import com.parkourcraft.parkour.data.stats.LevelCompletion;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Level_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp() || !(sender instanceof  Player)) {
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
                                       Parkour.getLevelManager().getNames())));
                } else if (a[0].equalsIgnoreCase("create")) { // subcommand: create
                    if (a.length == 2) {
                        String levelName = a[1].toLowerCase();

                        if (levelName.contains("'"))
                            sender.sendMessage(Utils.translate("&7Please do not use ' ..."));
                        else if (Parkour.getLevelManager().exists(levelName))
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7already exists"));
                        else {
                            Levels_YAML.create(levelName);
                            sender.sendMessage(Utils.translate("&7Created level &2" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("create"));
                    }
                } else if (a[0].equalsIgnoreCase("delete")) { // subcommand: delete
                    if (a.length == 2) {
                        String levelName = a[1].toLowerCase();

                        if (!Parkour.getLevelManager().exists(levelName))
                            sender.sendMessage(Utils.translate("&7Level &2" + levelName + " &7does not exist"));
                        else {
                            Parkour.getLevelManager().remove(levelName);
                            sender.sendMessage(Utils.translate("&7Deleted level &2" + levelName));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("delete"));
                    }
                } else if (a[0].equalsIgnoreCase("load")) { // subcommand: load
                    Parkour.getConfigManager().load("levels");
                    sender.sendMessage(Utils.translate("&7Loaded &2levels.yml &7from disk"));
                    Parkour.getLevelManager().load();
                    sender.sendMessage(Utils.translate("&7Loaded levels from &2levels.yml&7, &a" +
                                       Parkour.getLevelManager().getNames().size() + " &7total"));
                } else if (a[0].equalsIgnoreCase("title")) { //subcommand: title
                    if (a.length > 1) {
                        String levelName = a[1].toLowerCase();

                        if (Parkour.getLevelManager().exists(levelName)) {
                            if (a.length > 2) {
                                String title = "";
                                for (int i = 2; i < a.length; i++)
                                    title = title + " " + a[i];
                                title = title.trim();

                                Levels_YAML.setTitle(levelName, title);
                                sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's title to &2title"));
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + " &7's current title: &2" +
                                                   Levels_YAML.getTitle(levelName)));
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
                        LevelObject level = Parkour.getLevelManager().get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setReward(Integer.parseInt(a[2]));
                                    Levels_DB.updateReward(level);
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
                                Levels_YAML.commit(levelName);

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
                                    Parkour.getLevelManager().load(levelName);

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

                                Levels_YAML.setMessage(levelName, message);
                                sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's completion message" +
                                                   " to &2" + message));
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current message: "
                                                  + Levels_YAML.getMessage(levelName)));
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

                        if (Parkour.getLevelManager().exists(levelName)) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    Levels_YAML.setMaxCompletions(levelName, Integer.parseInt(a[2]));
                                    sender.sendMessage(Utils.translate("&7Set &2" + levelName + "&7's max completions to &2" + a[2]));
                                } else {
                                    sender.sendMessage(Utils.translate("&cIncorrect parameters, must enter integer"));
                                    sender.sendMessage(getHelp("completions"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&2" + levelName + "&7's current max " +
                                                   "completions: &2" + Levels_YAML.getMaxCompletions(levelName)));
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

                        if (Parkour.getLevelManager().exists(levelName)) {
                            boolean broadcastSetting = Levels_YAML.getBroadcastSetting(levelName);

                            Levels_YAML.setBroadcast(levelName, !broadcastSetting);

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
                        LevelObject level = Parkour.getLevelManager().get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                List<String> requiredLevels = level.getRequiredLevels();

                                if (requiredLevels.contains(a[2])) {
                                    requiredLevels.remove(a[2]);
                                    Levels_YAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(Utils.translate("&7Removed &c" + a[2] + " &7from required" +
                                                       " levels"));
                                } else {
                                    requiredLevels.add(a[2]);
                                    Levels_YAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(Utils.translate("&7Added &2" + a[2] + " &7to required levels"));
                                }
                            }
                            level = Parkour.getLevelManager().get(levelName);
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
                    LevelObject levelObject = Parkour.getLevelManager().get(levelName);

                    if (Utils.isInteger(a[2])) {

                        int row = Integer.parseInt(a[2]);

                        if (levelObject != null) {

                            List<LevelCompletion> completions = levelObject.getLeaderboard();

                            if (row <= completions.size() && row > 0) {
                                int name = row - 1;
                                LevelCompletion levelCompletion = completions.get(name);
                                int time = (int) levelCompletion.getCompletionTimeElapsed();
                                Parkour.getDatabaseManager().add("DELETE FROM completions WHERE level_id=" +
                                        levelObject.getID() + " AND time_taken=" + time);
                                completions.remove(levelCompletion);
                                levelObject.setTotalCompletionsCount(levelObject.getTotalCompletionsCount() - 1);
                                sender.sendMessage(Utils.translate("&4" + levelCompletion.getPlayerName() + "'s" +
                                                   " &ctime has been removed succesfully from &4" + levelName));
                                // run it async 0.25 seconds later so it does it when database has updated
                                new BukkitRunnable() {
                                    public void run() {
                                        Stats_DB.loadLeaderboard(levelObject);
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
                        LevelObject level = Parkour.getLevelManager().get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setScoreModifier(Integer.parseInt(a[2]));
                                    Levels_DB.updateScoreModifier(level);

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
                } else if (a.length == 2 && a[0].equalsIgnoreCase("addleaderboard")) {

                    String levelName = a[1].toLowerCase();

                    if (Parkour.getLevelManager().exists(levelName)) {
                        if (!Parkour.getLevelManager().getEnabledLeaderboards().contains(levelName)) {
                            Parkour.getLevelManager().getEnabledLeaderboards().add(levelName);
                            Stats_DB.loadLeaderboard(Parkour.getLevelManager().get(levelName));

                            Parkour.getConfigManager().get("levels").set("leaderboard.levels", Parkour.getLevelManager().getEnabledLeaderboards());
                            Parkour.getConfigManager().save("levels");
                            Parkour.getConfigManager().load("levels");

                            sender.sendMessage(Utils.translate("&7You succesfully enabled &c&l" + levelName + "&7's Leaderboard"));
                        } else {
                            sender.sendMessage(Utils.translate("&7This level already has a leaderboard enabled!"));
                        }
                    } else {
                            sender.sendMessage(Utils.translate("&c&l" + levelName + " &7is not a level!"));
                    }
                } else if (a.length == 2 && a[0].equalsIgnoreCase("removeleaderboard")) {

                    String levelName = a[1].toLowerCase();

                    if (Parkour.getLevelManager().exists(levelName)) {
                        if (Parkour.getLevelManager().getEnabledLeaderboards().contains(levelName)) {
                            Parkour.getLevelManager().getEnabledLeaderboards().remove(levelName);

                            Parkour.getConfigManager().get("levels").set("leaderboard.levels", Parkour.getLevelManager().getEnabledLeaderboards());
                            Parkour.getConfigManager().save("levels");
                            Parkour.getConfigManager().load("levels");

                            sender.sendMessage(Utils.translate("&7You succesfully disabled &c&l" + levelName + "&7's Leaderboard"));
                        } else {
                            sender.sendMessage(Utils.translate("&7This level does not have a leaderboard enabled!"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&c&l" + levelName + " &7is not a level!"));
                    }
                } else { // subcommand: unknown
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
        sender.sendMessage(getHelp("addleaderboard"));
        sender.sendMessage(getHelp("removeleaderboard"));
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
            case "addleaderboard":
                return Utils.translate("&a/level addleaderboard <level> &7Add a leaderboard to cache");
            case "removeleaderboard":
                return Utils.translate("&a/level removeleaderboard <level> &7Remove a leaderboard from cache");
        }
        return "";
    }
}
