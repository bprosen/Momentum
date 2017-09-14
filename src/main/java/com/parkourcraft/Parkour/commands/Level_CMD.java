package com.parkourcraft.Parkour.commands;


import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.CheckInteger;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;
import com.parkourcraft.Parkour.utils.storage.LocationManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class Level_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

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
                        ChatColor.GREEN + "Levels "
                        + ChatColor.GRAY + String.join(
                                ChatColor.DARK_GRAY + ", " + ChatColor.GRAY,
                                LevelManager.getLevelNames()
                        )
                );
            } else if (a[0].equalsIgnoreCase("create")) { // subcommand: create
                String levelName = a[1];

                if (a.length == 2) {
                    if (Levels_YAML.levelExists(levelName))
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
                String levelName = a[1];

                if (a.length == 2) {
                    if (!Levels_YAML.levelExists(levelName))
                        sender.sendMessage(
                                ChatColor.GRAY + "Level "
                                        + ChatColor.GREEN + levelName
                                        + ChatColor.GRAY + " does not exist"
                        );
                    else {
                        Levels_YAML.delete(levelName);

                        sender.sendMessage(
                                ChatColor.GRAY + "Deleted level "
                                        + ChatColor.GREEN + levelName
                        );
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                    sender.sendMessage(getHelp("create"));
                }
            } else if (a[0].equalsIgnoreCase("load")) { // subcommand: load
                FileManager.load("levels");
                sender.sendMessage(
                        ChatColor.GRAY + "Loaded " + ChatColor.GREEN + "levels.yml"
                        + ChatColor.GRAY + " from disk"
                );

                LevelManager.loadLevels();
                sender.sendMessage(ChatColor.GRAY + "Loaded levels from " + ChatColor.GREEN + "levels.yml");
            } else if (a[0].equalsIgnoreCase("set")) { // subcommand: set
                if (a.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                    sender.sendMessage(getHelp("set"));
                } else {
                    String levelName = a[2];

                    if (Levels_YAML.levelExists(levelName)) {
                        if (a[1].equalsIgnoreCase("title")) {
                            String title = "";
                            for (int i = 3; i < a.length; i++)
                                title = title + " " + a[i];
                            title = title.trim();

                            Levels_YAML.setTitle(levelName, title);
                            sender.sendMessage(
                                    ChatColor.GRAY + "Set "
                                    + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s title to "
                                    + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', title)
                            );
                        } else if (a[1].equalsIgnoreCase("reward")) {
                           if (CheckInteger.check(a[3])) {
                                Levels_YAML.setReward(levelName, Integer.parseInt(a[3]));
                               sender.sendMessage(
                                       ChatColor.GRAY + "Set "
                                        + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s reward to "
                                        + ChatColor.GOLD + a[3]
                               );
                           } else {
                               sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                               sender.sendMessage(getHelp("set"));
                           }
                        } else if (a[1].equalsIgnoreCase("startloc")) {
                            if (sender instanceof Player) {
                                Player player = ((Player) sender).getPlayer();

                                Location playerLocation = player.getLocation();
                                String spawnPositionName = levelName + "-spawn";

                                LocationManager.savePosition(spawnPositionName, playerLocation);
                                Levels_YAML.setStartLocationName(levelName, spawnPositionName);

                                sender.sendMessage(
                                        ChatColor.GRAY + "Location saved as "
                                        + ChatColor.GREEN + spawnPositionName
                                        + ChatColor.GRAY + " for "
                                        + ChatColor.GREEN + levelName
                                );
                            } else
                                sender.sendMessage(ChatColor.DARK_RED + "This command can only be run in-game");
                        } else if (a[1].equalsIgnoreCase("respawnloc")) {
                            if (a.length > 3) {
                                if (a[3].equalsIgnoreCase("default")) {
                                    String respawnPositionName = levelName + "-respawn";

                                    LocationManager.deletePosition(respawnPositionName);
                                    Levels_YAML.setRespawnLocationName(levelName, "default");

                                    sender.sendMessage(
                                            ChatColor.GRAY + "The respawn location for "
                                            + ChatColor.GREEN + levelName
                                            + ChatColor.GRAY + " has been reset"
                                    );
                                } else {
                                    sender.sendMessage(
                                            ChatColor.RED + "The only parameter that can be used is "
                                            + ChatColor.DARK_RED + "default"
                                    );
                                    sender.sendMessage(
                                            ChatColor.GRAY + "In order to reset the respawn location use "
                                            + ChatColor.GREEN + "/levels set respawnloc "
                                            + levelName + " default"
                                    );
                                }
                            } else {
                                if (sender instanceof Player) {
                                    Player player = ((Player) sender).getPlayer();

                                    Location playerLocation = player.getLocation();
                                    String respawnPositionName = levelName + "-respawn";

                                    LocationManager.savePosition(respawnPositionName, playerLocation);
                                    Levels_YAML.setStartLocationName(levelName, respawnPositionName);

                                    sender.sendMessage(
                                            ChatColor.GRAY + "Location saved as "
                                                    + ChatColor.GREEN + respawnPositionName
                                                    + ChatColor.GRAY + " for "
                                                    + ChatColor.GREEN + levelName
                                    );
                                    sender.sendMessage(
                                            ChatColor.GRAY + "In order to reset the respawn location use "
                                                    + ChatColor.GREEN + "/levels set respawnloc "
                                                    + levelName + " default"
                                    );
                                } else
                                    sender.sendMessage(ChatColor.DARK_RED + "This command can only be run in-game");
                            }
                        } else if (a[1].equalsIgnoreCase("message")) {
                            String message = "";
                            for (int i = 3; i < a.length; i++)
                                message = message + " " + a[i];
                            message = message.trim();

                            Levels_YAML.setMessage(levelName, message);
                            sender.sendMessage(
                                    ChatColor.GRAY + "Set "
                                            + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s completion message to "
                                            + ChatColor.RESET
                                            + ChatColor.translateAlternateColorCodes('&', message)
                            );
                        } else if (a[1].equalsIgnoreCase("completions")) {
                            if (CheckInteger.check(a[3])) {
                                Levels_YAML.setMaxCompletions(levelName, Integer.parseInt(a[3]));
                                sender.sendMessage(
                                        ChatColor.GRAY + "Set "
                                        + ChatColor.GREEN + levelName + ChatColor.GRAY + "'s max completions to "
                                        + ChatColor.GREEN + a[3]
                                );
                            } else {
                                sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                                sender.sendMessage(getHelp("set"));
                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                            sender.sendMessage(getHelp("set"));
                        }
                    } else
                        sender.sendMessage(
                                ChatColor.GRAY + "Level "
                                        + ChatColor.GREEN + levelName
                                        + ChatColor.GRAY + " does not exist"
                        );
                }
            } else { // subcommand: unknown
                sender.sendMessage(
                        ChatColor.RED + "'" + ChatColor.DARK_RED + a[0] +
                                ChatColor.RED + "' is not a valid parameter"
                );
                sendHelp(sender);
            }
        }

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "To apply changes use " + ChatColor.GREEN + "/level load");
        sender.sendMessage(ChatColor.GRAY + "Level names are case sensitive");
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("load"));
        sender.sendMessage(getHelp("show"));
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("delete"));
        sender.sendMessage(getHelp("set"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("show"))
            return ChatColor.GREEN + "/level show <level>" +
                    ChatColor.GRAY + " Shows level information";
        else if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/level list" +
                    ChatColor.GRAY + " Lists the configured levels";
        else if (cmd.equalsIgnoreCase("create"))
            return ChatColor.GREEN + "/level create <level>" +
                    ChatColor.GRAY + " Creates a level";
        else if (cmd.equalsIgnoreCase("load"))
            return ChatColor.GREEN + "/level load" +
                    ChatColor.GRAY + " Loads levels.yml, then levels";
        else if (cmd.equalsIgnoreCase("delete"))
            return ChatColor.GREEN + "/level delete <level>" +
                    ChatColor.GRAY + " Deletes a level";
        else if (cmd.equalsIgnoreCase("set"))
            return ChatColor.GREEN + "/level set "
                    + ChatColor.translateAlternateColorCodes(
                            '&',
                    "<title &2/ &areward &2/ &astartloc &2/ &arespawnloc &2/ &amessage &2/ &acompletions>"
                            + " <level> [value]"
            )
                    + ChatColor.GRAY + " Set level values";
        return "";
    }
}
