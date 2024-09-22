package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SquadManager {
	private final Set<PlayerStats> inSquadChat;
	private final Set<PlayerStats> inSquadChatSpy;

	public SquadManager() {
		this.inSquadChat = new HashSet<>();
		this.inSquadChatSpy = new HashSet<>();
	}

	public void createSquad(PlayerStats leader) {
		Squad squad = Squad.Builder.create().setLeader(leader).build();
		Momentum.getStatsManager().updateSquad(leader, squad);
	}

	public void invite(PlayerStats inviter, PlayerStats invitee) {
		Squad squad = inviter.getSquad();
		squad.addInvite(invitee);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (squad.hasInvite(invitee)) {
					squad.removeInvite(invitee);
					inviter.sendMessage(Utils.translate("&9" + invitee.getName() + " &3did not accept the squad invite in time"));
					invitee.sendMessage(Utils.translate("&3You did not accept the squad invite in time"));
				}
			}
		}.runTaskLater(Momentum.getPlugin(), 20 * 30); // 30 seconds to accept invite
	}

	public void join(@NotNull Squad squad, PlayerStats newMember) {
		squad.addMember(newMember);
		Momentum.getStatsManager().updateSquad(newMember, squad);
	}

	public void leave(PlayerStats member) {
		Squad squad = member.getSquad();
		inSquadChat.remove(member);
		squad.removeMember(member);
		Momentum.getStatsManager().updateSquad(member, null);
	}

	public void kick(PlayerStats member) {
		leave(member);
	}

	public void promote(PlayerStats newLeader) {
		newLeader.getSquad().setLeader(newLeader);
	}

	public void disband(Squad squad) {
		squad.getSquadMembers().forEach(this::leave);
	}

	// returns true if player toggled on and false if toggled off
	public boolean toggleSquadChat(PlayerStats member) {
		if (!inSquadChat.add(member)) {
			inSquadChat.remove(member);
			return false;
		}

		return true;
	}
	public boolean isInSquadChat(PlayerStats member) {
		return inSquadChat.contains(member);
	}

	public void toggleSquadChatSpy(PlayerStats playerStats) {
		if (!inSquadChatSpy.add(playerStats))
			inSquadChatSpy.remove(playerStats);
	}

	public void sendMessage(PlayerStats member, String message, boolean self) {
		Squad squad = member.getSquad();
		for (PlayerStats m : squad.getSquadMembers()) {
			if (!self && m.equals(member))
				continue;
			m.sendMessage(Utils.translate(message));
		}

		// chat spy persists through relogs so player needs to be online
		inSquadChatSpy.stream().filter(spy -> Momentum.getStatsManager().get(spy.getUUID()) != null && !squad.getSquadMembers().contains(spy)).forEach(spy -> spy.sendMessage(Utils.translate("&1[SqSpy]  " + message)));
	}

	public static void notifyMembers(Squad squad, String message) {
		squad.getSquadMembers().forEach(member -> member.sendMessage(Utils.translate(message)));
	}

	public static void notifyMembers(Squad squad, String message, PlayerStats... except) {
		squad.getSquadMembers().stream().filter(member -> !Arrays.asList(except).contains(member)).forEach(member -> member.sendMessage(Utils.translate(message)));
	}

	public static boolean isLeader(PlayerStats member) {
		Squad squad = member.getSquad();
		return squad != null && squad.getSquadLeader().equals(member);
	}

	public static boolean isMember(Squad squad, PlayerStats member) {
		return squad.getSquadMembers().contains(member);
	}
}
