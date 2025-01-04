package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.*;

public class Squad {
	private PlayerStats squadLeader;
	private final Map<PlayerStats, Long> squadMembers; // includes squad leader
	private final Set<PlayerStats> outgoingInvites;
	private boolean warpCooldown;

	private Squad(PlayerStats leader) {
		this.squadLeader = leader;
		this.squadMembers = new HashMap<>();
		this.outgoingInvites = new HashSet<>();
	}

	private Squad(PlayerStats leader, Map<PlayerStats, Long> members) {
		this.squadLeader = leader;
		this.squadMembers = members;
		this.outgoingInvites = new HashSet<>();
	}

	protected void addMember(PlayerStats member) { squadMembers.put(member, System.currentTimeMillis()); }

	protected void removeMember(PlayerStats member) { squadMembers.remove(member); }

	protected void clearMembers() { squadMembers.clear(); }

	protected void setLeader(PlayerStats newLeader) { this.squadLeader = newLeader; }

	protected void resetLeader() { this.squadLeader = null; }

	public boolean hasInvite(PlayerStats invitee) { return outgoingInvites.contains(invitee); }

	protected void addInvite(PlayerStats invitee) { outgoingInvites.add(invitee); }

	protected void removeInvite(PlayerStats invitee) { outgoingInvites.remove(invitee); }

	protected void clearInvites() { outgoingInvites.clear(); }

	protected PlayerStats getSquadLeader() { return this.squadLeader; }
	protected Map<PlayerStats, Long> getSquadMembers() { return this.squadMembers; }

	public int size() { return squadMembers.size(); }

	public boolean hasWarpCooldown() { return warpCooldown; }
	protected void setWarpCooldown(boolean cooldown) { warpCooldown = cooldown; }

	public static class Builder {
		private PlayerStats leader;
		private final Map<PlayerStats, Long> members = new HashMap<>();

		public static Builder create() {
			return new Builder();
		}

		public Builder setLeader(PlayerStats leader) {
			this.leader = leader;
			this.members.put(leader, System.currentTimeMillis());
			return this;
		}

		public Builder addMembers(Set<PlayerStats> members) {
			members.forEach(member -> this.members.put(member, System.currentTimeMillis()));
			return this;
		}

		public Builder setMembers(Set<PlayerStats> members) {
			this.members.clear();
			members.forEach(member -> this.members.put(member, System.currentTimeMillis()));
			return this;
		}

		public Builder addMember(PlayerStats member) {
			members.put(member, System.currentTimeMillis());
			return this;
		}

		public Squad build() {
			return new Squad(leader, members);
		}
	}
}
