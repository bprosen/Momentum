package com.renatusnetwork.momentum.data.squads;

import com.renatusnetwork.momentum.data.stats.PlayerStats;

import java.time.Instant;
import java.util.*;

public class Squad {
	private PlayerStats squadLeader;
	private final Map<PlayerStats, Instant> squadMembers; // includes squad leader
	private final Set<PlayerStats> outgoingInvites;
	private boolean warpCooldown = false;

	private Squad(PlayerStats leader) {
		this.squadLeader = leader;
		this.squadMembers = new HashMap<>();
		this.outgoingInvites = new HashSet<>();
	}

	private Squad(PlayerStats leader, Map<PlayerStats, Instant> members) {
		this.squadLeader = leader;
		this.squadMembers = members;
		this.outgoingInvites = new HashSet<>();
	}

	protected void addMember(PlayerStats member) { squadMembers.put(member, Instant.now()); }

	protected void removeMember(PlayerStats member) { squadMembers.remove(member); }

	protected void removeAllMembers() { squadMembers.clear(); }

	protected void setLeader(PlayerStats newLeader) { this.squadLeader = newLeader; }

	public boolean hasInvite(PlayerStats invitee) { return outgoingInvites.contains(invitee); }

	protected void addInvite(PlayerStats invitee) { outgoingInvites.add(invitee); }

	protected void removeInvite(PlayerStats invitee) { outgoingInvites.remove(invitee); }

	protected PlayerStats getSquadLeader() { return this.squadLeader; }
	protected Map<PlayerStats, Instant> getSquadMembers() { return this.squadMembers; }

	public int size() { return squadMembers.size(); }

	public boolean hasWarpCooldown() { return warpCooldown; }
	protected void setWarpCooldown(boolean cooldown) { warpCooldown = cooldown; }

	public static class Builder {
		private PlayerStats leader = null;
		private Map<PlayerStats, Instant> members = new HashMap<>();

		public static Builder create() {
			return new Builder();
		}

		public Builder setLeader(PlayerStats leader) {
			this.leader = leader;
			this.members.put(leader, Instant.now());
			return this;
		}

		public Builder addMembers(Set<PlayerStats> members) {
			for (PlayerStats member : members) {
				this.members.put(member, Instant.now());
			}
			return this;
		}

		public Builder setMembers(Set<PlayerStats> members) {
			this.members.clear();
			for (PlayerStats member : members) {
				this.members.put(member, Instant.now());
			}
			return this;
		}

		public Builder addMember(PlayerStats member) {
			members.put(member, Instant.now());
			return this;
		}

		public Squad build() {
			return new Squad(leader, members);
		}
	}
}
