package com.parkourcraft.Parkour.stats.objects;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelStats {

    private String levelName;

    private Map<Long, LevelCompletion> levelCompletionsMap = new HashMap<>();

    public LevelStats(String levelName) {
        this.levelName = levelName;
    }

    public void levelCompletion(LevelCompletion levelCompletion) {
        levelCompletionsMap.put(levelCompletion.getTimeOfCompletion(), levelCompletion);
    }

    public List<LevelCompletion> getCompletionsNotInDatabase() {
        List<LevelCompletion> levelCompletions = new ArrayList<>();

        for (Map.Entry<Long, LevelCompletion> entry : levelCompletionsMap.entrySet()) {
            if (!entry.getValue().inDatabase())
                levelCompletions.add(entry.getValue());
        }

        return levelCompletions;
    }

}
