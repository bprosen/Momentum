package com.parkourcraft.Parkour.storage.local;

import java.util.ArrayList;
import java.util.List;

public class FileLoader {

    private static List<String> fileNames = new ArrayList<String>() {{
        add("settings");
        add("levels");
        add("locations");
        add("menus");
        add("perks");
    }};

    public static void startUp() {
        for (String fileName : fileNames)
            FileManager.initializeFile(fileName);
    }

    public static void load() {
        for (String fileName : fileNames)
            FileManager.load(fileName);
    }

    public static void save() {
        for (String fileName : fileNames)
            FileManager.save(fileName);
    }

}
