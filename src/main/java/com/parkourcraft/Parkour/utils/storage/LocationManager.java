package com.parkourcraft.Parkour.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.parkourcraft.Parkour.storage.SaveManager;
import com.parkourcraft.Parkour.storage.local.FileManager;

import java.util.ArrayList;
import java.util.List;

public class LocationManager {

    private static FileConfiguration locations = FileManager.getFileConfig("locations");

    public static void teleport(Player player, String positionName) {
        if (exists(positionName))
            player.teleport(LocationManager.get(positionName));
    }

    public static void deletePosition(String positionName) {
        if (exists(positionName))
            locations.set(positionName, null);

        FileManager.save("locations");
    }

    public static List<String> getPositionNames() {
        List<String> positionNames = new ArrayList<String>(locations.getKeys(false));

        return positionNames;
    }

    public static void savePosition(String positionName, Location location) {
        String worldName = location.getWorld().getName();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double yaw = location.getYaw();
        double pitch = location.getPitch();

        savePosition(positionName, worldName, x, y, z, yaw, pitch);
    }

    public static void savePosition(String positionName, String worldName, double x, double y, double z, double yaw, double pitch) {
        locations.set(positionName + ".world", worldName);
        locations.set(positionName + ".x", x);
        locations.set(positionName + ".y", y);
        locations.set(positionName + ".z", z);
        locations.set(positionName + ".yaw", yaw);
        locations.set(positionName + ".pitch", pitch);

        FileManager.save("locations");
    }

    public static Location get(String positionName) {
        if (locations.isSet(positionName)) {
            World world = Bukkit.getServer().getWorld(locations.getString(positionName + ".world"));
            double x = locations.getDouble(positionName + ".x");
            double y = locations.getDouble(positionName + ".y");
            double z = locations.getDouble(positionName + ".z");
            float yaw = (float) locations.getDouble(positionName + ".yaw");
            float pitch = (float) locations.getDouble(positionName + ".pitch");
            Location location = new Location(world, x, y, z, yaw, pitch);
            return location;
        } else
            return null;
    }

    public static boolean exists(String positionName) {
        return locations.isSet(positionName);
    }

}
