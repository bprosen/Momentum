package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;

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

    public boolean isBestScore(String playerName, int score) {

        boolean isBest = false;

        // it will first check if they have a score (exist), then it will check if their gotten score is better
        if (InfinitePKDB.hasScore(playerName)) {
            int currentBestScore = InfinitePKDB.getScoreFromName(playerName);

            if (score > currentBestScore)
                isBest = true;
        }
        return isBest;
    }

    // method to update their score in all 3 possible placed
    public void updateScore(String playerName, int score) {
        PlayerStats playerStats = Parkour.getStatsManager().getByNameIgnoreCase(playerName);

        if (playerStats != null)
            playerStats.setInfinitePKScore(score);

        if (isLBPosition(playerName))
            getLeaderboardPosition(playerName).setScore(score);

        Parkour.getDatabaseManager().add(
                "UPDATE players SET infinitepk_score=" + score + " WHERE player_name='" + playerName + "'"
        );
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
