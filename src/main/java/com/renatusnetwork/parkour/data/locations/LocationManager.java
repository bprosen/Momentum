package com.renatusnetwork.parkour.data.locations;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationManager {

    private HashMap<String, Location> locations;

    public LocationManager() {
        load();
    }

    public void load() {
        locations = new HashMap<>();

        for (String locationName : LocationsYAML.getNames())
            load(locationName);

        Parkour.getPluginLogger().info("Locations loaded: " + locations.size());
    }

    public void load(String locationName) {
        Location location = LocationsYAML.get(locationName);

        if (location == null && exists(locationName))
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
        LocationsYAML.save(locationName, location);
    }

    public void remove(String locationName) {
        if (exists(locationName))
            LocationsYAML.remove(locationName);
    }

    public boolean hasCompletionLocation(String levelName) {
        if (exists(levelName + "-completion"))
            return true;
        return false;
    }

    public boolean hasSpawnLocation(String levelName) {
        if (exists(levelName + "-spawn"))
            return true;
        return false;
    }

    public HashMap<String, Location> getLocations() { return locations; }

    public Location getLobbyLocation() {
        return get("spawn");
    }

}
