package com.parkourcraft.parkour.data;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    public int clans_tag_length_min;
    public int clans_tag_length_max;
    public int clan_calc_percent_min;
    public int clan_calc_percent_max;
    public int clan_calc_level_reward_needed;
    public int clan_split_reward_min_needed;

    public String player_submitted_world;
    public int player_submitted_plot_width;
    public int player_submitted_plot_default_y;
    public int player_submitted_plot_buffer_width;

    public Location spawn_location = null;

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
        clans_tag_length_min = settings.getInt("clans.tag_length.min");
        clans_tag_length_max = settings.getInt("clans.tag_length.max");
        clan_calc_percent_min = settings.getInt("clans.clan_xp_calc.min-percent");
        clan_calc_percent_max = settings.getInt("clans.clan_xp_calc.max-percent");
        clan_calc_level_reward_needed = settings.getInt("clans.clan_xp_calc.level-reward-needed");
        clan_split_reward_min_needed = settings.getInt("clans.split_reward_min_needed");
        player_submitted_world = settings.getString("player_submitted.world");
        player_submitted_plot_width = settings.getInt("player_submitted.plot_width");
        player_submitted_plot_default_y = settings.getInt("player_submitted.plot_default_y");
        player_submitted_plot_buffer_width = settings.getInt("player_submitted.plot_buffer_width");
        loadSpawn();
    }

    public void loadSpawn() {

        String locationString = Parkour.getConfigManager().get("settings").getString("spawn.location");

        if (locationString != null) {

            String[] locStringSplit = locationString.split(":");
            World world = Bukkit.getWorld(locStringSplit[0]);
            double x = Double.parseDouble(locStringSplit[1]);
            double y = Double.parseDouble(locStringSplit[2]);
            double z = Double.parseDouble(locStringSplit[3]);
            float yaw = Float.parseFloat(locStringSplit[4]);
            float pitch = Float.parseFloat(locStringSplit[5]);

            spawn_location = new Location(world, x, y, z, yaw, pitch);
        }
    }
}
