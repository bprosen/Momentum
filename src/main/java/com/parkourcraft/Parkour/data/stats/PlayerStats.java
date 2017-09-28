package com.parkourcraft.Parkour.data.stats;

import com.parkourcraft.Parkour.data.LevelManager;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.storage.mysql.DatabaseManager;
import com.parkourcraft.Parkour.storage.mysql.DatabaseQueries;

import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerStats {

    private String UUID;
    private String playerName;
    private int playerID;

    private long levelStartTime = 0;

    private Map<String, List<LevelCompletion>> levelCompletionsMap = new HashMap<>();
    private Map<String, Long> perks = new HashMap<>();

    public PlayerStats(String UUID, String playerName) {
        this.UUID = UUID;
        this.playerName = playerName;
    }

    public void levelCompletion(String levelName, LevelCompletion levelCompletion) {
        if (!levelCompletionsMap.containsKey(levelName))
            levelCompletionsMap.put(levelName, new ArrayList<>());

        levelCompletionsMap.get(levelName).add(levelCompletion);
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed) {
        this.levelCompletion(levelName, new LevelCompletion(timeOfCompletion, completionTimeElapsed));
    }

    public String getUUID() {
        return UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void startedLevel() {
        levelStartTime = System.currentTimeMillis();
    }

    public void disableLevelStartTime() {
        levelStartTime = 0;
    }

    public long getLevelStartTime() {
        return levelStartTime;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }

    public Map<String, List<LevelCompletion>> getLevelCompletionsMap() {
        return levelCompletionsMap;
    }

    public int getLevelCompletionsCount(String levelName) {
        if (levelCompletionsMap.containsKey(levelName))
            return levelCompletionsMap.get(levelName).size();

        return 0;
    }

    public List<LevelCompletion> getQuickestCompletions(String levelName) {
        List<LevelCompletion> levelCompletions = new ArrayList<>();

        if (levelCompletionsMap.containsKey(levelName)) {

            for (LevelCompletion levelCompletion : levelCompletionsMap.get(levelName))
                if (levelCompletion.getCompletionTimeElapsed() > 0)
                    levelCompletions.add(levelCompletion);

            if (levelCompletions.size() < 2)
                return levelCompletions;

            for (int i = 0; i < levelCompletions.size() - 1; i++) {
                int min_id = i;

                for (int j = i + 1; j < levelCompletions.size(); j++)
                    if (levelCompletions.get(j).getCompletionTimeElapsed()
                            < levelCompletions.get(min_id).getCompletionTimeElapsed())
                        min_id = j;

                LevelCompletion temp = levelCompletions.get(min_id);
                levelCompletions.set(min_id, levelCompletions.get(i));
                levelCompletions.set(i, temp);
            }
        }

        return levelCompletions;
    }

    public void addPerk(String perkName, Long time) {
        perks.put(perkName, time);
    }

    public boolean hasPerk(String perkName) {
        return perks.containsKey(perkName);
    }



}
