package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSignManager;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

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
		String signID = args[1];

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

			double x;
			double y;
			double z;
			try {
				x = Double.parseDouble(args[2]);
				y = Double.parseDouble(args[3]);
				z = Double.parseDouble(args[4]);
			}
			catch (NumberFormatException ignore) {
				sendHelp(sender);
				return true;
			}

			if (csignManager.commandSignExists(signID)) {
				sender.sendMessage(Utils.translate("&cCommand sign already exists with that id"));
			} else if (csignManager.commandSignExists(csignManager.getSignIDFromLocation(new Location(player.getWorld(), x, y, z)))) {
				sender.sendMessage(Utils.translate("&cCommand sign already exists at that location"));
			} else {
				String cmd = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
				csignManager.addCommandSign(signID, cmd, player.getWorld(), x, y, z);
				sender.sendMessage(Utils.translate("&aSuccessfully created the command sign at (" + x + ", " + y + ", " + z + ")"));
			}
		} else if (args[0].equalsIgnoreCase("delete")) {
			csignManager.deleteCommandSign(signID);
			sender.sendMessage(Utils.translate("&aSuccessfully deleted command sign &2" + signID));
		} else {
			sendHelp(sender);
			return true;
		}

		return true;
	}

	private static void sendHelp(CommandSender sender) {
		sender.sendMessage(Utils.translate("&7-- Help --"));
		sender.sendMessage(Utils.translate("&a/commandsign help  &7Displays this menu"));
		sender.sendMessage(Utils.translate("&a/commandsign list  &7Shows list of all command signs"));
		sender.sendMessage(Utils.translate("&a/commandsign create <x> <y> <z> <command>  &7Creates a command sign at the specified location that executes the specified command"));
		sender.sendMessage(Utils.translate("&a/commandsign delete <x> <y> <z>  &7Deletes the command sign at the specified location"));
		sender.sendMessage(Utils.translate("&7----------"));
	}

	private static void showList(CommandSender sender) {
		sender.sendMessage(Utils.translate("&7-- <sign_id>: <world>(<x>, <y>, <z>) --"));
		Map<String, String> cmdSigns = Momentum.getCommandSignManager().getCommandSigns();
		for (Map.Entry<String, String> e : cmdSigns.entrySet())
			sender.sendMessage(Utils.translate("&a" + e.getKey() + ": " + e.getValue()));
	}
}
