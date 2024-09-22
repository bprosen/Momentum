package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SquadManager {
	Set<PlayerStats> inSquadChat;
	Set<PlayerStats> inSquadChatSpy;

	public SquadManager() {
		this.inSquadChat = new HashSet<>();
		this.inSquadChatSpy = new HashSet<>();
	}

	public void invite(PlayerStats inviter, PlayerStats invitee) {
		Squad squad = inviter.getSquad();
		if (squad == null) {
			inviter.sendMessage(Utils.translate("&cYou are not in a squad!"));
			return;
		}
		if (!isLeader(inviter)) {
			inviter.sendMessage(Utils.translate("&cOnly the squad leader can send invites!"));
			return;
		}
		if (isMember(squad, invitee)) {
			inviter.sendMessage(Utils.translate("&9" + invitee.getName() + " &3is already in the squad"));
			return;
		}
		if (!squad.addInvite(invitee)) {
			inviter.sendMessage(Utils.translate("&4" + invitee.getName() + " &chas already been invited to the squad!"));
			return;
		}

		squad.addInvite(invitee);
		notifyMembers(squad, Utils.translate("&9" + invitee.getName() + " &3has been invited to the squad"));

		new BukkitRunnable() {
			@Override
			public void run() {
				if (inviter.getSquad().hasInvite(invitee)) {
					inviter.sendMessage(Utils.translate("&9" + invitee.getName() + " &3did not accept the squad invite in time"));
					invitee.sendMessage(Utils.translate("&3You did not accept the squad invite in time"));
				}
			}
		}.runTaskLater(Momentum.getPlugin(), 20 * 30); // 30 seconds to accept invite
	}

	/*
	public void join(Squad squad, PlayerStats newMember) {

	}

	 */

	public static void notifyMembers(Squad squad, String message) {
		squad.getSquadMembers().forEach(member -> member.sendMessage(message));
	}

	public static boolean isLeader(PlayerStats member) {
		Squad squad = member.getSquad();
		return squad != null && squad.getSquadLeader().equals(member);
	}

	public static boolean isMember(Squad squad, PlayerStats member) {
		return squad.getSquadMembers().contains(member);
	}
}
