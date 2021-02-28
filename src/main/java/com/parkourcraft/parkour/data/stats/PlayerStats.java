package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.data.clans.Clan;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerStats {

    private Player player;
    private String UUID;
    private String playerName;
    private String level = null;
    private int playerID = -1;
    private long levelStartTime = 0;
    private boolean spectatable;
    private PlayerStats playerToSpectate;
    private Clan clan;
    private Location currentCheckpoint = null;
    private Location practiceSpawn = null;
    private Map<String, List<LevelCompletion>> levelCompletionsMap = new HashMap<>();
    private Map<String, Long> perks = new HashMap<>();

    public PlayerStats(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId().toString();
        this.playerName = player.getName();
    }

    public void levelCompletion(String levelName, LevelCompletion levelCompletion) {
        if (!levelCompletionsMap.containsKey(levelName))
            levelCompletionsMap.put(levelName, new ArrayList<>());

        levelCompletionsMap.get(levelName).add(levelCompletion);
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed) {
        this.levelCompletion(levelName, new LevelCompletion(timeOfCompletion, completionTimeElapsed));
    }

    public boolean isLoaded() {
        if (playerID > 0)
            return true;

        return false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getUUID() {
        return UUID;
    }

    public boolean inLevel() {
        if (level != null)
            return true;
        return false;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void resetLevel() {
        level = null;
    }

    public String getLevel() {
        return level;
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

    public void setSpectatable(boolean setting) {
        spectatable = setting;
    }

    public boolean isSpectatable() {
        if (playerToSpectate != null)
            return false;

        return spectatable;
    }

    public void setPracticeMode(Location loc) {
        practiceSpawn = loc;
    }

    public void resetPracticeMode() {
        practiceSpawn = null;
    }

    public Location getPracticeLocation() {
        return practiceSpawn;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public Clan getClan() {
        return clan;
    }

    public void setPlayerToSpectate(PlayerStats playerStats) {
        playerToSpectate = playerStats;
    }

    public PlayerStats getPlayerToSpectate() {
        return playerToSpectate;
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

    public void setCheckpoint(Location loc) {
        currentCheckpoint = loc;
    }

    public Location getCheckpoint() { return currentCheckpoint; }

    public void resetCheckpoint() {
        if (currentCheckpoint != null)
            currentCheckpoint = null;
    }
}