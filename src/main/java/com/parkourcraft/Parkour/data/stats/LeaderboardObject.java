package com.parkourcraft.Parkour.data.stats;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardObject {

    private String levelName;
    private int levelID;

    // rank:[playerID:levelCompletion]
    private Map<Integer, Map<Integer, LevelCompletion>> rankingsMap = new HashMap<>();

    public LeaderboardObject(String levelName) {
        this.levelName = levelName;
        this.levelID = LevelManager.getIDFromCache(levelName);
    }

    public String getLevelName() {
        return levelName;
    }

    public int getLevelID() {
        return levelID;
    }

    public void loadLeaderboard() {
        rankingsMap = new HashMap<>();

        List<Map<String, String>> leaderboardResults = DatabaseQueries.getResults(
                "completions",
                "player_id, time_taken, UNIX_TIMESTAMP(completion_date)",
                "WHERE level_id=" + levelID + " ORDER BY time_taken DESC LIMIT 10"
        );
    }

}
