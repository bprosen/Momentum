package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandSignCMD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Utils.translate("&cConsole cannot run this"));
			return true;
		}
		if (!sender.isOp()) {
			sender.sendMessage(Utils.translate("&cInsufficient permissions"));
			return true;
		}
		if (args.length < 4) {
			sendHelp(sender);
			return true;
		}

		Player player = (Player) sender;

		double x;
		double y;
		double z;
		try {
			x = Double.parseDouble(args[1]);
			y = Double.parseDouble(args[2]);
			z = Double.parseDouble(args[3]);
		}
		catch (NumberFormatException ignore) {
			sendHelp(sender);
			return true;
		}

		if (args[0].equalsIgnoreCase("create")) {
			if (StatsDB.hasCommandSign(player.getWorld().getName(), x, y, z))
				sender.sendMessage(Utils.translate("&cCommand sign already exists at that location"));
			else {
				String cmd = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
				StatsDB.insertCommandSign(cmd, player.getWorld().getName(), x, y, z);
				sender.sendMessage(Utils.translate("&aSuccessfully created the command sign at (" + x + ", " + y + ", " + z + ")"));
			}
		} else if (args[0].equalsIgnoreCase("delete")) {
			StatsDB.deleteCommandSign(((Player) sender).getWorld().getName(), x, y, z);
			Momentum.getStatsManager().get(player).unobtainCommandSign(player.getWorld(), x, y, z);
			sender.sendMessage(Utils.translate("&aSuccessfully deleted the command sign at (" + x + ", " + y + ", " + z + ")"));
		} else {
			sendHelp(sender);
			return true;
		}

		return true;
	}

	private static void sendHelp(CommandSender sender) {
		sender.sendMessage(Utils.translate("&7-- Help --"));
		sender.sendMessage(Utils.translate("&a/commandsign help  &7Displays this menu"));
		sender.sendMessage(Utils.translate("&a/commandsign create <x> <y> <z> <command>  &7Creates a command sign at the specified location that executes the specified command"));
		sender.sendMessage(Utils.translate("&a/commandsign delete <x> <y> <z>  &7Deletes the command sign at the specified location"));
		sender.sendMessage(Utils.translate("&7----------"));
	}
}
