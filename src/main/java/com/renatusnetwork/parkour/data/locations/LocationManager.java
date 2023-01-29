package com.renatusnetwork.parkour.data.locations;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
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

    public boolean hasPortalLocation(String levelName)
    {
        return exists(levelName + "-portal");
    }

    public boolean isNearPortal(double playerX, double playerY, double playerZ, double radius, PortalType portalType)
    {
        boolean inPortal = false;
        Location portalLoc = null;

        // portal type
        switch (portalType)
        {
            case INFINITE:
                portalLoc = locations.get(SettingsManager.INFINITE_PORTAL_NAME);
                break;
            case ASCENDANCE:
                portalLoc = locations.get(SettingsManager.ASCENDANCE_PORTAL_NAME);
                break;
        }

        if (portalLoc != null) {

            // booleans for all radius
            boolean inX = ((portalLoc.getBlockX() + radius) >= ((int) playerX)) && ((portalLoc.getBlockX() - radius) <= ((int) playerX));
            boolean inY = ((portalLoc.getBlockY() + radius) >= ((int) playerY)) && ((portalLoc.getBlockY() - radius) <= ((int) playerY));
            boolean inZ = ((portalLoc.getBlockZ() + radius) >= ((int) playerZ)) && ((portalLoc.getBlockZ() - radius) <= ((int) playerZ));

            if (inX && inY && inZ)
                inPortal = true;
        }
        return inPortal;
    }

    public HashMap<String, Location> getLocations() { return locations; }

    public Location getLobbyLocation() {
        return get("spawn");
    }

}
