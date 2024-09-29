package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.HashSet;
import java.util.Set;

public class Squad {
	private PlayerStats squadLeader;
	private final Set<PlayerStats> squadMembers; // includes squad leader
	private final Set<PlayerStats> outgoingInvites;
	private boolean warpCooldown = false;

	private Squad(PlayerStats leader) {
		this.squadLeader = leader;
		this.squadMembers = new HashSet<>();
		this.outgoingInvites = new HashSet<>();
	}

	private Squad(PlayerStats leader, Set<PlayerStats> members) {
		this.squadLeader = leader;
		this.squadMembers = members;
		this.outgoingInvites = new HashSet<>();
	}

	protected void addMember(PlayerStats member) { squadMembers.add(member); }

	protected void removeMember(PlayerStats member) { squadMembers.remove(member); }

	protected void setLeader(PlayerStats newLeader) { this.squadLeader = newLeader; }

	public boolean hasInvite(PlayerStats invitee) { return outgoingInvites.contains(invitee); }

	protected void addInvite(PlayerStats invitee) { outgoingInvites.add(invitee); }

	protected void removeInvite(PlayerStats invitee) { outgoingInvites.remove(invitee); }

	protected PlayerStats getSquadLeader() { return this.squadLeader; }
	protected Set<PlayerStats> getSquadMembers() { return this.squadMembers; }

	public int count() { return squadMembers.size(); }

	public boolean hasWarpCooldown() { return warpCooldown; }
	protected void setWarpCooldown(boolean cooldown) { warpCooldown = cooldown; }

	public static class Builder {
		private PlayerStats leader = null;
		private Set<PlayerStats> members = new HashSet<>();

		public static Builder create() {
			return new Builder();
		}

		public Builder setLeader(PlayerStats leader) {
			this.leader = leader;
			this.members.add(leader);
			return this;
		}

		public Builder addMembers(Set<PlayerStats> members) {
			this.members.addAll(members);
			return this;
		}

		public Builder setMembers(Set<PlayerStats> members) {
			this.members = members;
			return this;
		}

		public Builder addMember(PlayerStats member) {
			members.add(member);
			return this;
		}

		public Squad build() {
			return new Squad(leader, members);
		}
	}
}
