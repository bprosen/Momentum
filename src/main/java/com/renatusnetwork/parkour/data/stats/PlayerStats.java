package com.renatusnetwork.parkour.data.stats;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.clans.Clan;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelCompletion;
import com.renatusnetwork.parkour.data.menus.LevelSortingType;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifierType;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.utils.Utils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerStats {

    private Player player;
    private String uuid;
    private String name;
    private double coins;
    private Level level;
    private long levelStartTime;
    private boolean spectatable;
    private PlayerStats playerToSpectate;
    private Clan clan;
    private Location currentCheckpoint;
    private Location practiceSpawn;
    private Location spectateSpawn;
    private boolean inRace;
    private Rank rank;
    private ItemStack chestplateSavedFromElytra;
    private int prestiges;
    private int raceWins;
    private int raceLosses;
    private int ratedLevelsCount;
    private float raceWinRate;
    private float prestigeMultiplier;
    private int individualLevelsBeaten;
    private boolean inInfinite;
    private InfiniteType infiniteType;
    private boolean eventParticipant;
    private boolean bypassingPlots;
    private int totalLevelCompletions;
    private boolean nightVision;
    private boolean grinding;
    private int eventWins;
    private Material infiniteBlock;
    private boolean inTutorial;
    private boolean inBlackmarket;
    private boolean failsToggled;
    private int fails;
    private boolean attemptingRankup;
    private boolean attemptingMastery;
    private LevelSortingType sortingType;

    private FastBoard board;

    private HashMap<String, Set<LevelCompletion>> levelCompletions;
    private HashSet<String> masteryCompletions;
    private HashSet<LevelCompletion> records;
    private HashSet<Perk> perks;
    private HashMap<String, Location> checkpoints;
    private HashSet<String> boughtLevels;
    private HashMap<String, Location> saves;
    private HashMap<ModifierType, Modifier> modifiers;
    private HashMap<InfiniteType, Integer> bestInfiniteScores;
    private ArrayList<Level> favoriteLevels;

    public PlayerStats(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId().toString();
        this.name = player.getName();

        // load maps
        this.levelCompletions = new HashMap<>();
        this.perks = new HashSet<>();
        this.checkpoints = new HashMap<>();
        this.boughtLevels = new HashSet<>();
        this.saves = new HashMap<>();
        this.modifiers = new HashMap<>();
        this.bestInfiniteScores = new HashMap<>();
        this.records = new HashSet<>();
        this.masteryCompletions = new HashSet<>();
        this.favoriteLevels = new ArrayList<>();

        // default for now, if they are not a new player the mysql db loading will adjust these
        this.infiniteBlock = Parkour.getSettingsManager().infinite_default_block;
        this.rank = Parkour.getRanksManager().get(Parkour.getSettingsManager().default_rank);

        for (InfiniteType type : InfiniteType.values())
            bestInfiniteScores.put(type, 0); // arbitrary to be replaced when stats load

        this.sortingType = Parkour.getSettingsManager().default_level_sorting_type;
    }

    //
    // Player Info Section
    //
    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public void initBoard() {
        this.board = new FastBoard(player);
    }

    public void updateBoard(List<String> lines) {
        board.updateLines(lines);
    }

    public void setLevelSortingType(LevelSortingType type) {
        this.sortingType = type;
    }

    public LevelSortingType getLevelSortingType() {
        return sortingType;
    }

    public boolean hasBoard() {
        return board != null;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getDisplayName() {
        return player.getDisplayName();
    }

    public String getUUID() {
        return uuid;
    }

    public FastBoard getBoard() {
        return board;
    }

    public void deleteBoard()
    {
        board.delete();
        board = null;
    }

    public boolean hasNightVision() {
        return nightVision;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
    }

    public boolean isInTutorial() {
        return inTutorial;
    }

    public void setTutorial(boolean tutorial) {
        inTutorial = tutorial;
    }

    public void setBlackMarket(boolean blackMarket) {
        inBlackmarket = blackMarket;
    }

    public boolean isInBlackMarket() {
        return inBlackmarket;
    }

    //
    // Coins Sections
    //
    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        if (coins < 0)
            coins = 0;

        this.coins = coins;
    }

    public void addCoins(double coins) {
        this.coins += coins;
    }

    public void removeCoins(double coins) {
        this.coins -= coins;

        // no allowing negative numbers, NO DEBT
        if (this.coins < 0)
            this.coins = 0;
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

    public int getRaceWins() {
        return raceWins;
    }

    public void setRaceWins(int raceWins) {
        this.raceWins = raceWins;
    }

    public int getRaceLosses() {
        return raceLosses;
    }

    public void setRaceLosses(int raceLosses) {
        this.raceLosses = raceLosses;
    }

    public float getRaceWinRate() {
        return raceWinRate;
    }

    public void setRaceWinRate(float raceWinRate) {
        this.raceWinRate = raceWinRate;
    }

    //
    // Level Section
    //
    public void setLevel(Level level) {
        // only continue if non null
        if (level != null) {
            if (level.isRaceLevel())
                resetLevel(); // force the item removal
            else {
                // set item
                SettingsManager settingsManager = Parkour.getSettingsManager();
                player.getInventory().setItem(settingsManager.leave_hotbar_slot, settingsManager.leave_item);
            }
        }
        this.level = level;
    }

    public void resetLevel() {
        level = null;

        ItemStack itemStack = Utils.getSpawnItemIfExists(player.getInventory());

        // remove if not null
        if (itemStack != null)
            player.getInventory().remove(itemStack);
    }

    public Level getLevel() {
        return level;
    }

    public boolean inLevel() {
        return level != null;
    }

    public Level getFavoriteLevel(int index)
    {
        if (index < favoriteLevels.size())
            return favoriteLevels.get(index);
        return null;
    }

    public int numFavoriteLevels()
    {
        return favoriteLevels.size();
    }

    public void addFavoriteLevel(Level level)
    {
        favoriteLevels.add(level);
    }

    public ArrayList<Level> getFavoriteLevels()
    {
        return favoriteLevels;
    }

    public boolean hasFavoriteLevels()
    {
        return !favoriteLevels.isEmpty();
    }

    public boolean hasFavorite(Level level)
    {
        return favoriteLevels.contains(level);
    }

    public void removeFavoriteLevel(Level level)
    {
        favoriteLevels.remove(level);
    }

    public void setFavoriteLevels(ArrayList<Level> favoriteLevels)
    {
        this.favoriteLevels = favoriteLevels;
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

    public void addTotalLevelCompletions() { totalLevelCompletions++; }

    public void setIndividualLevelsBeaten(int individualLevelsBeaten) { this.individualLevelsBeaten = individualLevelsBeaten; }

    public int getIndividualLevelsBeaten() { return individualLevelsBeaten; }

    public ItemStack getChestplateSavedFromElytra() { return chestplateSavedFromElytra; }

    public void setChestplateSavedFromElytra(ItemStack chestplate) { chestplateSavedFromElytra = chestplate; }

    public int getNumRecords() { return records.size(); }

    public HashSet<LevelCompletion> getRecords() { return records; }

    public void setRecords(HashSet<LevelCompletion> records) { this.records = records; }

    public void removeRecord(LevelCompletion recordCompletion) { records.remove(recordCompletion); }

    public void addRecord(LevelCompletion recordCompletion) { records.add(recordCompletion); }

    public boolean hasBoughtLevel(Level level)
    {
        return boughtLevels.contains(level.getName());
    }

    public void buyLevel(Level level)
    {
        boughtLevels.add(level.getName());
    }

    public void removeBoughtLevel(Level level) { boughtLevels.remove(level.getName()); }

    public void setBoughtLevels(HashSet<String> levels) { boughtLevels = levels ; }

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

    public boolean isSpectatable()
    {
        return spectatable;
    }

    public void setPlayerToSpectate(PlayerStats playerStats) {
        playerToSpectate = playerStats;
    }

    public PlayerStats getPlayerToSpectate() {
        return playerToSpectate;
    }

    public boolean isSpectating() { return playerToSpectate != null; }

    //
    // InfinitePK Section
    //

    public int getBestInfiniteScore(InfiniteType type) { return bestInfiniteScores.get(type); }

    public int getBestInfiniteScore()
    {
        return bestInfiniteScores.get(infiniteType);
    }

    public void setInfiniteScore(int infiniteScore)
    {
        bestInfiniteScores.replace(infiniteType, infiniteScore);
    }

    public void setInfiniteScore(InfiniteType type, int infiniteScore)
    {
        bestInfiniteScores.replace(type, infiniteScore);
    }

    public void setInfinite(boolean inInfinite) { this.inInfinite = inInfinite; }

    public boolean isInInfinite() {
        return inInfinite;
    }

    public Material getInfiniteBlock() { return infiniteBlock; }

    public void setInfiniteBlock(Material infiniteBlock) { this.infiniteBlock = infiniteBlock; }

    public InfiniteType getInfiniteType() { return infiniteType; }

    public void setInfiniteType(InfiniteType type) { this.infiniteType = type; }

    //
    // Rank Section
    //
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean hasRank() { return rank != null; }

    public boolean isLastRank()
    {
        // get if they are at last rank
        return rank != null && rank.isMaxRank();
    }

    public boolean hasPrestiges() { return prestiges > 0; }

    public int getPrestiges() { return prestiges; }

    public void addPrestige() { prestiges++; }

    public void setPrestiges(int prestiges) { this.prestiges = prestiges; }

    public float getPrestigeMultiplier() { return prestigeMultiplier; }

    public void setPrestigeMultiplier(float prestigeMultiplier) { this.prestigeMultiplier = prestigeMultiplier; }

    public void setAttemptingRankup(boolean attemptingRankup) { this.attemptingRankup = attemptingRankup; }

    public boolean isAttemptingRankup() { return attemptingRankup; }

    //
    // Mastery Section
    //
    public void setAttemptingMastery(boolean attemptingMastery) { this.attemptingMastery = attemptingMastery; }

    public void addMasteryCompletion(String levelName) { masteryCompletions.add(levelName); }

    public void removeMasteryCompletion(String levelName) { masteryCompletions.remove(levelName); }

    public boolean hasMasteryCompletion(Level level) { return masteryCompletions.contains(level.getName()); }

    public boolean hasMasteryCompletion(String levelName) { return masteryCompletions.contains(levelName); }

    public int getNumMasteryCompletions() { return masteryCompletions.size(); }

    public boolean isAttemptingMastery() { return attemptingMastery; }

    //
    //
    // Fails Section
    //
    public void setFailMode(boolean failsToggled) { this.failsToggled = failsToggled; }
    public boolean inFailMode() { return failsToggled; }

    public int getFails() { return fails; }

    public void addFail()
    {
        if (failsToggled && !inInfinite && !inTutorial && !inRace && !eventParticipant)
            fails++;
    }

    public void resetFails() { fails = 0; }

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

    public boolean inPracticeMode() { return practiceSpawn != null; }

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

    public boolean inClan() { return clan != null; }


    //
    // Checkpoint Section
    //
    public void setCurrentCheckpoint(Location location)
    {
        currentCheckpoint = location;
    }

    public Location getCurrentCheckpoint()
    {
        return currentCheckpoint;
    }

    public boolean hasCurrentCheckpoint()
    {
        return currentCheckpoint != null;
    }

    public void resetCurrentCheckpoint()
    {
        currentCheckpoint = null;
    }

    public void addCheckpoint(Level level, Location location)
    {
        checkpoints.put(level.getName(), location);
    }

    public void removeCheckpoint(Level level)
    {
        checkpoints.remove(level.getName());
    }

    public Location getCheckpoint(Level level)
    {
        return checkpoints.get(level.getName());
    }

    public boolean hasCheckpoint(Level level)
    {
        return checkpoints.containsKey(level.getName());
    }


    public HashMap<String, Location> getCheckpoints() { return checkpoints; }

    //
    // Saves Section
    //
    public Location getSave(Level level)
    {
        return saves.get(level.getName());
    }

    public boolean hasSave(Level level)
    {
        return saves.containsKey(level.getName());
    }

    public void removeSave(Level level)
    {
        saves.remove(level.getName());
    }

    public void addSave(Level level, Location location)
    {
        saves.put(level.getName(), location);
    }

    //
    // Completions Section
    //
    public void levelCompletion(LevelCompletion levelCompletion)
    {
        String levelName = levelCompletion.getLevelName();

        if (levelName != null && levelCompletion != null)
        {
            if (!levelCompletions.containsKey(levelName))
                levelCompletions.put(levelName, new HashSet<>());

            if (levelCompletions.get(levelName) != null)
                levelCompletions.get(levelName).add(levelCompletion);
        }
    }

    public void levelCompletion(String levelName, long timeOfCompletion, long completionTimeElapsed)
    {
        this.levelCompletion(new LevelCompletion(levelName, uuid, name, timeOfCompletion, completionTimeElapsed));
    }

    public boolean hasCompleted(Level level)
    {
        return levelCompletions.containsKey(level.getName());
    }

    public boolean hasCompleted(String levelName)
    {
        return levelCompletions.containsKey(levelName);
    }

    public int getLevelCompletionsCount(Level level)
    {
        String levelName = level.getName();

        if (levelCompletions.containsKey(levelName))
            return levelCompletions.get(levelName).size();

        return 0;
    }

    // fastest completion
    public LevelCompletion getQuickestCompletion(Level level)
    {
        String levelName = level.getName();
        LevelCompletion fastestCompletion = null;

        if (levelCompletions.containsKey(levelName))
        {
            // loop through to find fastest completion
            for (LevelCompletion levelCompletion : levelCompletions.get(levelName))
                // if not null and not including not timed levels, continue
                if (levelCompletion != null && levelCompletion.wasTimed())
                    // if null or faster than already fastest completion, set to new completion
                    if (fastestCompletion == null || (levelCompletion.getCompletionTimeElapsedMillis() < fastestCompletion.getCompletionTimeElapsedMillis()))
                        fastestCompletion = levelCompletion;
        }
        return fastestCompletion;
    }

    //
    // Perks Section
    //
    public void addPerk(Perk perk) {
        perks.add(perk);
    }

    public boolean hasPerk(Perk perk)
    {
        return perks.contains(perk);
    }

    public int getGainedPerksCount() { return perks.size(); }

    public HashSet<Perk> getPerks() { return perks; }

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

    public void setEventWins(int eventWins) { this.eventWins = eventWins; }

    public void addEventWin() { eventWins++; }

    public int getEventWins() { return eventWins; }

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

    //
    // Grinding Section
    //
    public boolean isGrinding() {
        return grinding;
    }

    public void toggleGrinding() {
        grinding = !grinding;
    }

    public void setGrinding(boolean grinding) { this.grinding = grinding; }

    //
    // Modifier Section
    //
    public void addModifier(Modifier modifier)
    {
        // prevent same type (overwriting)
        if (!hasModifier(modifier.getType()))
            modifiers.put(modifier.getType(), modifier);
    }

    public void removeModifier(Modifier modifier)
    {
        modifiers.remove(modifier.getType());
    }

    public boolean hasModifier(ModifierType modifierType)
    {
        return modifiers.containsKey(modifierType);
    }

    public boolean hasModifierByName(Modifier targetModifier)
    {
        boolean result = false;

        for (Modifier modifier : modifiers.values())
        {
            if (modifier.equals(targetModifier))
            {
                result = true;
                break;
            }
        }
        return result;
    }

    public Collection<Modifier> getModifiers()
    {
        return modifiers.values();
    }

    public void setModifiers(HashMap<ModifierType, Modifier> modifiers)
    {
        this.modifiers = modifiers;
    }

    public Modifier getModifier(ModifierType modifierType)
    {
        return modifiers.get(modifierType);
    }

    //
    // Misc
    //
    public void clearPotionEffects() {

        for (PotionEffect potionEffect : player.getActivePotionEffects())

            // dont remove effect if its night vision, and they have it enabled
            if (!(potionEffect.getType().equals(PotionEffectType.NIGHT_VISION) && nightVision))
                player.removePotionEffect(potionEffect.getType());
    }

    public boolean equals(PlayerStats playerStats)
    {
        return playerStats.getName().equals(name);
    }

    public void sendMessage(String message)
    {
        player.sendMessage(message);
    }
}