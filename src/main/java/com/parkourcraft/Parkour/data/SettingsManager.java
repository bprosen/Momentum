package com.parkourcraft.Parkour.data;

import org.bukkit.configuration.file.FileConfiguration;

public class SettingsManager {

    public String levels_message_completion;
    public String levels_message_broadcast;

    public String signs_first_line;
    public String signs_second_line_completion;
    public String signs_second_line_spawn;

    public int clans_max_members;
    public int clans_price_create;
    public int clans_price_tag;

    public SettingsManager(FileConfiguration settings) {
        load(settings);
    }

    public void load(FileConfiguration settings) {
        levels_message_completion = settings.getString("levels.message.completion");
        levels_message_broadcast = settings.getString("levels.message.broadcast");

        signs_first_line = settings.getString("signs.first_line");
        signs_second_line_completion = settings.getString("signs.second_line.completion");
        signs_second_line_spawn = settings.getString("signs.second_line.spawn");

        clans_max_members = settings.getInt("clans.max_members");
        clans_price_create = settings.getInt("clans.price.create");
        clans_price_tag = settings.getInt("clans.price.tag");
    }

}
