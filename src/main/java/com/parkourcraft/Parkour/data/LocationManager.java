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

    private Map<String, Location> locationsMap;

    public LocationManager() {
        load();
    }

    public void load() {
        locationsMap = new HashMap<>();

        for (String locationName : Locations_YAML.getNames())
            load(locationName);

        Parkour.getPluginLogger().info("Locations loaded: " + locationsMap.size());
    }

    public void load(String locationName) {
        Location location = Locations_YAML.get(locationName);

        if (location == null
                && exists(locationName))
            locationsMap.remove(locationName);
        else
            locationsMap.put(locationName, location);
    }

    public Location get(String locationName) {
        return locationsMap.get(locationName);
    }

    public List<String> getNames() {
        return new ArrayList<>(locationsMap.keySet());
    }

    public boolean exists(String locationName) {
        return locationsMap.containsKey(locationName);
    }

    public void teleport(Player player, String locationName) {
        if (exists(locationName))
            player.teleport(get(locationName));
    }

    public void save(String locationName, Location location) {
        Locations_YAML.save(locationName, location);
    }

    public void remove(String locationName) {
        if (exists(locationName))
            Locations_YAML.remove(locationName);
    }

    public Location getLobbyLocation() {
        return get("spawn");
    }

}
