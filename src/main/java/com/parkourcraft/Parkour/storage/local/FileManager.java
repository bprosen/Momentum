package com.parkourcraft.Parkour.storage.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.parkourcraft.Parkour.Parkour;

public class FileManager {


    private static Map<String, FileConfiguration> fileConfigsMap = new HashMap<String, FileConfiguration>();
    private static Map<String, File> filesMap = new HashMap<String, File>();


    public static void printInfo() {
        for (File file : filesMap.values()) {
            Parkour.getPluginLogger().info("PRINT#1  path: " + file.getPath());
        }

    }

    public static FileConfiguration getFileConfig(String fileName) {
        FileConfiguration fileConfig = fileConfigsMap.get(fileName);
        if (fileConfig != null)
            return fileConfig;
        return null;
    }

    public static void initializeFile(String fileName) {
        File file = new File(Parkour.getPlugin().getDataFolder(), fileName + ".yml");
        FileConfiguration fileConfig = new YamlConfiguration();

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                copy(Parkour.getPlugin().getResource("config/" + fileName + ".yml"), file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileConfigsMap.put(fileName, fileConfig);
        filesMap.put(fileName, file);
        load(fileName);
    }

    public static void load(String fileName) {
        try {
            fileConfigsMap.get(fileName).load(filesMap.get(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save(String fileName) {
        try {
            fileConfigsMap.get(fileName).save(filesMap.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
