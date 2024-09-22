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

		SquadManager squadManager = Momentum.getSquadManager();
		PlayerStats player = Momentum.getStatsManager().get((Player) sender);
		Squad squad = player.getSquad();

		int n = args.length;
		if (label.equalsIgnoreCase("sqc")) {
			if (squad == null)
				noSquad(sender);
			else if (n == 0)
				player.sendMessage(Utils.translate("&3You have toggled squad chat &b" + (squadManager.toggleSquadChat(player) ? "on" : "off")));
			else {
				String msg = String.format("&9[SqC] &3%s &b%s", player.getDisplayName(), String.join(" ", Arrays.copyOfRange(args, 1, n - 1)));
				squadManager.sendMessage(player, msg, true);
			}
			return true;
		}
		else if (n == 0 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return true;
		}

		// prechecks: s > 0, args[0] != "help", label != "sqc"

		switch (args[0].toLowerCase()) {
			case "create":
				if (squad == null) {
					squadManager.createSquad(player);
					player.sendMessage(Utils.translate("&3Squad has been created"));
				}
				else
					player.sendMessage(Utils.translate("&cYou are already in a squad!"));

				break;
			case "invite":
				if (n != 2)
					sendHelp(sender);
				else if (squad == null)
					noSquad(sender);
				else {
					if (!SquadManager.isLeader(player))
						player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
					else {
						PlayerStats invitee = Momentum.getStatsManager().getByName(args[1]);
						if (invitee != null) {
							squadManager.invite(player, invitee);
							SquadManager.notifyMembers(squad, "&9[SqC] &3" + player.getDisplayName() + " &bhas invited &3" + invitee.getDisplayName() + " &bto the squad");
						}
						else
							player.sendMessage(Utils.translate("&cThat player is not online!"));
					}
				}

				break;
			case "accept":
				if (n != 2) {
					sendHelp(sender);
					break;
				}
				PlayerStats inviter = Momentum.getStatsManager().getByName(args[1]);
				if (inviter == null)
					player.sendMessage(Utils.translate("&cThat player is not online!"));
				else if (inviter.getSquad() == null || !inviter.getSquad().hasInvite(player))
					player.sendMessage(Utils.translate("&cNo incoming invites from &4" + inviter.getName() + " &cwere found"));
				else {
					Squad newSquad = inviter.getSquad();
					boolean sqChat = squadManager.isInSquadChat(player);
					if (squad != null) {
						squadManager.leave(player); // make sure to leave current squad before joining a new one
						SquadManager.notifyMembers(squad, "&9[SqC] &3" + player.getDisplayName() + " &bhas left the squad");
						player.sendMessage(Utils.translate("&3You have left the squad"));
					}

					squadManager.join(newSquad, player);
					SquadManager.notifyMembers(newSquad, "&9[SqC] &3" + player.getDisplayName() + " &bhas joined the squad", player);
					player.sendMessage(Utils.translate("&3You have joined the squad"));

					// preserve squad chat if player leaves and rejoins/joins a new squad
					if (sqChat)
						squadManager.toggleSquadChat(player);
				}

				break;
			case "leave":
				if (squad == null)
					noSquad(sender);
				else if (SquadManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou must relinquish leadership before leaving the squad!"));
				else {
					squadManager.leave(player);
					SquadManager.notifyMembers(squad, "&9[SqC] &3" + player.getDisplayName() + " &bhas left the squad");
					player.sendMessage(Utils.translate("&3You have left the squad"));
				}

				break;
			case "kick":
				if (n != 2)
					sendHelp(sender);
				else if (squad == null)
					noSquad(sender);
				else if (!SquadManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					PlayerStats targetMember = Momentum.getStatsManager().getByName(args[1]);
					if (targetMember == null || !SquadManager.isMember(squad, targetMember))
						player.sendMessage(Utils.translate("&cThat player is not in the squad!"));
					else if (targetMember.equals(player))
						player.sendMessage(Utils.translate("&cYou cannot kick yourself!"));
					else {
						squadManager.kick(targetMember);
						SquadManager.notifyMembers(squad, "&9[SqC] &3" + player.getDisplayName()  + " &bhas kicked &3" + targetMember.getDisplayName() + " &bfrom the squad");
						targetMember.sendMessage("&3You have been kicked from the squad");
					}
				}

				break;
			case "disband":
				if (squad == null)
					noSquad(sender);
				else if (!SquadManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					SquadManager.notifyMembers(squad, "&9[SqC] &3The squad has been disbanded");
					squadManager.disband(squad);
				}

				break;
			case "promote":
				if (n != 2)
					sendHelp(sender);
				else if (squad == null)
					noSquad(sender);
				else if (!SquadManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					PlayerStats targetMember = Momentum.getStatsManager().getByName(args[1]);
					if (targetMember == null || !SquadManager.isMember(squad, targetMember))
						player.sendMessage(Utils.translate("&cThat player is not in the squad!"));
					else if (targetMember.equals(player))
						player.sendMessage(Utils.translate("&cYou cannot promote yourself!"));
					else {
						squadManager.promote(targetMember);
						SquadManager.notifyMembers(squad, "&9[SqC] &3" + targetMember.getDisplayName() + " &bhas been promoted to squad leader");
					}
				}

				break;
			case "chat":
				if (squad == null)
					noSquad(sender);
				else if (n == 1)
					player.sendMessage(Utils.translate("&3You have toggled squad chat &b" + (squadManager.toggleSquadChat(player) ? "on" : "off")));
				else {
					String msg = String.format("&9[SqC] &3%s &b%s", player.getDisplayName(), String.join(" ", Arrays.copyOfRange(args, 1, n - 1)));
					squadManager.sendMessage(player, msg, true);
				}

				break;
			case "chatspy":
				if (!sender.hasPermission("momentum.admin"))
					sendHelp(sender);
				else
					squadManager.toggleSquadChatSpy(player);

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
