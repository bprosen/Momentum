package com.parkourcraft.Parkour.utils.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.parkourcraft.Parkour.storage.SaveManager;
import com.parkourcraft.Parkour.storage.local.FileManager;

public class LocationManager {

    private static FileConfiguration locations = FileManager.getFileConfig("locations");

    public static void teleport(Player player, String positionName) {
        if (exists(positionName))
            player.teleport(LocationManager.get(positionName));
    }

    public static void deletePosition(String positionName) {
        if (exists(positionName))
            locations.set(positionName, null);
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
        locations.set("location." + positionName + ".world", worldName);
        locations.set("location." + positionName + ".x", x);
        locations.set("location." + positionName + ".y", y);
        locations.set("location." + positionName + ".z", z);
        locations.set("location." + positionName + ".yaw", yaw);
        locations.set("location." + positionName + ".pitch", pitch);

        SaveManager.addChange("locations");
    }

    public static Location get(String positionName) {
        if (locations.isSet("location." + positionName)) {
            World world = Bukkit.getServer().getWorld(locations.getString("location." + positionName + ".world"));
            double x = locations.getDouble("location." + positionName + ".x");
            double y = locations.getDouble("location." + positionName + ".y");
            double z = locations.getDouble("location." + positionName + ".z");
            float yaw = (float) locations.getDouble("location." + positionName + ".yaw");
            float pitch = (float) locations.getDouble("location." + positionName + ".pitch");
            Location location = new Location(world, x, y, z, yaw, pitch);
            return location;
        } else
            return null;
    }

    public static boolean exists(String positionName) {
        return FileManager.getFileConfig("locations").isSet("location." + positionName);
    }

}
