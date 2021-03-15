package com.parkourcraft.parkour.data.stats;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.clans.Clan;
import com.parkourcraft.parkour.data.rank.Rank;
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
    private Location spectateSpawn = null;
    private boolean inRace = false;
    private Rank rank;
    private int rankUpStage;
    private Map<String, List<LevelCompletion>> levelCompletionsMap = new HashMap<>();
    private Map<String, Long> perks = new HashMap<>();

    public PlayerStats(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId().toString();
        this.playerName = player.getName();
    }

    //
    // Player Info Section
    //
    public boolean isLoaded() {
        if (playerID > 0)
            return true;

        return false;
    }

    public Player getPlayer() {
        return player;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUUID() {
        return UUID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerID() {
        return playerID;
    }

    //
    // Race Section
    //
    public void startedRace() {
        inRace = true;
    }

    public void endedRace() {
        inRace = false;
    }

    public boolean inRace() {
        return inRace;
    }

    //
    // Level Section
    //
    public void setLevel(String level) {
        this.level = level;
    }

    public void resetLevel() {
        level = null;
    }

    public String getLevel() {
        return level;
    }

    public boolean inLevel() {
        if (level != null)
            return true;
        return false;
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

    //
    // Spectator Section
    //
    public void setSpectateSpawn(Location spectateSpawn) {
        this.spectateSpawn = spectateSpawn;
    }

    public void resetSpectateSpawn() { spectateSpawn = null; }

    public Location getSpectateSpawn() {
        return spectateSpawn;
    }

    public void setSpectatable(boolean setting) {
        spectatable = setting;
    }

    public boolean isSpectatable() {
        if (playerToSpectate != null)
            return false;
        return spectatable;
    }

    public void setPlayerToSpectate(PlayerStats playerStats) {
        playerToSpectate = playerStats;
    }

    public PlayerStats getPlayerToSpectate() {
        return playerToSpectate;
    }

    //
    // Rank Section
    //
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isLastRank() {
        // get if they are at last rank
        if (rank != null && rank.getRankId() == Parkour.getRanksManager().getRankList().size())
            return true;
        return false;
    }

    public void setRankUpStage(int rankUpStage) {
        this.rankUpStage = rankUpStage;
    }

    public int getRankUpStage() {
        return rankUpStage;
    }

    //
    // Practice Mode Section
    //
    public void setPracticeMode(Location loc) {
        practiceSpawn = loc;
    }

    public void resetPracticeMode() {
        practiceSpawn = null;
    }

    public Location getPracticeLocation() {
        return practiceSpawn;
    }

    //
    // Clan Section
    //
    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public Clan getClan() {
        return clan;
    }

    public void resetClan() { clan = null; }

    //
    // Checkpoint Section
    //
    public void setCheckpoint(Location loc) {
        currentCheckpoint = loc;
    }

    public Location getCheckpoint() { return currentCheckpoint; }

    public void resetCheckpoint() {
        currentCheckpoint = null;
    }

    //
    // Completions Section
    //
    public void levelCompletion(String levelName, LevelCompletion levelCompletion) {
        if (levelName != null && levelCompletion != null) {
            if (!levelCompletionsMap.containsKey(levelName))
                levelCompletionsMap.put(levelName, new ArrayList<>());

            levelCompletionsMap.get(levelName).add(levelCompletion);
        }
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed) {
        this.levelCompletion(levelName, new LevelCompletion(timeOfCompletion, completionTimeElapsed));
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
                if (levelCompletion != null && levelCompletion.getCompletionTimeElapsed() > 0)
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

    //
    // Perks Section
    //
    public void addPerk(String perkName, Long time) {
        perks.put(perkName, time);
    }

    public boolean hasPerk(String perkName) {
        return perks.containsKey(perkName);
    }
}