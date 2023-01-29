package com.renatusnetwork.parkour.data.locations;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class LocationsYAML {

    private static FileConfiguration locationsFile = Parkour.getConfigManager().get("locations");

    private static void commit(String locationName) {
        Parkour.getConfigManager().save("locations");
        Parkour.getLocationManager().load(locationName);
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

    public static void renameLocation(String locationName, String newLocationName) {

        String ogLocationName = locationName;
        String ogNewLocationName = newLocationName;

        Set<String> pathTypes = new HashSet<String>() {{
            add("-spawn");
            add("-completion");
            add("-portal");
        }};

        for (String pathType : pathTypes) {
            if (locationsFile.isConfigurationSection(locationName + pathType)) {

                locationName += pathType;
                newLocationName += pathType;

                HashMap<String, Object> pathList = new HashMap<>();

                for (String string : locationsFile.getConfigurationSection(locationName).getKeys(true))
                    if (!locationsFile.isConfigurationSection(locationName + "." + string))
                        pathList.put(string, locationsFile.get(locationName + "." + string));

                locationsFile.set(locationName, null);

                for (Map.Entry<String, Object> entry : pathList.entrySet())
                    locationsFile.set(newLocationName + "." + entry.getKey(), entry.getValue());
            }
            locationName = ogLocationName;
            newLocationName = ogNewLocationName;
        }
        commit(newLocationName);
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