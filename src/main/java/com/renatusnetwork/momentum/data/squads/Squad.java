package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.util.HashSet;
import java.util.Set;

public class Squad {
	private PlayerStats squadLeader;
	private final Set<PlayerStats> squadMembers; // includes squad leader

	private Squad(PlayerStats leader) {
		this.squadLeader = leader;
		this.squadMembers = new HashSet<>();
	}

	private Squad(PlayerStats leader, Set<PlayerStats> members) {
		this.squadLeader = leader;
		this.squadMembers = members;
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

	public PlayerStats getSquadLeader() {
		return this.squadLeader;
	}
	public Set<PlayerStats> getSquadMembers() {
		return this.squadMembers;
	}

	public static class Builder {
		private PlayerStats leader;
		private Set<PlayerStats> members;

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
