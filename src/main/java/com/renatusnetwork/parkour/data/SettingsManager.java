package com.renatusnetwork.parkour.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SettingsManager {

    // constants
    public static final String INFINITE_PORTAL_NAME = "infinite-portal";
    public static final String ASCENDANCE_PORTAL_NAME = "ascendance-portal";

    public String levels_message_completion;
    public String levels_message_broadcast;
    public double featured_level_reward_multiplier;
    public int max_rated_levels_leaderboard_size;

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
    public List<String> blocked_clan_names = new ArrayList<>();

    public String player_submitted_world;
    public int player_submitted_plot_width;
    public int player_submitted_plot_default_y;
    public int player_submitted_plot_buffer_width;
    public String minimum_rank_for_plot_creation;

    public int event_reminder_delay;
    public int max_event_run_time;
    public int check_next_event_delay;
    public int min_players_online;
    public double anvil_spawn_percentage;
    public int anvil_spawn_y_above_start_y;
    public int pvp_event_task_delay;
    public int half_heart_event_task_delay;
    public int rising_water_event_task_delay;

    public Material sword_type;
    public String sword_title;
    public int sword_hotbar_slot;

    public int max_infinitepk_leaderboard_size;
    public int max_infinitepk_x;
    public int max_infinitepk_y;
    public int max_infinitepk_z;
    public int infinitepk_starting_y;
    public int min_infinitepk_y;
    public float infinitepk_starting_pitch;
    public float infinitepk_starting_yaw;
    public Location infinitepk_portal_respawn;

    public int max_global_level_completions_leaderboard_size;
    public int max_global_personal_completions_leaderboard_size;
    public int max_clans_leaderboard_size;
    public int max_race_leaderboard_size;
    public int max_coins_leaderboard_size;

    public int max_prestige_multiplier;
    public int prestige_multiplier_per_prestige;

    public double min_race_bet_amount;

    public String ascendant_realm_world;
    public String ascendance_hub_level;

    public double rage_quit_price;
    public List<String> rage_quit_messages;

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
        blocked_clan_names = settings.getStringList("clans.blocked_clan_names");
        player_submitted_world = settings.getString("player_submitted.world");
        player_submitted_plot_width = settings.getInt("player_submitted.plot_width");
        player_submitted_plot_default_y = settings.getInt("player_submitted.plot_default_y");
        player_submitted_plot_buffer_width = settings.getInt("player_submitted.plot_buffer_width");
        event_reminder_delay = settings.getInt("event.reminder_delay");
        max_event_run_time = settings.getInt("event.max_run_time");
        check_next_event_delay = settings.getInt("event.check_next_event_delay");
        min_players_online = settings.getInt("event.min_players_online");
        anvil_spawn_percentage = settings.getDouble("event.anvil_spawn_percentage");
        anvil_spawn_y_above_start_y = settings.getInt("event.anvil_spawn_y_above_start_y");
        pvp_event_task_delay = settings.getInt("event.task_delay.pvp");
        half_heart_event_task_delay = settings.getInt("event.task_delay.half_heart");
        rising_water_event_task_delay = settings.getInt("event.task_delay.rising_water");
        minimum_rank_for_plot_creation = settings.getString("player_submitted.minimum_rank_for_plot_creation");
        featured_level_reward_multiplier = settings.getDouble("levels.featured_level_reward_multiplier");
        sword_title = settings.getString("setup-sword.title");
        sword_type = Material.matchMaterial(settings.getString("setup-sword.type"));
        sword_hotbar_slot = settings.getInt("setup-sword.hotbar_slot");
        max_infinitepk_leaderboard_size = settings.getInt("infinitepk.max_leaderboard_size");
        max_infinitepk_x = settings.getInt("infinitepk.max_x");
        max_infinitepk_y = settings.getInt("infinitepk.max_y");
        max_infinitepk_z = settings.getInt("infinitepk.max_z");
        min_infinitepk_y = settings.getInt("infinitepk.min_y");
        infinitepk_starting_y = settings.getInt("infinitepk.starting_y");
        infinitepk_starting_pitch = (float) settings.getDouble("infinitepk.starting_pitch");
        infinitepk_starting_yaw = (float) settings.getDouble("infinitepk.starting_yaw");
        max_global_level_completions_leaderboard_size = settings.getInt("completions.global_level_completions_leaderboard.max_size");
        max_global_personal_completions_leaderboard_size = settings.getInt("completions.global_personal_completions_leaderboard.max_size");
        max_clans_leaderboard_size = settings.getInt("clans.max_leaderboard_size");
        max_coins_leaderboard_size = settings.getInt("stats.max_coins_leaderboard_size");
        max_prestige_multiplier = settings.getInt("prestiges.max_multiplier");
        prestige_multiplier_per_prestige = settings.getInt("prestiges.multiplier_per_prestige");
        max_rated_levels_leaderboard_size = settings.getInt("levels.max_rated_levels_leaderboard_size");
        max_race_leaderboard_size = settings.getInt("races.max_leaderboard_size");
        min_race_bet_amount = settings.getInt("races.min_bet_amount");
        ascendant_realm_world = settings.getString("ascendance.realm_world");
        rage_quit_price = settings.getDouble("rage_quit.price");
        rage_quit_messages = settings.getStringList("rage_quit.messages");
        ascendance_hub_level = settings.getString("ascendance.hub_level");

        // load the respawn point for infinite pk if they enter from spawn
        String infinitePKRespawn = settings.getString("infinitepk.portal_respawn");
        // need to null check jic
        if (infinitePKRespawn != null) {
            String[] infinitePKSplit = infinitePKRespawn.split(":");

            infinitepk_portal_respawn = new Location(Bukkit.getWorld(infinitePKSplit[0]),
                    Double.parseDouble(infinitePKSplit[1]), Double.parseDouble(infinitePKSplit[2]), Double.parseDouble(infinitePKSplit[3]),
                    Float.parseFloat(infinitePKSplit[4]), Float.parseFloat(infinitePKSplit[5]));
        }
    }
}