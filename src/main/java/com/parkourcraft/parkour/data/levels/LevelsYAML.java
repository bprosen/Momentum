package com.parkourcraft.parkour.data.levels;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.stats.StatsDB;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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

        if (exists(levelName) && isSet(levelName, "rising_water_y_below_starting"))
            risingWaterY = levelsFile.getInt(levelName + ".rising_water_y_below_starting");

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
        if (isSet(levelName, "liquids_reset_players"))
            return false;
        return true;
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
        if (levelsFile.isSet(levelName + ".event"))
            return EventType.valueOf(levelsFile.getString(levelName + ".event"));
        return null;
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

    public static void setDropperRespawnY(String levelName, int newY) {
        levelsFile.set(levelName + ".dropper_respawn_y", newY);
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

    public static boolean isDropperLevel(String levelName) {
        boolean dropper = false;
        if (isSet(levelName, "dropper"))
            dropper = levelsFile.getBoolean(levelName + ".dropper");

        return dropper;
    }

    public static void toggleDropperLevel(String levelName) {
        boolean dropper = isDropperLevel(levelName);
        levelsFile.set(levelName + ".dropper", !dropper);
        commit(levelName);
    }

    public static int getDropperRespawnY(String levelName) {
        int dropperY = -1;
        if (isSet(levelName, "dropper_respawn_y"))
            dropperY = levelsFile.getInt(levelName + ".dropper_respawn_y");

        return dropperY;
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

    public static List<String> getCommands(String levelName) {
        List<String> tempList = new ArrayList<>();

        if (levelsFile.isList(levelName + ".commands"))
            tempList = levelsFile.getStringList(levelName + ".commands");

        return tempList;
    }
}
