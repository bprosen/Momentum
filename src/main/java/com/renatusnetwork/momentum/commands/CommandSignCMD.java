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

public class CommandSignCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list"))
				showList(sender);
			else
				sendHelp(sender);
			return true;
		}

		if (!sender.isOp()) {
			sender.sendMessage(Utils.translate("&cInsufficient permissions"));
			return true;
		}
		if (args.length < 2) {
			sendHelp(sender);
			return true;
		}

		CommandSignManager csignManager = Momentum.getCommandSignManager();
		String name = args[1];

		if (args[0].equalsIgnoreCase("create")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Utils.translate("&cConsole cannot run this"));
				return true;
			}
			if (args.length < 5) {
				sendHelp(sender);
				return true;
			}

			Player player = (Player) sender;

			int x;
			int y;
			int z;
			try {
				x = Integer.parseInt(args[2]);
				y = Integer.parseInt(args[3]);
				z = Integer.parseInt(args[4]);
			} catch (NumberFormatException ignore) {
				sendHelp(sender);
				return true;
			}

			if (csignManager.commandSignExists(name)) {
				sender.sendMessage(Utils.translate("&cCommand sign already exists with that name"));
			} else if (csignManager.commandSignExists(new CmdSignLocation(player.getWorld(), x, y, z))) {
				sender.sendMessage(Utils.translate("&cCommand sign already exists at that location"));
			} else {
				String cmd = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
				csignManager.addCommandSign(name, cmd, player.getWorld(), x, y, z);
				sender.sendMessage(Utils.translate("&aSuccessfully created command sign at (" + x + ", " + y + ", " + z + ")"));
			}
		} else if (args[0].equalsIgnoreCase("delete")) {
			csignManager.deleteCommandSign(name);
			sender.sendMessage(Utils.translate("&aSuccessfully deleted command sign &2" + name));
		} else if (args[1].equalsIgnoreCase("modify")) {
			if (!csignManager.commandSignExists(name)) {
				sender.sendMessage(Utils.translate("&cNo command sign exists with name &4" + name));
				return true;
			}

			String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
			csignManager.updateCommand(name, cmd);
		} else
			sendHelp(sender);

		return true;
	}

	private static void sendHelp(CommandSender sender) {
		sender.sendMessage(Utils.translate("&7-- Help --"));
		sender.sendMessage(Utils.translate("&a/commandsign help  &7Displays this menu"));
		sender.sendMessage(Utils.translate("&a/commandsign list  &7Shows list of all command signs"));
		sender.sendMessage(Utils.translate("&a/commandsign create <name> <x> <y> <z> <command>  &7Creates uniquely named command sign at supplied integer coordinates"));
		sender.sendMessage(Utils.translate("&a/commandsign delete <name>  &7Deletes the specified command sign"));
		sender.sendMessage(Utils.translate("&a/commandsign modify <name> <command>  &7Updates a sign's command"));
		sender.sendMessage(Utils.translate("&7----------"));
	}

	private static void showList(CommandSender sender) {
		sender.sendMessage(Utils.translate("&7-- <sign_id>: <world>(<x>, <y>, <z>) --"));
		Collection<CommandSign> csigns = Momentum.getCommandSignManager().getCommandSigns();
		for (CommandSign csign : csigns) {
			CmdSignLocation loc = csign.getLocation();
			sender.sendMessage(Utils.translate("&a" + csign.getName() + ": " + loc.getWorld().getName() + "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() +")"));
		}
	}
}
