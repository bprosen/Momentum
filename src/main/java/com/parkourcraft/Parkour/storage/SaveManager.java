package com.parkourcraft.Parkour.storage;

import java.util.ArrayList;
import java.util.List;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.storage.local.FileManager;

public class SaveManager {

    private static List<String> filesChanged = new ArrayList<String>();

    public static boolean getStatus() {
        if (filesChanged.size() > 0)
            return true;
        return false;
    }

    public static void addChange(String changedFile) {
        if (!filesChanged.contains(changedFile))
            filesChanged.add(changedFile);
    }

    public static void makeSaves() {
        if (filesChanged.size() > 0) {
            String savesOneLine = "";
            for (String fileName : filesChanged) {
                savesOneLine = savesOneLine + fileName + ".yml ";
                FileManager.save(fileName);
            }
            Parkour.getPluginLogger().info("File saves made to: " + savesOneLine);
            filesChanged = new ArrayList<String>();
        }
    }

}
