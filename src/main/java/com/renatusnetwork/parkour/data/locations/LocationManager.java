package com.renatusnetwork.parkour.data.locations;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;

public class LocationManager {

    private HashMap<String, Location> locations;

    public LocationManager() {
        load();
    }

    public void load()
    {
        locations = LocationsDB.loadLocations();

        Parkour.getPluginLogger().info("Locations loaded: " + locations.size());
    }

    public void load(String locationName)
    {
        locations.put(locationName, LocationsDB.loadLocation(locationName));
    }

    public void add(String locationName, Location location)
    {
        // update if it exists, insert if not
        if (exists(locationName))
            LocationsDB.updateLocation(locationName, location);
        else
            LocationsDB.insertLocation(locationName, location);

        locations.put(locationName, location);
    }

    public Location get(String locationName) {
        return locations.get(locationName);
    }

    public boolean exists(String locationName) {
        return locations.containsKey(locationName);
    }

    public void teleport(Player player, String locationName)
    {
        Location location = get(locationName);

        if (location != null)
            player.teleport(location);
    }

    public void remove(String locationName)
    {
        locations.remove(locationName);
        LocationsDB.removeLocation(locationName);
    }

    public boolean hasCompletionLocation(String levelName) {
        return exists(levelName + "-completion");
    }

    public boolean hasSpawnLocation(String levelName) {
        return exists(levelName + "-spawn");
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
            case BLACK_MARKET:
                portalLoc = locations.get(SettingsManager.BLACK_MARKET_PORTAL_NAME);
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

    public int numLocations() { return locations.size(); }

    public Location getLobbyLocation() {
        return get("spawn");
    }

    public Location getTutorialLocation() { return get("tutorial"); }
}
