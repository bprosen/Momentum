package com.parkourcraft.parkour.storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private Map<String, File> files = new HashMap<>();
    private Map<String, FileConfiguration> configs = new HashMap<>();

    public ConfigManager(Plugin plugin) {
        initialize("settings", plugin);
        initialize("levels", plugin);
        initialize("locations", plugin);
        initialize("menus", plugin);
        initialize("perks", plugin);
        initialize("ranks", plugin);
    }

    private void initialize(String fileName, Plugin plugin) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        FileConfiguration fileConfig = new YamlConfiguration();

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                copy(plugin.getResource("config/" + fileName + ".yml"), file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        files.put(fileName, file);
        configs.put(fileName, fileConfig);

        load(fileName);
    }

    private void copy(InputStream in, File file) {
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

    public FileConfiguration get(String fileName) {
        FileConfiguration fileConfig = configs.get(fileName);
        if (fileConfig != null)
            return fileConfig;
        return null;
    }

    public void load(String fileName) {
        try {
            configs.get(fileName).load(files.get(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String fileName) {
        try {
            configs.get(fileName).save(files.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
