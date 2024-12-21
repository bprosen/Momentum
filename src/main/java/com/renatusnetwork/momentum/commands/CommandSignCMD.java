package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.cmdsigns.CmdSignLocation;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSign;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSignManager;
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
        if (a.length == 1) {
            if (a[0].equalsIgnoreCase("list")) {
                showList(sender);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage(Utils.translate("&cInsufficient permissions"));
            return true;
        }
        if (a.length < 2) {
            sendHelp(sender);
            return true;
        }

        CommandSignManager csignManager = Momentum.getCommandSignManager();
        String name = a[1];

        if (a[0].equalsIgnoreCase("create")) {
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
                String cmd = String.join(" ", Arrays.copyOfRange(a, 5, a.length));
                csignManager.addCommandSign(name, cmd, player.getWorld(), x, y, z);
                sender.sendMessage(Utils.translate("&aSuccessfully created command sign at (" + x + ", " + y + ", " + z + ")"));
            }
        } else if (a[0].equalsIgnoreCase("delete")) {
            if (!csignManager.commandSignExists(name)) {
                sender.sendMessage(Utils.translate("&cCommand sign does not exist with that name"));
            } else {
                csignManager.deleteCommandSign(name);
                sender.sendMessage(Utils.translate("&aSuccessfully deleted command sign &2" + name));
            }
        } else if (a[0].equalsIgnoreCase("modify")) {
            if (!csignManager.commandSignExists(name)) {
                sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                return true;
            }

            String cmd = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
            csignManager.updateCommand(name, cmd);
            sender.sendMessage(Utils.translate("&aSuccessfully updated command(s) for &2" + name));
        } else if (a[0].equalsIgnoreCase("broadcast")) {
            if (!csignManager.commandSignExists(name)) {
                sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                return true;
            }
            csignManager.toggleBroadcast(name);
            sender.sendMessage(Utils.translate("&7Toggled broadcast of &a" + name + "&7 to " + (csignManager.getCommandSign(name).isBroadcast() ? "&atrue" : "&cfalse")));
        } else if (a[0].equalsIgnoreCase("title")) {
            if (!csignManager.commandSignExists(name)) {
                sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
                return true;
            }

            String title = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
            csignManager.updateTitle(name, title);
            sender.sendMessage(Utils.translate("&7Set title for &2" + name + "&7 to &a" + title));
        } else {
            sendHelp(sender);
        }

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&2&lCommandSigns Help"));
        sender.sendMessage(Utils.translate("&a/commandsign help  &7Displays this menu"));
        sender.sendMessage(Utils.translate("&a/commandsign list  &7Shows list of all command signs"));
        sender.sendMessage(Utils.translate("&a/commandsign show <name>  &7Displays details of a command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign create <name> <x> <y> <z> [command]  &7Creates uniquely named command sign at supplied integer coordinates"));
        sender.sendMessage(Utils.translate("    &2&ooptional &2[command]  &7aAdds the singular command to the created command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign delete <name> &7Deletes the specified command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign modify <name> <index> <command>  &7Updates a sign's command at the specified index"));
        sender.sendMessage(Utils.translate("&a/commandsign add <name> <command>  &7Adds a command to the command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign remove <name> <index>  &7Removes the command at the specified index from the command sign"));
        sender.sendMessage(Utils.translate("&a/commandsign broadcast <name>  &7Toggles broadcast on a sign"));
        sender.sendMessage(Utils.translate("&a/commandsign title <name> <title>  &7Sets sign title"));
    }

    private static void showList(CommandSender sender) {
        sender.sendMessage(Utils.translate("&7Command Signs"));

        Collection<CommandSign> csigns = Momentum.getCommandSignManager().getCommandSigns();
        for (CommandSign csign : csigns) {
            CmdSignLocation loc = csign.getLocation();
            List<String> cmds = csign.getCommands();
            sender.sendMessage(Utils.translate("&a" + csign.getName() + ": " + loc.getWorld().getName() + " (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")"));
            sender.sendMessage(Utils.translate("  &7Command(s):"));
            for (int i = 0; i < cmds.size(); i++) {
                sender.sendMessage(Utils.translate("    &2&o" + (i + 1) + ". /") + cmds.get(i));
            }
            sender.sendMessage(Utils.translate("  &7Broadcast: " + (csign.isBroadcast() ? "&atrue" : "&cfalse")));
        }
    }
}
