package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;

import java.util.*;

public class InfinitePKManager {

    private Set<InfinitePK> participants = new HashSet<>();
    private Set<InfinitePKLBPosition> leaderboard = new HashSet<>(Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

    public InfinitePKManager() {
        InfinitePKDB.loadLeaderboard();
    }

    public InfinitePK get(String UUID) {
        for (InfinitePK participant : participants)
            if (participant.getUUID().equalsIgnoreCase(UUID))
                return participant;

        return null;
    }

    public void add(InfinitePK infinitePK) {
        participants.add(infinitePK);
    }

    public void remove(String UUID) {
        InfinitePK infinitePK = get(UUID);

        if (infinitePK != null)
            participants.remove(infinitePK);
    }

    public InfinitePKLBPosition getLeaderboardPosition(int position) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getPosition() == position)
                return infinitePKLBPosition;

        return null;
    }

    public InfinitePKLBPosition getLeaderboardPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return infinitePKLBPosition;

        return null;
    }

    public boolean isLBPosition(String playerName) {
        for (InfinitePKLBPosition infinitePKLBPosition : leaderboard)
            if (infinitePKLBPosition.getName().equalsIgnoreCase(playerName))
                return true;

        return false;
    }

    public Set<InfinitePKLBPosition> getLeaderboard() {
        return leaderboard;
    }

    public Set<InfinitePK> getParticipants() {
        return participants;
    }
}
