package com.parkourcraft.Parkour.data.locations;

import com.parkourcraft.Parkour.data.LocationManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Locations_YAML {

    private static FileConfiguration locationsFile = FileManager.getFileConfig("locations");

    private static void commit(String locationName) {
        FileManager.save("locations");
        LocationManager.load(locationName);
    }

    private static boolean exists(String locationName) {
        return locationsFile.isSet(locationName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(locationsFile.getKeys(false));
    }

    public static Location get(String locationName) {
        if (exists(locationName)) {
            World world = Bukkit.getServer().getWorld(locationsFile.getString(locationName + ".world"));

            double x = locationsFile.getDouble(locationName + ".x");
            double y = locationsFile.getDouble(locationName + ".y");
            double z = locationsFile.getDouble(locationName + ".z");
            float yaw = (float) locationsFile.getDouble(locationName + ".yaw");
            float pitch = (float) locationsFile.getDouble(locationName + ".pitch");

            return new Location(world, x, y, z, yaw, pitch);
        }

        return null;
    }

    public static void save(String locationName, Location location) {
        String worldName = location.getWorld().getName();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double yaw = location.getYaw();
        double pitch = location.getPitch();

        save(locationName, worldName, x, y, z, yaw, pitch);
    }

    private static void save(String locationName, String worldName, double x, double y, double z, double yaw, double pitch) {
        locationsFile.set(locationName + ".world", worldName);
        locationsFile.set(locationName + ".x", x);
        locationsFile.set(locationName + ".y", y);
        locationsFile.set(locationName + ".z", z);
        locationsFile.set(locationName + ".yaw", yaw);
        locationsFile.set(locationName + ".pitch", pitch);

        commit(locationName);
    }

    public static void remove(String locationName) {
        if (exists(locationName)) {
            locationsFile.set(locationName, null);

            commit(locationName);
        }
    }

}
