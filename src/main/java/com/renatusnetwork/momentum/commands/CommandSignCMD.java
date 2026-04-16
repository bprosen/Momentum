package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.cmdsigns.CmdSignLocation;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSign;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSignManager;
import com.renatusnetwork.momentum.storage.mysql.DatabaseQueries;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CommandSignCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] a) {
        if (!sender.isOp()) {
            sender.sendMessage(Utils.translate("&cInsufficient permissions"));
            return true;
        }

        if (a.length == 1) {
            if (a[0].equalsIgnoreCase("list")) {
                showList(sender);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        if (a.length < 2) {
            sendHelp(sender);
            return true;
        }

        CommandSignManager csignManager = Momentum.getCommandSignManager();
        String name = a[1];

        // switch variable stuff
        int index;
        String cmd;
        boolean result;
        switch (a[0].toLowerCase()) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Utils.translate("&cConsole cannot run this"));
                    return true;
                }
                if (a.length < 5) {
                    sendHelp(sender);
                    return true;
                }

                Player player = (Player) sender;

                int x, y, z;
                try {
                    x = Integer.parseInt(a[2]);
                    y = Integer.parseInt(a[3]);
                    z = Integer.parseInt(a[4]);
                } catch (NumberFormatException ignore) {
                    sendHelp(sender);
                    return true;
                }

                if (csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cCommand sign already exists with that name"));
                } else if (csignManager.commandSignExists(new CmdSignLocation(player.getWorld(), x, y, z))) {
                    sender.sendMessage(Utils.translate("&cCommand sign already exists at that location"));
                } else {
                    csignManager.addCommandSign(name, player.getWorld(), x, y, z);
                    sender.sendMessage(Utils.translate("&aSuccessfully created command sign at &2(" + x + ", " + y + ", " + z + ")"));
                }

                break;
            case "delete":
                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cCommand sign &4" + name + " &cdoes not exist"));
                } else {
                    csignManager.deleteCommandSign(name);
                    sender.sendMessage(Utils.translate("&aSuccessfully deleted command sign &2" + name));
                }

                break;
            case "modify":
                if (a.length < 3) {
                    sendHelp(sender);
                    return true;
                }

                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }

                try {
                    index = Integer.parseInt(a[2]) - 1; // minus one since the displayed list in game is shifted to start at 1 instead of 0 for practicality
                } catch (NumberFormatException ignore) {
                    sendHelp(sender);
                    return true;
                }

                cmd = String.join(" ", Arrays.copyOfRange(a, 3, a.length));
                if (cmd.isEmpty()) {
                    sendHelp(sender);
                    return true;
                }

                if (Utils.containsIgnoreCase(csignManager.getCommandSign(name).getCommands(), cmd)) {
                    sender.sendMessage(Utils.translate("&cCommand already exists in command sign"));
                    return true;
                }

                result = csignManager.updateCommand(name, index, cmd);
                sender.sendMessage(Utils.translate(result ? "&aSuccessfully updated command at index &2" + (index + 1) + " &afor &2" + name : Utils.translate("&cIndex &4" + (index + 1) + " &cdoes not exist for &4" + name)));

                break;
            case "broadcast":
                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }
                csignManager.toggleBroadcast(name);
                sender.sendMessage(Utils.translate("&7Toggled broadcast of &2" + name + "&7 to " + (csignManager.getCommandSign(name).isBroadcast() ? "&atrue" : "&cfalse")));

                break;
            case "title":
                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }

                String title = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
                csignManager.updateTitle(name, title);
                sender.sendMessage(Utils.translate("&7Set title for &2" + name + "&7 to &a" + title));

                break;
            case "show":
                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }

                CommandSign csign = csignManager.getCommandSign(name);
                showInfo(sender, csign);

                break;
            case "add":
                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }

                cmd = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
                if (cmd.isEmpty()) {
                    sendHelp(sender);
                    return true;
                }

                if (Utils.containsIgnoreCase(csignManager.getCommandSign(name).getCommands(), cmd)) {
                    sender.sendMessage(Utils.translate("&cCommand already exists in command sign"));
                    return true;
                }

                csignManager.addCommand(name, cmd);
                sender.sendMessage(Utils.translate("&aSuccessfully added command to &2" + name));

                break;
            case "remove":
                if (a.length < 3) {
                    sendHelp(sender);
                    return true;
                }

                if (!csignManager.commandSignExists(name)) {
                    sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                    return true;
                }

                if (a[2].equalsIgnoreCase("all")) {
                    csignManager.clearCommands(name);
                    sender.sendMessage(Utils.translate("&aSuccessfully removed all commands for &2" + name));
                    break;
                }

                try {
                    index = Integer.parseInt(a[2]) - 1; // minus one since the displayed list in game is shifted to start at 1 instead of 0 for practicality
                } catch (NumberFormatException ignore) {
                    sendHelp(sender);
                    return true;
                }

                result = csignManager.removeCommand(name, index);
                sender.sendMessage(Utils.translate(result ? "&aSuccessfully removed command at index &2" + (index + 1) + " &afor &2" + name : Utils.translate("&cindex &4" + (index + 1) + " &cdoes not exist for &4" + name)));

                break;
            default:
                sendHelp(sender);

                break;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&2&lCommandSigns Help"));
        sender.sendMessage(Utils.translate("&a/commandsign help  &7Displays this menu"));
        sender.sendMessage(Utils.translate("&a/commandsign list  &7Shows list of all command signs"));
        sender.sendMessage(Utils.translate("&a/commandsign show <name>  &7Displays details of a command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign create <name> <x> <y> <z>  &7Creates a uniquely named command sign at supplied integer coordinates"));
        sender.sendMessage(Utils.translate("&a/commandsign delete <name>  &7Deletes the specified command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign modify <name> <index> <command>  &7Updates a sign's command at the specified index"));
        sender.sendMessage(Utils.translate("&a/commandsign add <name> <command>  &7Adds a command to the command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign remove <name> <index|all>  &7Removes the command at the specified index, or all commands, from the command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign broadcast <name>  &7Toggles broadcast on a sign"));
        sender.sendMessage(Utils.translate("&a/commandsign title <name> <title>  &7Sets sign title"));
    }

    // maybe add pages in the future
    private static void showList(CommandSender sender) {
        Collection<CommandSign> csigns = Momentum.getCommandSignManager().getCommandSigns();

        if (csigns.isEmpty()) {
            sender.sendMessage(Utils.translate("&cThere are currently no command signs"));
            return;
        }

        sender.sendMessage(Utils.translate("&7Command Signs"));

        for (CommandSign csign : csigns) {
            CmdSignLocation loc = csign.getLocation();
            List<String> cmds = csign.getCommands();
            sender.sendMessage(Utils.translate("&2" + csign.getName() + "&7: &a" + loc.toString()));
            sender.sendMessage(Utils.translate("  &7Command(s):"));
            for (int i = 0; i < cmds.size(); i++) {
                sender.sendMessage(Utils.translate("    &2&o" + (i + 1) + ". /") + cmds.get(i));
            }
            sender.sendMessage(Utils.translate("  &7Broadcast: " + (csign.isBroadcast() ? "&atrue" : "&cfalse")));
        }
    }

    // probably redundant but nice to have in case the list command gets really long
    private static void showInfo(CommandSender sender, CommandSign csign) {
        sender.sendMessage(Utils.translate("&a" + csign.getName() + ":"));
        sender.sendMessage(Utils.translate("  &7Title: " + csign.getTitle()));
        sender.sendMessage(Utils.translate("  &7Location: &2&o" + csign.getLocation().toString()));
        sender.sendMessage(Utils.translate("  &7Commands:"));
        for (int i = 0; i < csign.getCommands().size(); i++) {
            sender.sendMessage(Utils.translate("    &2&o" + (i + 1) + ". /") + csign.getCommands().get(i));
        }
        sender.sendMessage(Utils.translate("  &7Broadcast: " + (csign.isBroadcast() ? "&atrue" : "&cfalse")));
    }
}
