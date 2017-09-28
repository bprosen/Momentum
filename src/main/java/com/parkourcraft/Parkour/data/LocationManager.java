package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.locations.Locations_YAML;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManager {

    private static Map<String, Location> locationsMap = new HashMap<>();

    public static void load(String locationName) {
        Location location = Locations_YAML.get(locationName);

        if (location == null
                && exists(locationName))
            locationsMap.remove(locationName);
        else
            locationsMap.put(locationName, location);
    }

    public static void loadLocations() {
        locationsMap = new HashMap<>();

        for (String locationName : Locations_YAML.getNames())
            load(locationName);

        Parkour.getPluginLogger().info("Locations loaded: " + locationsMap.size());
    }

    public static Location get(String locationName) {
        return locationsMap.get(locationName);
    }

    public static List<String> getNames() {
        return new ArrayList<>(locationsMap.keySet());
    }

    public static boolean exists(String locationName) {
        return locationsMap.containsKey(locationName);
    }

    public static void teleport(Player player, String locationName) {
        if (exists(locationName))
            player.teleport(get(locationName));
    }

    public static void save(String locationName, Location location) {
        Locations_YAML.save(locationName, location);
    }

    public static void remove(String locationName) {
        if (exists(locationName))
            Locations_YAML.remove(locationName);
    }

    public static Location getLobbyLocation() {
        return LocationManager.get("spawn");
    }

}
