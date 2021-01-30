package com.parkourcraft.parkour.data.locations;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationManager {

    private Map<String, Location> locations;

    public LocationManager() {
        load();
    }

    public void load() {
        locations = new HashMap<>();

        for (String locationName : Locations_YAML.getNames())
            load(locationName);

        Parkour.getPluginLogger().info("Locations loaded: " + locations.size());
    }

    public void load(String locationName) {
        Location location = Locations_YAML.get(locationName);

        if (location == null
                && exists(locationName))
            locations.remove(locationName);
        else
            locations.put(locationName, location);
    }

    public Location get(String locationName) {
        return locations.get(locationName);
    }

    public List<String> getNames() {
        return new ArrayList<>(locations.keySet());
    }

    public boolean exists(String locationName) {
        return locations.containsKey(locationName);
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