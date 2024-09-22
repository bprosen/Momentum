package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.HashSet;
import java.util.Set;

public class Squad {
	private PlayerStats squadLeader;
	private final Set<PlayerStats> squadMembers; // includes squad leader
	private final Set<PlayerStats> outgoingInvites;

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

	// returns true if member was not already in party
	public boolean addMember(PlayerStats member) {
		return squadMembers.add(member);
	}

	// returns true if member was in party
	public boolean removeMember(PlayerStats member) {
		return squadMembers.remove(member);
	}

	public boolean setLeader(PlayerStats newLeader) {
		if (!squadMembers.contains(newLeader))
			return false;

		this.squadLeader = newLeader;
		return true;
	}

	public boolean hasInvite(PlayerStats invitee) {
		return outgoingInvites.contains(invitee);
	}

	// returns false if the player is already invited
	public boolean addInvite(PlayerStats invitee) {
		return outgoingInvites.add(invitee);
	}

	public void removeInvite(PlayerStats invitee) {
		outgoingInvites.remove(invitee);
	}

	protected PlayerStats getSquadLeader() {
		return this.squadLeader;
	}
	protected Set<PlayerStats> getSquadMembers() {
		return this.squadMembers;
	}
	protected int getMemberCount() {
		return this.squadMembers.size();
	}


	public static class Builder {
		private PlayerStats leader = null;
		private Set<PlayerStats> members = new HashSet<>();

		public static Builder create() {
			return new Builder();
		}

		public Builder setLeader(PlayerStats leader) {
			this.leader = leader;
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
