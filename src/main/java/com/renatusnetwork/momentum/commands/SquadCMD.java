package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.squads.Squad;
import com.renatusnetwork.momentum.data.squads.SquadManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SquadCMD implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			sender.sendMessage("This command cannot be run from console!");
			return true;
		}

		SquadManager sqMgr = Momentum.getSquadManager();
		PlayerStats player = Momentum.getStatsManager().get((Player) sender);
		Squad squad = player.getSquad();

		int n = args.length;
		if (label.equalsIgnoreCase("sqc")) {
			if (squad == null)
				noSquad(sender);
			else if (n == 0) {
				boolean flag = sqMgr.toggleSquadChat(player);
				player.sendMessage(Utils.translate("&3You have toggled squad chat &b" + (flag ? "on" : "off")));
			}
			else {
				String msg = String.format("&9[SqC] &3%s &b%s", player.getDisplayName(), String.join(" ", Arrays.copyOfRange(args, 1, n - 1)));
				sqMgr.sendMessage(player, msg, true);
			}
			return true;
		}
		else if (n == 0 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return true;
		}

		// prechecks: squad != null && s > 0 && !arg[0].equalsIgnoreCase("help")

		switch (args[0].toLowerCase()) {
			case "create":
				break;
			case "invite":
				break;
			case "accept":
				break;
			case "leave":
				break;
			case "kick":
				break;
			case "disband":
				break;
			case "promote":
				break;
			case "chat":
				break;
			case "chatspy":
				break;
		}

		return true;
	}

	private void sendHelp(CommandSender sender) {
		sender.sendMessage(Utils.translate("&9-- Help --"));
		sender.sendMessage(Utils.translate("&9Command Aliases: &b[squad, sq]"));
		sender.sendMessage(Utils.translate("&3/squad [help]  &bDisplays this menu"));
		sender.sendMessage(Utils.translate("&3/squad create  &bcreates a squad"));
		sender.sendMessage(Utils.translate("&3/squad invite <player>  &binvites player to squad"));
		sender.sendMessage(Utils.translate("&3/squad accept <player>  &baccepts squad invite from player"));
		sender.sendMessage(Utils.translate("&3/squad leave  &bleaves current squad"));
		sender.sendMessage(Utils.translate("&3/squad kick <player>  &bkicks player from the squad"));
		sender.sendMessage(Utils.translate("&3/squad disband  &bdisbands squad"));
		sender.sendMessage(Utils.translate("&3/squad promote <player>  &btransfers ownership of squad to player"));
		sender.sendMessage(Utils.translate("&3/squad chat  &btoggles squad chat"));
		sender.sendMessage(Utils.translate("	&3Aliases: &b[sqc]"));
		sender.sendMessage(Utils.translate("&3/squad chat <message> &bsends message to squad chat"));
		sender.sendMessage(Utils.translate("	&3Aliases: &b[sqc <message>]"));
		sender.sendMessage(Utils.translate("&3/squad warp  &bsends all players in squad to leader's level"));
		if (sender.hasPermission("momentum.admin"))
			sender.sendMessage(Utils.translate("&3/squad chatspy  &btoggles squad chat spy"));
		sender.sendMessage(Utils.translate("&9----------"));
	}

	private void noSquad(CommandSender sender) {
		sender.sendMessage(Utils.translate("&cYou are not in a squad!"));
	}
}
