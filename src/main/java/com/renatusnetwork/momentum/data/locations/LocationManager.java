package com.renatusnetwork.momentum.data.locations;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationManager {

    private HashMap<String, Location> locations;
    private Location lobbyLocation;
    private Location tutorialLocation;

    public LocationManager() {
        load();
    }

    public void load()
    {
        this.locations = LocationsDB.loadLocations();
        reloadCachedLocations();

        Momentum.getPluginLogger().info("Locations loaded: " + locations.size());
    }

    public void reloadCachedLocations()
    {
        this.lobbyLocation = get("spawn");
        this.tutorialLocation = get("tutorial");
    }

    public void load(String locationName)
    {
        locations.put(locationName, LocationsDB.loadLocation(locationName));
    }

    public void set(String locationName, Location location)
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

    public void updateName(String oldLocationName, String newLocationName)
    {
        Location location = locations.get(oldLocationName);

        if (location != null)
        {
            // remove and replace
            locations.remove(oldLocationName);
            locations.put(newLocationName, location);

            LocationsDB.updateLocationName(oldLocationName, newLocationName);
        }
    }

    public HashMap<Integer, Location> getAscentLevelLocations(String levelName)
    {
        int num = 1;
        boolean exists = true;
        HashMap<Integer, Location> tempMap = new HashMap<>();

        // keep going until location doesnt exist
        while (exists)
        {
            String locationName = SettingsManager.LEVEL_ASCENT_FORMAT.replace("%level%", levelName).replace("%num%", String.valueOf(num));

            Location location = get(locationName);
            exists = location != null;
            tempMap.put(num, location);

            num++;
        }
        return tempMap;
    }

    public List<Location> getMazeLocations(String levelName, boolean respawnLocations, boolean exitLocations)
    {
        int num = 1;
        boolean exists = true;
        List<Location> temp = new ArrayList<>();

        // keep going until location doesnt exist
        while (exists)
        {
            String locationName = null;

            // get different types (they have the same formatting)
            if (respawnLocations)
                locationName = SettingsManager.LEVEL_MAZE_RESPAWN_FORMAT.replace("%level%", levelName).replace("%num%", String.valueOf(num));
            else if (exitLocations)
                locationName = SettingsManager.LEVEL_MAZE_EXIT_FORMAT.replace("%level%", levelName).replace("%num%", String.valueOf(num));

            if (locationName != null)
            {
                Location location = get(locationName);
                exists = location != null;
                temp.add(location);

                num++;
            }
        }
        return temp;
    }

    public boolean hasCompletionLocation(String levelName)
    {
        return exists(SettingsManager.LEVEL_COMPLETION_FORMAT.replace("%level%", levelName));
    }

    public boolean hasSpawnLocation(String levelName) {
        return exists(SettingsManager.LEVEL_SPAWN_FORMAT.replace("%level%", levelName));
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
        return lobbyLocation;
    }

    public Location getTutorialLocation() { return tutorialLocation; }

    public boolean equals(Location one, Location two)
    {
        return one.getWorld().getName().equalsIgnoreCase(two.getWorld().getName()) &&
               one.getX() == two.getX() &&
               one.getY() == two.getY() &&
               one.getZ() == two.getZ();
    }
}
