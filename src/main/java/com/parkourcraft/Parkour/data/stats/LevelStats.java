package com.parkourcraft.Parkour.data.stats;

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

    public Map<Long, LevelCompletion> getLevelCompletionsMap() {
        return levelCompletionsMap;
    }

    public int getCompletionsCount() {
        return levelCompletionsMap.size();
    }

    public List<LevelCompletion> getQuickestCompletions() {
        List<LevelCompletion> levelCompletions = new ArrayList<>();

        for (LevelCompletion levelCompletion : levelCompletionsMap.values())
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

        return levelCompletions;
    }

}
