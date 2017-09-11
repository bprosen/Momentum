package com.parkourcraft.Parkour.commands;


import com.parkourcraft.Parkour.levels.LevelManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.CheckInteger;
import com.parkourcraft.Parkour.utils.storage.Levels_YAML;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
                if (a.length == 2) {
                    if (Levels_YAML.levelExists(a[1]))
                        sender.sendMessage(
                                ChatColor.GRAY + "Level "
                                + ChatColor.GREEN + a[1]
                                + ChatColor.GRAY + " already exists"
                        );
                    else {
                        Levels_YAML.create(a[1]);
                        sender.sendMessage(
                                ChatColor.GRAY + "Created level "
                                        + ChatColor.GREEN + a[1]
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
                    if (Levels_YAML.levelExists(a[2])) {
                        if (a[1].equalsIgnoreCase("title")) {
                            String title = "";
                            for (int i = 3; i < a.length; i++)
                                title = title + " " + a[i];
                            title = title.trim();

                            Levels_YAML.setTitle(a[2], title);
                            sender.sendMessage(
                                    ChatColor.GRAY + "Set "
                                    + ChatColor.GREEN + a[2] + ChatColor.GRAY + "'s title to "
                                    + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', title)
                            );
                        } else if (a[1].equalsIgnoreCase("reward")) {
                           if (CheckInteger.check(a[3])) {
                                Levels_YAML.setReward(a[2], Integer.parseInt(a[3]));
                               sender.sendMessage(
                                       ChatColor.GRAY + "Set "
                                        + ChatColor.GREEN + a[2] + ChatColor.GRAY + "'s reward to "
                                        + ChatColor.GOLD + a[3]
                               );
                           } else {
                               sender.sendMessage(ChatColor.RED + "Incorrect parameters, must enter integer");
                               sender.sendMessage(getHelp("set"));
                           }
                        } else if (a[1].equalsIgnoreCase("startloc")) {
                            sender.sendMessage("unfinished");
                        } else if (a[1].equalsIgnoreCase("respawnloc")) {
                            sender.sendMessage("unfinished");
                        } else if (a[1].equalsIgnoreCase("message")) {
                            String message = "";
                            for (int i = 3; i < a.length; i++)
                                message = message + " " + a[i];
                            message = message.trim();

                            Levels_YAML.setMessage(a[2], message);
                            sender.sendMessage(
                                    ChatColor.GRAY + "Set "
                                            + ChatColor.GREEN + a[2] + ChatColor.GRAY + "'s completion message to "
                                            + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', message)
                            );
                        } else if (a[1].equalsIgnoreCase("completions")) {
                            if (CheckInteger.check(a[3])) {
                                Levels_YAML.setMaxCompletions(a[2], Integer.parseInt(a[3]));
                                sender.sendMessage(
                                        ChatColor.GRAY + "Set "
                                        + ChatColor.GREEN + a[2] + ChatColor.GRAY + "'s max completions to "
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
                                        + ChatColor.GREEN + a[2]
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
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("load"));
        sender.sendMessage(getHelp("show"));
        sender.sendMessage(getHelp("create"));
        sender.sendMessage(getHelp("set"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("show"))
            return ChatColor.GREEN + "/level show <level>" +
                    ChatColor.GRAY + " Show level information";
        else if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/level list" +
                    ChatColor.GRAY + " List the configured levels";
        else if (cmd.equalsIgnoreCase("create"))
            return ChatColor.GREEN + "/level create <level>" +
                    ChatColor.GRAY + " Create a level";
        else if (cmd.equalsIgnoreCase("load"))
            return ChatColor.GREEN + "/level load" +
                    ChatColor.GRAY + " Loads levels.yml, then levels";
        else if (cmd.equalsIgnoreCase("set"))
            return ChatColor.GREEN + "/level set "
                    + ChatColor.translateAlternateColorCodes(
                            '&',
                    "<title &2/ &areward &2/ &astartloc &2/ &arespawnloc &2/ &amessage &2/ &acompletions>"
                            + " <level> <value>"
            )
                    + ChatColor.GRAY + " Set level values";
        return "";
    }
}
