package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.data.levels.Levels_YAML;
import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.storage.mysql.DataQueries;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Level_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()
                || !(sender instanceof  Player)) {
            if (a.length == 0) {
                sendHelp(sender);
            } else {
                if (a[0].equalsIgnoreCase("show")) { // subcommand: show
                    if (a.length == 2) {
                        sender.sendMessage("unfinished");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("show"));
                    }
                } else if (a[0].equalsIgnoreCase("list")) { // subcommand: list
                    sender.sendMessage(
                            ChatColor.GRAY + "Levels loaded in: "
                                    + ChatColor.GREEN + String.join(
                                    ChatColor.GRAY + ", " + ChatColor.GREEN,
                                    LevelManager.getNames()
                            )
                    );
                } else if (a[0].equalsIgnoreCase("create")) { // subcommand: create
                    if (a.length == 2) {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName))
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " already exists"
                            );
                        else {
                            Levels_YAML.create(levelName);

                            sender.sendMessage(
                                    ChatColor.GRAY + "Created level "
                                            + ChatColor.GREEN + levelName
                            );
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("create"));
                    }
                } else if (a[0].equalsIgnoreCase("delete")) { // subcommand: delete
                    if (a.length == 2) {
                        String levelName = a[1];

                        if (!LevelManager.exists(levelName))
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                        else {
                            LevelManager.remove(levelName);

                            sender.sendMessage(
                                    ChatColor.GRAY + "Deleted level "
                                            + ChatColor.GREEN + levelName
                            );
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("delete"));
                    }
                } else if (a[0].equalsIgnoreCase("load")) { // subcommand: load
                    FileManager.load("levels");
                    sender.sendMessage(
                            ChatColor.GRAY + "Loaded " + ChatColor.GREEN + "levels.yml"
                                    + ChatColor.GRAY + " from disk"
                    );

                    LevelManager.loadAll();
                    sender.sendMessage(
                            ChatColor.GRAY + "Loaded levels from " + ChatColor.GREEN + "levels.yml"
                                    + ChatColor.GRAY + ", "
                                    + ChatColor.GREEN + LevelManager.getNames().size()
                                    + ChatColor.GRAY + " total"
                    );
                } else if (a[0].equalsIgnoreCase("title")) { //subcommand: title
                    if (a.length > 1) {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            if (a.length > 2) {
                                String title = "";
                                for (int i = 2; i < a.length; i++)
                                    title = title + " " + a[i];
                                title = title.trim();

                                Levels_YAML.setTitle(levelName, title);
                                sender.sendMessage(
                                        ChatColor.GRAY + "Set "
                                                + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s title to "
                                                + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
                                                '&',
                                                title
                                        )
                                );
                            } else {
                                sender.sendMessage(
                                        ChatColor.GREEN + levelName + ChatColor.GRAY + "'s current title: "
                                                + ChatColor.translateAlternateColorCodes(
                                                '&',
                                                Levels_YAML.getTitle(levelName)
                                        )
                                );
                                sender.sendMessage(getHelp("title"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("title"));
                    }
                } else if (a[0].equalsIgnoreCase("reward")) { //subcommand: reward
                    if (a.length > 1) {
                        String levelName = a[1];
                        LevelObject level = LevelManager.get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setReward(Integer.parseInt(a[2]));
                                    DataQueries.updateLevelReward(level);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Set "
                                                    + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s reward to "
                                                    + ChatColor.GOLD + a[2]
                                    );
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                                    sender.sendMessage(getHelp("reward"));
                                }
                            } else {
                                sender.sendMessage(
                                        ChatColor.GREEN + levelName + ChatColor.GRAY + "'s current reward: "
                                                + ChatColor.GOLD + level.getReward()
                                );
                                sender.sendMessage(getHelp("reward"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("reward"));
                    }
                } else if (a[0].equalsIgnoreCase("startloc")) { //subcommand: startloc
                    if (a.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("startloc"));
                    } else {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            if (sender instanceof Player) {
                                Player player = ((Player) sender).getPlayer();

                                Location playerLocation = player.getLocation();
                                String spawnPositionName = levelName + "-spawn";

                                Parkour.locations.save(spawnPositionName, playerLocation);
                                Levels_YAML.commit(levelName);

                                sender.sendMessage(
                                        ChatColor.GRAY + "Location saved as "
                                                + ChatColor.GREEN + spawnPositionName
                                                + ChatColor.GRAY + " for "
                                                + ChatColor.GREEN + levelName
                                );
                            } else
                                sender.sendMessage(ChatColor.DARK_RED + "This command can only be run in-game");
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    }
                } else if (a[0].equalsIgnoreCase("completionloc")) { //subcommand: completionloc
                    if (a.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("completionloc"));
                    } else {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            if (a.length > 2) {
                                if (a[2].equalsIgnoreCase("default")) {
                                    String completionLocationName = levelName + "-completion";

                                    Parkour.locations.remove(completionLocationName);
                                    LevelManager.load(levelName);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "The completion location for "
                                                    + ChatColor.GREEN + levelName
                                                    + ChatColor.GRAY + " has been reset to default"
                                    );
                                } else {
                                    sender.sendMessage(
                                            ChatColor.RED + "The only parameter that can be used is "
                                                    + ChatColor.DARK_RED + "default"
                                    );
                                    sender.sendMessage(
                                            ChatColor.GRAY + "In order to reset the completion location use "
                                                    + ChatColor.GREEN + "/levels completionloc "
                                                    + levelName + " default"
                                    );
                                }
                            } else {
                                if (sender instanceof Player) {
                                    Player player = ((Player) sender).getPlayer();

                                    Location playerLocation = player.getLocation();
                                    String completionLocationName = levelName + "-completion";

                                    Parkour.locations.save(completionLocationName, playerLocation);
                                    LevelManager.load(levelName);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Location saved as "
                                                    + ChatColor.GREEN + completionLocationName
                                                    + ChatColor.GRAY + " for "
                                                    + ChatColor.GREEN + levelName
                                    );
                                    sender.sendMessage(
                                            ChatColor.GRAY + "In order to reset the completion location use "
                                                    + ChatColor.GREEN + "/levels completionloc "
                                                    + levelName + " default"
                                    );
                                } else
                                    sender.sendMessage(ChatColor.DARK_RED + "This command can only be run in-game");
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    }
                } else if (a[0].equalsIgnoreCase("message")) { //subcommand: message
                    if (a.length > 1) {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            if (a.length > 2) {
                                String message = "";
                                for (int i = 2; i < a.length; i++)
                                    message = message + " " + a[i];
                                message = message.trim();

                                Levels_YAML.setMessage(levelName, message);
                                sender.sendMessage(
                                        ChatColor.GRAY + "Set "
                                                + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s completion message to "
                                                + ChatColor.RESET + ChatColor.translateAlternateColorCodes(
                                                '&',
                                                message
                                        )
                                );
                            } else {
                                sender.sendMessage(
                                        ChatColor.GREEN + levelName + ChatColor.GRAY + "'s current message: "
                                                + ChatColor.translateAlternateColorCodes(
                                                '&',
                                                Levels_YAML.getMessage(levelName)
                                        )
                                );
                                sender.sendMessage(getHelp("message"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("message"));
                    }
                } else if (a[0].equalsIgnoreCase("completions")) { //subcommand: completions
                    if (a.length > 1) {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    Levels_YAML.setMaxCompletions(levelName, Integer.parseInt(a[2]));
                                    sender.sendMessage(
                                            ChatColor.GRAY + "Set "
                                                    + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s max completions to "
                                                    + ChatColor.GREEN + a[2]
                                    );
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                                    sender.sendMessage(getHelp("completions"));
                                }
                            } else {
                                sender.sendMessage(
                                        ChatColor.GREEN + levelName + ChatColor.GRAY + "'s current max completions: "
                                                + ChatColor.GREEN + Levels_YAML.getMaxCompletions(levelName)
                                );
                                sender.sendMessage(getHelp("completions"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("completions"));
                    }
                } else if (a[0].equalsIgnoreCase("broadcast")) { //subcommand: broadcast
                    if (a.length > 1) {
                        String levelName = a[1];

                        if (LevelManager.exists(levelName)) {
                            boolean broadcastSetting = Levels_YAML.getBroadcastSetting(levelName);

                            Levels_YAML.setBroadcast(levelName, !broadcastSetting);

                            sender.sendMessage(
                                    ChatColor.GRAY + "Broadcast completion for "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " was set to " + (!broadcastSetting)
                            );
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("broadcast"));
                    }
                } else if (a[0].equalsIgnoreCase("requires")) { //subcommand: requires
                    if (a.length > 1) {
                        String levelName = a[1];
                        LevelObject level = LevelManager.get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                List<String> requiredLevels = level.getRequiredLevels();

                                if (requiredLevels.contains(a[2])) {
                                    requiredLevels.remove(a[2]);
                                    Levels_YAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Removed " +
                                                    ChatColor.RED + a[2] +
                                                    ChatColor.GRAY + " from required levels"
                                    );
                                } else {
                                    requiredLevels.add(a[2]);
                                    Levels_YAML.setRequiredLevels(levelName, requiredLevels);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Added " +
                                                    ChatColor.GREEN + a[2] +
                                                    ChatColor.GRAY + " to required levels"
                                    );
                                }

                            }

                            level = LevelManager.get(levelName);

                            sender.sendMessage(
                                    ChatColor.GREEN + levelName +
                                            ChatColor.GRAY + "'s required levels: "
                                            + ChatColor.GREEN + String.join(
                                            ChatColor.GRAY + ", " + ChatColor.GREEN,
                                            level.getRequiredLevels()
                                    )
                            );

                            if (a.length != 3)
                                sender.sendMessage(getHelp("requires"));
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else
                        sender.sendMessage(getHelp("requires"));
                } else if (a[0].equalsIgnoreCase("modifier")) { //subcommand: modifier
                    if (a.length > 1) {
                        String levelName = a[1];
                        LevelObject level = LevelManager.get(levelName);

                        if (level != null) {
                            if (a.length == 3) {
                                if (Utils.isInteger(a[2])) {
                                    level.setScoreModifier(Integer.parseInt(a[2]));
                                    DataQueries.updateLevelScoreModifier(level);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Set "
                                                    + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s score modifier to "
                                                    + ChatColor.GOLD + a[2]
                                    );
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                                    sender.sendMessage(getHelp("modifier"));
                                }
                            } else {
                                sender.sendMessage(
                                        ChatColor.GREEN + levelName + ChatColor.GRAY + "'s current score modifier: "
                                                + ChatColor.GOLD + level.getScoreModifier()
                                );
                                sender.sendMessage(getHelp("modifier"));
                            }
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "Level "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " does not exist"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("modifier"));
                    }
                } else { // subcommand: unknown
                    sender.sendMessage(
                            ChatColor.RED + "'" + ChatColor.DARK_RED + a[0] +
                                    ChatColor.RED + "' is not a valid parameter"
                    );
                    sendHelp(sender);
                }
            }
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to run that command");

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "To apply changes use " + ChatColor.GREEN + "/level load");
        sender.sendMessage(ChatColor.GRAY + "Level names are case sensitive");
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("load"));
        //sender.sendMessage(getHelp("show"));
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
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("show"))
            return ChatColor.GREEN + "/level show <level>" +
                    ChatColor.GRAY + " Show level information";
        else if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/level list" +
                    ChatColor.GRAY + " List levels loaded in memory";
        else if (cmd.equalsIgnoreCase("create"))
            return ChatColor.GREEN + "/level create <level>" +
                    ChatColor.GRAY + " Create a level";
        else if (cmd.equalsIgnoreCase("load"))
            return ChatColor.GREEN + "/level load" +
                    ChatColor.GRAY + " Loads levels.yml, then levels";
        else if (cmd.equalsIgnoreCase("delete"))
            return ChatColor.GREEN + "/level delete <level>" +
                    ChatColor.GRAY + " Delete a level";
        else if (cmd.equalsIgnoreCase("title"))
            return ChatColor.GREEN + "/level title <level> [title]" +
                    ChatColor.GRAY + " View/Set a level's title";
        else if (cmd.equalsIgnoreCase("reward"))
            return ChatColor.GREEN + "/level reward <level> [reward]" +
                    ChatColor.GRAY + " View/Set reward";
        else if (cmd.equalsIgnoreCase("startloc"))
            return ChatColor.GREEN + "/level startloc <level>" +
                    ChatColor.GRAY + " Set the start to your location";
        else if (cmd.equalsIgnoreCase("completionloc"))
            return ChatColor.GREEN + "/level completionloc <level> [default]" +
                    ChatColor.GRAY + " Set respawn to your location";
        else if (cmd.equalsIgnoreCase("message"))
            return ChatColor.GREEN + "/level message <level> [message]" +
                    ChatColor.GRAY + " View/Set completion mesage";
        else if (cmd.equalsIgnoreCase("completions"))
            return ChatColor.GREEN + "/level completions <level> [completions]" +
                    ChatColor.GRAY + " View/Set max completions";
        else if (cmd.equalsIgnoreCase("broadcast"))
            return ChatColor.GREEN + "/level broadcast <level>" +
                    ChatColor.GRAY + " Toggle broadcast completion";
        else if (cmd.equalsIgnoreCase("requires"))
            return ChatColor.GREEN + "/level requires <level> <level>" +
                    ChatColor.GRAY + " Add/Remove required level";
        else if (cmd.equalsIgnoreCase("modifier"))
            return ChatColor.GREEN + "/level modifier <level> [modifier]" +
                    ChatColor.GRAY + " View/Set score modifier";

        return "";
    }
}
