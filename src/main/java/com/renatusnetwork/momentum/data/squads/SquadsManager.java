package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SquadsManager {
	private final Set<PlayerStats> inSquadChat;
	private final Set<PlayerStats> inSquadChatSpy;
	private final Map<String, Squad> offlineCache; // squad members who go offline without leaving will be stored here for 1 minute

	public SquadsManager() {
		this.inSquadChat = new HashSet<>();
		this.inSquadChatSpy = new HashSet<>();
		this.offlineCache = new HashMap<>();
	}

	public void createSquad(PlayerStats leader) {
		Squad squad = Squad.Builder.create().setLeader(leader).build();
		leader.updateSquad(squad);
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
					if (squad.size() == 1)
						disband(squad);
				}
			}
		}.runTaskLater(Momentum.getPlugin(), 20 * 30); // 30 seconds to accept invite
	}

	public void join(@NotNull Squad squad, PlayerStats newMember) {
		squad.removeInvite(newMember); // remove invite after joining
		squad.addMember(newMember);
		newMember.updateSquad(squad);
	}

	public void leave(PlayerStats member) {
		Squad squad = member.getSquad();
		inSquadChat.remove(member);
		squad.removeMember(member);
		member.updateSquad(null);
	}

	public void kick(PlayerStats member) {
		leave(member);
	}

	public void promote(PlayerStats newLeader) {
		newLeader.getSquad().setLeader(newLeader);
	}

	public void disband(Squad squad) {
		for (PlayerStats member : squad.getSquadMembers()) {
			inSquadChat.remove(member);
			member.updateSquad(null);
		}
		Momentum.getPluginLogger().info(offlineCache.toString());
		offlineCache.values().removeAll(Collections.singleton(squad)); // if the squad doesnt exist anymore, no need to keep cache for offline players
		Momentum.getPluginLogger().info(offlineCache.toString());
		squad = null;
	}

	public void warpMembers(PlayerStats leader) {
		Squad squad = leader.getSquad();
		squad.getSquadMembers().stream().filter(member -> !member.equals(leader) && (!member.inLevel() || !leader.getLevel().equals(member.getLevel()))).forEach(member -> Momentum.getLevelManager().teleportToLevel(member, leader.getLevel()));
		squad.setWarpCooldown(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				squad.setWarpCooldown(false);
			}
		}.runTaskLater(Momentum.getPlugin(), 20 * 3); // 3 sec cooldown
	}

	// returns true if player toggled on and false if toggled off
	public boolean toggleSquadChat(PlayerStats member) {
		if (!inSquadChat.add(member)) {
			inSquadChat.remove(member);
			return false;
		}

		return true;
	}

	public boolean toggleSquadChatSpy(PlayerStats playerStats) {
		if (!inSquadChatSpy.add(playerStats)) {
			inSquadChatSpy.remove(playerStats);
			return false;
		}

		return true;
	}

	public boolean isInSquadChat(PlayerStats member) {
		return inSquadChat.contains(member);
	}

	public Collection<PlayerStats> getSquadMembers(Squad squad) {
		return squad.getSquadMembers();
	}

	public void sendMessage(PlayerStats member, String message, boolean self) {
		Squad squad = member.getSquad();
		for (PlayerStats m : squad.getSquadMembers()) {
			if (!self && m.equals(member))
				continue;
			m.sendMessage(Utils.translate(message));
		}

		// chat spy persists through relogs so player needs to be online
		inSquadChatSpy.stream().filter(spy -> Momentum.getStatsManager().get(spy.getPlayer()) != null && !squad.getSquadMembers().contains(spy)).forEach(spy -> spy.sendMessage(Utils.translate("&1[SqSpy]  " + message)));
	}

	public static void notifyMembers(Squad squad, String message) {
		squad.getSquadMembers().forEach(member -> member.sendMessage(Utils.translate(message)));
	}

	public static void notifyMembers(Squad squad, String message, PlayerStats... except) {
		squad.getSquadMembers().stream().filter(member -> !Arrays.asList(except).contains(member)).forEach(member -> member.sendMessage(Utils.translate(message)));
	}

	public void notifySqChatSpies(String message) {
		inSquadChatSpy.forEach(member -> member.sendMessage(Utils.translate("&1[SqSpy]  " + message)));
	}

	public static boolean isLeader(PlayerStats member) {
		Squad squad = member.getSquad();
		return squad != null && squad.getSquadLeader().equals(member);
	}

	public static boolean isMember(Squad squad, PlayerStats member) {
		return squad.getSquadMembers().contains(member);
	}

	public void addOffline(String uuid, Squad squad) {
		offlineCache.put(uuid, squad);
	}

	public Squad getOffline(String uuid) {
		return offlineCache.get(uuid);
	}

	public void removeOffline(String uuid) {
		offlineCache.remove(uuid);
	}
}
