package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.ranks.Rank;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PlayerStats {

    private Player player;
    private String UUID;
    private String playerName;
    private Level level = null;
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
    private ItemStack chestplateSavedFromElytra = null;
    private int prestiges = 0;
    private int raceWins = 0;
    private int raceLosses = 0;
    private int ratedLevelsCount;
    private int gainedPerksCount = 0;
    private float raceWinRate = 0.00f;
    private float prestigeMultiplier = 1.00f;
    private int rankUpStage;
    private int individualLevelsBeaten;
    private int infinitePKScore = 0;
    private boolean inInfinitePK = false;
    private boolean eventParticipant = false;
    private boolean bypassingPlots = false;
    private int totalLevelCompletions = 0;
    private HashMap<String, Set<LevelCompletion>> levelCompletionsMap = new HashMap<>();
    private HashMap<String, Long> perks = new HashMap<>();
    private HashMap<String, Location> ascendanceCheckpoints = new HashMap<>();

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

    public int getRaceWins() { return raceWins; }

    public void setRaceWins(int raceWins) { this.raceWins = raceWins; }

    public int getRaceLosses() { return raceLosses; }

    public void setRaceLosses(int raceLosses) { this.raceLosses = raceLosses; }

    public float getRaceWinRate() { return raceWinRate; }

    public void setRaceWinRate(float raceWinRate) { this.raceWinRate = raceWinRate; }

    //
    // Level Section
    //
    public void setLevel(Level level) {
        this.level = level;
    }

    public void resetLevel() {
        level = null;
    }

    public Level getLevel() {
        return level;
    }

    public boolean inLevel() {
        return level != null;
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

    public int getTotalLevelCompletions() { return totalLevelCompletions; }

    public void setTotalLevelCompletions(int totalLevelCompletions) { this.totalLevelCompletions = totalLevelCompletions; }

    public void setIndividualLevelsBeaten(int individualLevelsBeaten) { this.individualLevelsBeaten = individualLevelsBeaten; }

    public int getIndividualLevelsBeaten() { return individualLevelsBeaten; }

    public ItemStack getChestplateSavedFromElytra() { return chestplateSavedFromElytra; }

    public void setChestplateSavedFromElytra(ItemStack chestplate) { chestplateSavedFromElytra = chestplate; }

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
    // InfinitePK Section
    //
    public void setInfinitePKScore(int infinitePKScore) {
        this.infinitePKScore = infinitePKScore;
    }

    public int getInfinitePKScore() {
        return infinitePKScore;
    }

    public void setInfinitePK(boolean inInfinitePK) { this.inInfinitePK = inInfinitePK; }

    public boolean isInInfinitePK() {
        return inInfinitePK;
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

    public int getPrestiges() { return prestiges; }

    public void addPrestige() { prestiges += 1; }

    public void setPrestiges(int prestiges) { this.prestiges = prestiges; }

    public float getPrestigeMultiplier() { return prestigeMultiplier; }

    public void setPrestigeMultiplier(float prestigeMultiplier) { this.prestigeMultiplier = prestigeMultiplier; }

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

    public HashMap<String, Location> getAscendanceCheckpoints() { return ascendanceCheckpoints; }

    public Location getAscendanceCheckpoint(String levelName) {
        return ascendanceCheckpoints.get(levelName);
    }

    public void setAscendanceCheckpoints(HashMap<String, Location> checkpoints) {
        ascendanceCheckpoints = checkpoints;
    }

    public void resetAscendanceCheckpoints() {
        ascendanceCheckpoints.clear();
    }

    public void updateAscendanceCheckpoint(String levelName, Location location) {
        ascendanceCheckpoints.remove(levelName);
        ascendanceCheckpoints.put(levelName, location);
    }

    public void removeAscendanceCheckpoint(String levelName) {
        ascendanceCheckpoints.remove(levelName);
    }

    //
    // Completions Section
    //
    public String getMostCompletedLevel() {
        int mostCompletions = -1;
        String mostCompletedLevel = null;

        for (Map.Entry<String, Set<LevelCompletion>> entry : levelCompletionsMap.entrySet())
            if (entry.getValue().size() > mostCompletions) {
                mostCompletions = entry.getValue().size();
                mostCompletedLevel = entry.getKey();
            }

        if (mostCompletions > 0)
            return mostCompletedLevel;

        return null;
    }

    public void levelCompletion(String levelName, LevelCompletion levelCompletion) {
        if (levelName != null && levelCompletion != null) {
            if (!levelCompletionsMap.containsKey(levelName))
                levelCompletionsMap.put(levelName, new HashSet<>());

            if (levelCompletionsMap.get(levelName) != null)
                levelCompletionsMap.get(levelName).add(levelCompletion);
        }
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed) {
        this.levelCompletion(levelName, new LevelCompletion(timeOfCompletion, completionTimeElapsed));
    }

    public HashMap<String, Set<LevelCompletion>> getLevelCompletionsMap() {
        return levelCompletionsMap;
    }

    public int getLevelCompletionsCount(String levelName) {
        if (levelCompletionsMap.containsKey(levelName))
            return levelCompletionsMap.get(levelName).size();

        return 0;
    }

    // top 3 completions
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

    // fastest completion
    public LevelCompletion getQuickestCompletion(String levelName) {
        LevelCompletion fastestCompletion = null;

        if (levelCompletionsMap.containsKey(levelName)) {
            // loop through to find fastest completion
            for (LevelCompletion levelCompletion : levelCompletionsMap.get(levelName))
                // if not null and not including not timed levels, continue
                if (levelCompletion != null && levelCompletion.getCompletionTimeElapsed() > 0)
                    // if null or faster than already fastest completion, set to new completion
                    if (fastestCompletion == null || (levelCompletion.getCompletionTimeElapsed() < fastestCompletion.getCompletionTimeElapsed()))
                        fastestCompletion = levelCompletion;
        }
        return fastestCompletion;
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

    public int getGainedPerksCount() { return gainedPerksCount; }

    public void setGainedPerksCount(int gainedPerksCount) { this.gainedPerksCount = gainedPerksCount; }

    public HashMap<String, Long> getPerks() { return perks; }

    //
    // Event Section
    //
    public boolean isEventParticipant() {
        return eventParticipant;
    }

    public void joinedEvent() {
        eventParticipant = true;
    }

    public void leftEvent() {
        eventParticipant = false;
    }

    //
    // Rated Levels Section
    //
    public void setRatedLevelsCount(int ratedLevelsCount) { this.ratedLevelsCount = ratedLevelsCount; }

    public int getRatedLevelsCount() { return ratedLevelsCount; }

    //
    // Plots Sections
    //
    public void setPlotBypassing(boolean bypassingPlots) {
        this.bypassingPlots = bypassingPlots;
    }

    public boolean isBypassingPlots() { return bypassingPlots; }
}