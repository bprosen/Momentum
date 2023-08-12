package com.renatusnetwork.parkour.data.levels;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.types.EventType;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LevelsYAML {

    private static FileConfiguration levelsFile = Parkour.getConfigManager().get("levels");

    public static void commit(String levelName) {
        Parkour.getConfigManager().save("levels");
        Parkour.getLevelManager().load(levelName);

        // run total completions and leaderboard regen in async
        new BukkitRunnable() {
            @Override
            public void run() {
                Level level = Parkour.getLevelManager().get(levelName);

                if (level != null) {
                    StatsDB.loadTotalCompletions(level);
                    StatsDB.loadLeaderboard(level);
                }
            }
        }.runTaskAsynchronously(Parkour.getPlugin());
    }

    public static boolean exists(String levelName) {
        return levelsFile.isSet(levelName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(levelsFile.getKeys(false));
    }

    public static void renameLevel(String levelName, String newLevelName) {
        HashMap<String, Object> pathList = new HashMap<>();

        for (String string : levelsFile.getConfigurationSection(levelName).getKeys(true))
            if (!levelsFile.isConfigurationSection(levelName + "." + string))
                pathList.put(string, levelsFile.get(levelName + "." + string));

        levelsFile.set(levelName, null);

        for (Map.Entry<String, Object> entry : pathList.entrySet())
            levelsFile.set(newLevelName + "." + entry.getKey(), entry.getValue());

        commit(newLevelName);
    }

    public static boolean isSet(String levelName, String valueName) {
        return levelsFile.isSet(levelName + "." + valueName);
    }

    public static boolean isSection(String levelName, String valueName) {
        return levelsFile.isConfigurationSection(levelName + "." + valueName);
    }

    public static void create(String levelName) {
        if (!exists(levelName)) {
            levelsFile.set(levelName + ".title", levelName);

            commit(levelName);
        }
    }

    public static void remove(String levelName) {
        if (exists(levelName)) {
            levelsFile.set(levelName, null);

            commit(levelName);
        }
    }

    public static void setTitle(String levelName, String title) {
        if (exists(levelName)) {
            levelsFile.set(levelName + ".title", title);

            commit(levelName);
        }
    }

    public static void toggleWaterReset(String levelName) {
        if (exists(levelName)) {
            if (isSet(levelName, "liquids_reset_players"))
                levelsFile.set(levelName + ".liquids_reset_players", null);
            else
                levelsFile.set(levelName + ".liquids_reset_players", false);

            commit(levelName);
        }
    }

    public static int getRisingWaterStartingMinusY(String levelName) {
        // 10 is default
        int risingWaterY = 10;

        if (levelsFile.isConfigurationSection(levelName + ".event"))
            risingWaterY = levelsFile.getInt(levelName + ".event.y_below_starting");

        return risingWaterY;
    }

    public static void setMessage(String levelName, String message) {
        if (exists(levelName)) {
            if (message.equals("default"))
                levelsFile.set(levelName + ".message", null);
            else
                levelsFile.set(levelName + ".message", message);

            commit(levelName);
        }
    }

    public static void setMaxCompletions(String levelName, int maxCompletions) {
        if (exists(levelName)) {
            if (maxCompletions == -1)
                levelsFile.set(levelName + ".max_completions", null);
            else
                levelsFile.set(levelName + ".max_completions", maxCompletions);

            commit(levelName);
        }
    }

    public static void setBroadcast(String levelName, boolean setting) {
        if (exists(levelName)) {
            if (!setting)
                levelsFile.set(levelName + ".broadcast_completion", null);
            else
                levelsFile.set(levelName + ".broadcast_completion", setting);

            commit(levelName);
        }
    }

    public static void setRequiredLevels(String levelName, List<String> requiredLevels) {
        if (exists(levelName)) {
            levelsFile.set(levelName + ".required_levels", requiredLevels);

            commit(levelName);
        }
    }

    public static List<String> getRequiredLevels(String levelName) {
        if (isSet(levelName, "required_levels"))
            return levelsFile.getStringList(levelName + ".required_levels");

        return new ArrayList<>();
    }

    public static boolean getRankUpLevelSwitch(String levelName) {
        if (levelsFile.isSet(levelName + ".rankup_level"))
            return levelsFile.getBoolean(levelName + ".rankup_level");
        return false;
    }

    public static String getTitle(String levelName) {
        if (levelsFile.isSet(levelName + ".title"))
            return levelsFile.getString(levelName + ".title");
        return "";
    }

    public static String getMessage(String levelName) {
        if (isSet(levelName, "message"))
            return levelsFile.getString(levelName + ".message");
        return "";
    }

    public static int getMaxCompletions(String levelName) {
        if (isSet(levelName, "max_completions"))
            return levelsFile.getInt(levelName + ".max_completions");
        return -1;
    }

    public static boolean getBroadcastSetting(String levelName) {
        if (isSet(levelName, "broadcast_completion"))
            return levelsFile.getBoolean(levelName + ".broadcast_completion");

        return false;
    }

    public static boolean getLiquidResetSetting(String levelName) {
        // WE WANT DEFAULT TO BE TRUE!
        return !isSet(levelName, "liquids_reset_players");
    }

    public static HashMap<Integer, Location> getAscentLevelLocations(String levelName)
    {
        HashMap<Integer, Location> locations = new HashMap<>();

        if (levelsFile.isConfigurationSection(levelName + ".event"))
        {
            int id = 1;
            for (String location : levelsFile.getStringList(levelName + ".event.ascent_locations"))
            {
                locations.put(id, Parkour.getLocationManager().get(location));
                id++;
            }
        }

        return locations;
    }

    public static List<PotionEffect> getPotionEffects(String levelName) {
        List<PotionEffect> potionEffects = new ArrayList<>();

        if (isSection(levelName, "potion_effects")) {
            for (int i = 1;; i++) {
                if (isSection(levelName, "potion_effects." + i)) {

                    String potionType = levelsFile.getString(levelName + ".potion_effects." + i + ".type");
                    int amplifier = levelsFile.getInt(levelName + ".potion_effects." + i + ".amplifier");
                    int duration = levelsFile.getInt(levelName + ".potion_effects." + i + ".duration");

                    potionEffects.add(
                            new PotionEffect(
                                    PotionEffectType.getByName(potionType), duration * 20, amplifier));
                } else {
                    break;
                }
            }
        }
        return potionEffects;
    }

    public static String getRequiredPermissionNode(String levelName) {
        if (levelsFile.isSet(levelName + ".required_permission_node"))
            return levelsFile.getString(levelName + ".required_permission_node");
        return null;
    }

    public static EventType getEventType(String levelName) {
        if (levelsFile.isConfigurationSection(levelName + ".event"))
            return EventType.valueOf(levelsFile.getString(levelName + ".event.type").toUpperCase());
        return null;
    }

    public static Location getRandomMazeEventExit(String levelName) {
        if (levelsFile.isConfigurationSection(levelName + ".event"))
        {
            List<String> locs = levelsFile.getStringList(levelName + ".event.exits");
            return Parkour.getLocationManager().get(locs.get(ThreadLocalRandom.current().nextInt(locs.size())));
        }
        return null;
    }

    public static List<Location> getMazeRespawns(String levelName)
    {
        List<Location> locs = new ArrayList<>();

        if (levelsFile.isConfigurationSection(levelName + ".event"))
            for (String respawn : levelsFile.getStringList(levelName + ".event.respawns"))
                locs.add(Parkour.getLocationManager().get(respawn));

        return locs;
    }

    public static int getRespawnY(String levelName) {
        if (levelsFile.isSet(levelName + ".respawn_y"))
            return levelsFile.getInt(levelName + ".respawn_y");
        return -1;
    }

    public static void setRespawnY(String levelName, int newY) {
        levelsFile.set(levelName + ".respawn_y", newY);
        commit(levelName);
    }

    public static boolean isElytraLevel(String levelName) {
        boolean elytra = false;
        if (isSet(levelName, "elytra"))
            elytra = levelsFile.getBoolean(levelName + ".elytra");

        return elytra;
    }

    public static void toggleElytraLevel(String levelName) {
        boolean elytra = isElytraLevel(levelName);
        levelsFile.set(levelName + ".elytra", !elytra);
        commit(levelName);
    }

    public static void setDifficulty(String levelName, int difficulty)
    {
        levelsFile.set(levelName + ".difficulty", difficulty);
        commit(levelName);
    }

    public static int getDifficulty(String levelName)
    {
        int difficulty = -1;

        if (isSet(levelName, "difficulty"))
        {
            difficulty = levelsFile.getInt(levelName + ".difficulty");

            if (difficulty < 1)
                difficulty = 1;

            if (difficulty > 10)
                difficulty = 10;
        }

        return difficulty;
    }

    public static void toggleCooldown(String levelName)
    {
        boolean cooldown = hasCooldown(levelName);
        levelsFile.set(levelName + ".cooldown", !cooldown);
        commit(levelName);
    }

    public static boolean hasCooldown(String levelName)
    {
        boolean cooldown = false;

        // get if set
        if (isSet(levelName, "cooldown"))
            cooldown = levelsFile.getBoolean(levelName + ".cooldown");

        return cooldown;
    }

    public static boolean isDropperLevel(String levelName) {
        boolean dropper = false;
        if (isSet(levelName, "dropper"))
            dropper = levelsFile.getBoolean(levelName + ".dropper");

        return dropper;
    }

    public static boolean isTCLevel(String levelName) {
        boolean tc = false;
        if (isSet(levelName, "tc"))
            tc = levelsFile.getBoolean(levelName + ".tc");

        return tc;
    }

    public static void toggleTCLevel(String levelName) {
        boolean tc = isTCLevel(levelName);
        levelsFile.set(levelName + ".tc", !tc);
        commit(levelName);
    }

    public static void toggleDropperLevel(String levelName) {
        boolean dropper = isDropperLevel(levelName);
        levelsFile.set(levelName + ".dropper", !dropper);
        commit(levelName);
    }

    public static boolean isAscendanceLevel(String levelName) {
        boolean ascendance = false;
        if (isSet(levelName, "ascendance"))
            ascendance = levelsFile.getBoolean(levelName + ".ascendance");

        return ascendance;
    }

    public static void toggleAscendanceLevel(String levelName) {
        boolean ascendance = isAscendanceLevel(levelName);
        levelsFile.set(levelName + ".ascendance", !ascendance);
        commit(levelName);
    }

    public static void setPlayerRaceLocation(String player, String levelName, Location loc) {

        String world = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();

        levelsFile.set(levelName + ".race." + player + "_loc", world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch);
        commit(levelName);
    }

    public static Location getPlayerRaceLocation(String player, String levelName) {

        String[] locStringSplit = levelsFile.getString(levelName + ".race." + player + "_loc").split(":");
        World world = Bukkit.getWorld(locStringSplit[0]);
        double x = Double.parseDouble(locStringSplit[1]);
        double y = Double.parseDouble(locStringSplit[2]);
        double z = Double.parseDouble(locStringSplit[3]);
        float yaw = Float.parseFloat(locStringSplit[4]);
        float pitch = Float.parseFloat(locStringSplit[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Material getRaceMenuItemType(String levelName) {
        return Material.matchMaterial(levelsFile.getString(levelName + ".race.menu_item"));
    }

    public static int getPrice(String levelName)
    {
        int price = 0;

        if (isSet(levelName, "price"))
            price = levelsFile.getInt(levelName + ".price");

        return price;
    }

    public static boolean getNewLevel(String levelName)
    {
        boolean result = false;

        if (isSet(levelName, "new"))
            result = levelsFile.getBoolean(levelName + ".new");

        return result;
    }

    public static Rank getRankRequired(String levelName)
    {
        Rank rank = null;

        if (isSet(levelName, "required_rank"))
            rank = Parkour.getRanksManager().get(levelsFile.getString(levelName + ".required_rank"));

        return rank;
    }

    public static void setNewLevel(String levelName, boolean result)
    {
        if (result)
            levelsFile.set(levelName + ".new", result);
        else
            levelsFile.set(levelName + ".new", null); // remove

        commit(levelName);
    }

    public static void setPrice(String levelName, int price) {
        levelsFile.set(levelName + ".price", price);
        commit(levelName);
    }

    public static List<String> getCommands(String levelName) {
        List<String> tempList = new ArrayList<>();

        if (levelsFile.isList(levelName + ".commands"))
            tempList = levelsFile.getStringList(levelName + ".commands");

        return tempList;
    }
}
