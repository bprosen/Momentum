package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.squads.Squad;
import com.renatusnetwork.momentum.data.squads.SquadsManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SquadsCMD implements CommandExecutor {
	private final String NO_SQUAD_MSG = Utils.translate("&cYou are not in a squad!");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			sender.sendMessage("This command cannot be run from console!");
			return true;
		}

		SquadsManager squadsManager = Momentum.getSquadsManager();
		PlayerStats player = Momentum.getStatsManager().get((Player) sender);
		Squad squad = player.getSquad();

		int n = args.length;
		if (label.equalsIgnoreCase("sqc")) {
			if (!player.inSquad())
				sender.sendMessage(NO_SQUAD_MSG);
			else if (n == 0)
				player.sendMessage(Utils.translate("&3You have toggled &9&lSquad Chat &b" + (squadsManager.toggleSquadChat(player) ? "on" : "off")));
			else {
				String msg = String.format("&9SC &3%s &b%s", player.getDisplayName(), String.join(" ", Arrays.copyOfRange(args, 0, n)));
				squadsManager.sendMessage(player, msg, true);
			}
			return true;
		}
		else if (n == 0 || args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return true;
		}

		// true conditions if continued:
		// s > 0, args[0] != null, args[0] != "help", label != "sqc"

		switch (args[0].toLowerCase()) {
			case "list":
				if (squad == null)
					sender.sendMessage(NO_SQUAD_MSG);
				else {
					player.sendMessage(Utils.translate("&9Squad Members:"));
					squadsManager.getSquadMembers(squad).keySet().forEach(member -> player.sendMessage(Utils.translate("&9Sq -" + member.getDisplayName())));
				}

				break;
			case "invite":
				if (n != 2) {
					sendHelp(sender);
					break;
				}
				else if (!player.inSquad()) {
					squadsManager.createSquad(player);
					squad = player.getSquad();
				}
				else if (!SquadsManager.isLeader(player)) {
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
					break;
				}

				PlayerStats invitee = Momentum.getStatsManager().getByName(args[1]);
				if (invitee == null)
					player.sendMessage(Utils.translate("&cThat player is not online!"));
				else if (SquadsManager.isMember(squad, invitee))
					player.sendMessage(Utils.translate("&cThat player is already in the squad!"));
				else if (squad.hasInvite(invitee))
					player.sendMessage(Utils.translate("&cThat player has already been invited!"));
				else {
					squadsManager.invite(player, invitee);
					SquadsManager.notifyMembers(squad, "&9SC &3" + player.getDisplayName() + " &bhas invited &3" + invitee.getDisplayName() + " &bto the squad");

					TextComponent component = new TextComponent(TextComponent.fromLegacyText(Utils.translate("&3Run &b/squad accept " + player.getName() + " &3or &bClick here to accept")));
					component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Utils.translate("&9Click to accept!"))));
					component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/squad accept " + player.getName()));

					invitee.sendMessage(Utils.translate("&9" + player.getDisplayName() + " &3has invited you to join their squad"));
					invitee.getPlayer().spigot().sendMessage(component);
					invitee.sendMessage(Utils.translate("&3You have &b30 seconds &3to accept"));
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
				else if (!inviter.inSquad() || !inviter.getSquad().hasInvite(player))
					player.sendMessage(Utils.translate("&cNo incoming invites from &4" + inviter.getName() + " &cwere found"));
				else {
					Squad newSquad = inviter.getSquad();
					boolean sqChat = squadsManager.isInSquadChat(player);
					if (player.inSquad()) {
						squadsManager.leave(player); // make sure to leave current squad before joining a new one
						SquadsManager.notifyMembers(squad, "&9SC &3" + player.getDisplayName() + " &bhas left the squad");
						player.sendMessage(Utils.translate("&3You have left the squad"));
					}

					squadsManager.join(newSquad, player);
					SquadsManager.notifyMembers(newSquad, "&9SC &3" + player.getDisplayName() + " &bhas joined the squad", player);
					player.sendMessage(Utils.translate("&3You have joined the squad"));

					// preserve squad chat if player leaves and rejoins/joins a new squad
					if (sqChat)
						squadsManager.toggleSquadChat(player);
				}

				break;
			case "leave":
				if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else {
					boolean leader = SquadsManager.isLeader(player);
					squadsManager.leave(player);
					SquadsManager.notifyMembers(squad, "&9SC &3" + player.getDisplayName() + " &bhas left the squad");
					player.sendMessage(Utils.translate("&3You have left the squad"));
					if (squad.size() <= 1) {
						SquadsManager.notifyMembers(squad, "&3The squad has been disbanded because all players left");
						squadsManager.disband(squad);
					} else if (leader) {
						PlayerStats newLeader = squadsManager.getOldestMember(squad, player);
						squadsManager.promote(newLeader);
						SquadsManager.notifyMembers(squad, "&9SC " + newLeader.getDisplayName() + " &bhas been promoted to squad leader");
					}
				}

				break;
			case "kick":
				if (n != 2)
					sendHelp(sender);
				else if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else if (!SquadsManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					PlayerStats targetMember = Momentum.getStatsManager().getByName(args[1]);
					if (targetMember == null || !SquadsManager.isMember(squad, targetMember))
						player.sendMessage(Utils.translate("&cThat player is not in the squad!"));
					else if (targetMember.equals(player))
						player.sendMessage(Utils.translate("&cYou cannot kick yourself!"));
					else {
						squadsManager.kick(targetMember);
						SquadsManager.notifyMembers(squad, "&9SC &3" + player.getDisplayName()  + " &bhas kicked &3" + targetMember.getDisplayName() + " &bfrom the squad");
						targetMember.sendMessage(Utils.translate("&3You have been kicked from the squad"));
					}
				}

				break;
			case "disband":
				if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else if (!SquadsManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					SquadsManager.notifyMembers(squad, "&3The squad has been disbanded");
					squadsManager.disband(squad);
				}

				break;
			case "promote":
				if (n != 2)
					sendHelp(sender);
				else if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else if (!SquadsManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else {
					PlayerStats targetMember = Momentum.getStatsManager().getByName(args[1]);
					if (targetMember == null || !SquadsManager.isMember(squad, targetMember))
						player.sendMessage(Utils.translate("&cThat player is not in the squad!"));
					else if (targetMember.equals(player))
						player.sendMessage(Utils.translate("&cYou cannot promote yourself!"));
					else {
						squadsManager.promote(targetMember);
						SquadsManager.notifyMembers(squad, "&9SC &3" + targetMember.getDisplayName() + " &bhas been promoted to squad leader");
					}
				}

				break;
			case "chat":
				if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else if (n == 1)
					player.sendMessage(Utils.translate("&3You have toggled &9&lSquad Chat &b" + (squadsManager.toggleSquadChat(player) ? "on" : "off")));
				else {
					String msg = String.format("&9SC &3%s &b%s", player.getDisplayName(), String.join(" ", Arrays.copyOfRange(args, 1, n)));
					squadsManager.sendMessage(player, msg, true);
				}

				break;
			case "chatspy":
				if (!sender.hasPermission("momentum.admin"))
					sendHelp(sender);
				else
					player.sendMessage(Utils.translate("&3You have toggled &9&lSquad ChatSpy &b" + (squadsManager.toggleSquadChatSpy(player) ? "on" : "off")));

				break;
			case "warp":
				Level level = player.getLevel();

				if (!player.inSquad())
					sender.sendMessage(NO_SQUAD_MSG);
				else if (!SquadsManager.isLeader(player))
					player.sendMessage(Utils.translate("&cYou are not the squad leader!"));
				else if (squad.hasWarpCooldown())
					player.sendMessage(Utils.translate("&cWarp is on cooldown"));
				else if (player.getPlayer().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world))
					player.sendMessage(Utils.translate("&cYou cannot warp to a plot!"));
				else if (!player.inLevel())
					player.sendMessage(Utils.translate("&cYou are not in a level!"));
				else if (level.isAscendance())
					player.sendMessage(Utils.translate("&cYou cannot warp to ascendance!"));
				else if (level.isEventLevel() || level.isRaceLevel())
					player.sendMessage(Utils.translate("&cYou cannot warp to a race or event!"));
				else {
					// notify first so failure messages send to individual players after
					SquadsManager.notifyMembers(squad, "&9SC &3" + player.getDisplayName() + " &bhas warped to " + level.getFormattedTitle());
					squadsManager.warpMembers(player);

					squadsManager.notifySqChatSpies(String.format("&3%s &9has warped their party to &3%s", player.getDisplayName(), level.getFormattedTitle()));
				}

				break;
			default:
				sendHelp(sender);
		}

		return true;
	}

	private void sendHelp(CommandSender sender) {
		sender.sendMessage(Utils.translate("&9&lSquads Help"));
		sender.sendMessage(Utils.translate("&3/squads [help]  &bdisplays this menu"));
		sender.sendMessage(Utils.translate("&3/squads list  &blists all players in squad"));
		sender.sendMessage(Utils.translate("&3/squads invite <player>  &binvites player to squad (creates squad if not in one)"));
		sender.sendMessage(Utils.translate("&3/squads accept <player>  &baccepts squad invite from player"));
		sender.sendMessage(Utils.translate("&3/squads leave  &bleaves current squad"));
		sender.sendMessage(Utils.translate("&3/squads kick <player>  &bkicks player from the squad"));
		sender.sendMessage(Utils.translate("&3/squads disband  &bdisbands squad"));
		sender.sendMessage(Utils.translate("&3/squads promote <player>  &btransfers ownership of squad to player"));
		sender.sendMessage(Utils.translate("&3/squads chat  &btoggles squad chat"));
		sender.sendMessage(Utils.translate("&3/sqc  &btoggles squad chat"));
		sender.sendMessage(Utils.translate("&3/squads chat <message>  &bsends message to squad chat"));
		sender.sendMessage(Utils.translate("&3/sqc <message>  &bsends message to squad chat"));
		sender.sendMessage(Utils.translate("&3/squads warp  &bsends all players in squad to leader's level"));
		if (sender.hasPermission("momentum.admin"))
			sender.sendMessage(Utils.translate("&3/squads chatspy  &btoggles squad chat spy"));
	}
}
