package com.parkourcraft.parkour.data.infinite;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class InfinitePKManager {

    private Set<InfinitePK> participants = new HashSet<>();
    private LinkedHashMap<String, Integer> leaderboard = new LinkedHashMap<>(Parkour.getSettingsManager().max_infinitepk_leaderboard_size);

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

    public int getLeaderboardScore(String placementUUID) {
        return leaderboard.get(placementUUID);
    }

    public LinkedHashMap<String, Integer> getLeaderboard() {
        return leaderboard;
    }

    public Set<InfinitePK> getParticipants() {
        return participants;
    }
}
