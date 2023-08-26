package com.renatusnetwork.parkour.data;

import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SettingsManager {

    // constants
    public static final String INFINITE_PORTAL_NAME = "infinite-portal";
    public static final String ASCENDANCE_PORTAL_NAME = "ascendance-portal";
    public static final String BLACK_MARKET_PORTAL_NAME = "black_market-portal";

    public World main_world;
    public String levels_message_completion;
    public String levels_message_broadcast;
    public double featured_level_reward_multiplier;
    public int max_rated_levels_leaderboard_size;

    public String signs_first_line;
    public String signs_second_line_completion;
    public String signs_second_line_spawn;

    public int max_event_leaderboard_size;
    public int clans_max_members;
    public int clans_max_level;
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
    public int falling_anvil_event_task_delay;
    public int rising_water_event_task_delay;

    public LinkedHashMap<Integer, ItemStack> setup_swords;
    public int sword_hotbar_slot;
    public String sword_title;

    public int max_infinite_leaderboard_size;
    public int max_infinite_y;
    public int infinite_starting_y;
    public int min_infinite_y;
    public int infinite_soft_border_radius_x;
    public int infinite_soft_border_radius_z;
    public String infinite_respawn_loc;
    public String infinite_middle_loc;

    public int max_global_level_completions_leaderboard_size;
    public int max_global_personal_completions_leaderboard_size;
    public int max_clans_leaderboard_size;
    public int max_race_leaderboard_size;
    public int max_coins_leaderboard_size;
    public int max_records_leaderboard_size;

    public int max_prestige_multiplier;
    public int prestige_multiplier_per_prestige;
    public double base_prestige_cost;
    public double additional_cost_per_prestige;

    public double min_race_bet_amount;

    public String ascendant_realm_world;
    public String ascendance_hub_level;

    public double rage_quit_price;
    public List<String> rage_quit_messages;

    public int default_gg_timer;
    public int default_gg_coin_reward;

    public String tutorial_level_name;

    public int prac_hotbar_slot;
    public String prac_title;
    public Material prac_type;
    public ItemStack prac_item;

    public int leave_hotbar_slot;
    public String leave_title;
    public Material leave_type;
    public ItemStack leave_item;

    public double minimum_pay_amount;

    public HashMap<Integer, ItemStack> custom_join_inventory;

    public LinkedHashMap<Integer, Float> cooldown_modifiers;
    public Calendar cooldown_calendar;

    public int radiant_minimum_bid;
    public int brilliant_minimum_bid;
    public int legendary_minimum_bid;

    public int radiant_lock_minimum;
    public int brilliant_lock_minimum;
    public int legendary_lock_minimum;

    public float lock_chance;
    public int lock_minutes;

    public int jackpot_length;

    public int blackmarket_min_player_count;

    public Calendar black_market_reset_calendar;
    public int seconds_before_ending_from_no_bids;

    public String blackmarket_item_spawn_loc;
    public String blackmarket_tp_loc;
    public String blackmarket_message_prefix;
    public String jackpot_force_remove_permission_cmd;

    public int sprint_starting_timer;
    public int sprint_max_timer;
    public float sprint_time_gain;
    public LinkedHashMap<Integer, Float> reduction_factors;

    public int timed_timer;
    public float infinite_angle_bound;
    public float infinite_distance_min;
    public float infinite_distance_bound;
    public int infinite_generation_y_min;
    public int infinite_generation_y_max;

    public float infinite_generation_positive_y_min;
    public float infinite_generation_positive_y_max;
    public float infinite_generation_positive_y_diff;
    public float infinite_generation_negative_y_min;
    public float infinite_generation_negative_y_max;
    public float infinite_generation_negative_y_diff;

    public int infinite_angle_outside_border_min;
    public int infinite_angle_outside_border_max;

    public SettingsManager(FileConfiguration settings) {
        cooldown_calendar = Calendar.getInstance();
        cooldown_calendar.setTime(new Date());
        load(settings);
    }

    public void load(FileConfiguration settings) {
        main_world = Bukkit.getWorld(settings.getString("main_world"));
        levels_message_completion = settings.getString("levels.message.completion");
        levels_message_broadcast = settings.getString("levels.message.broadcast");
        signs_first_line = settings.getString("signs.first_line");
        signs_second_line_completion = settings.getString("signs.second_line.completion");
        signs_second_line_spawn = settings.getString("signs.second_line.spawn");
        clans_max_members = settings.getInt("clans.max_members");
        clans_max_level = settings.getInt("clans.max_level");
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
        falling_anvil_event_task_delay = settings.getInt("event.task_delay.falling_anvil");
        rising_water_event_task_delay = settings.getInt("event.task_delay.rising_water");
        minimum_rank_for_plot_creation = settings.getString("player_submitted.minimum_rank_for_plot_creation");
        featured_level_reward_multiplier = settings.getDouble("levels.featured_level_reward_multiplier");
        sword_hotbar_slot = settings.getInt("setup-sword.hotbar_slot");
        sword_title = Utils.translate(settings.getString("setup-sword.title"));

        setup_swords = new LinkedHashMap<>(); // we want it in order!

        ConfigurationSection section = settings.getConfigurationSection("setup-sword.prestiges");
        for (String key : section.getKeys(false))
        {
            int keyInt = Integer.parseInt(key);

            ItemStack sword = new ItemStack(Material.matchMaterial(section.getString(keyInt + ".type")));
            ItemMeta meta = sword.getItemMeta();
            meta.setDisplayName(sword_title);

            // set glow
            if (section.getBoolean(keyInt + ".glow"))
            {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            sword.setItemMeta(meta);

            setup_swords.put(keyInt, sword); // put in
        }

        max_infinite_leaderboard_size = settings.getInt("infinite.max_leaderboard_size");
        max_infinite_y = settings.getInt("infinite.max_y");
        min_infinite_y = settings.getInt("infinite.min_y");
        infinite_starting_y = settings.getInt("infinite.starting_y");
        max_global_level_completions_leaderboard_size = settings.getInt("completions.global_level_completions_leaderboard.max_size");
        max_global_personal_completions_leaderboard_size = settings.getInt("completions.global_personal_completions_leaderboard.max_size");
        max_clans_leaderboard_size = settings.getInt("clans.max_leaderboard_size");
        max_coins_leaderboard_size = settings.getInt("stats.max_coins_leaderboard_size");
        max_prestige_multiplier = settings.getInt("prestiges.max_multiplier");
        prestige_multiplier_per_prestige = settings.getInt("prestiges.multiplier_per_prestige");
        base_prestige_cost = settings.getDouble("prestiges.base_prestige_cost");
        additional_cost_per_prestige = settings.getDouble("prestiges.additional_cost_per_prestige");
        max_rated_levels_leaderboard_size = settings.getInt("levels.max_rated_levels_leaderboard_size");
        max_race_leaderboard_size = settings.getInt("races.max_leaderboard_size");
        max_records_leaderboard_size = settings.getInt("records.max_leaderboard_size");
        min_race_bet_amount = settings.getInt("races.min_bet_amount");
        ascendant_realm_world = settings.getString("ascendance.realm_world");
        rage_quit_price = settings.getDouble("rage_quit.price");
        rage_quit_messages = settings.getStringList("rage_quit.messages");
        ascendance_hub_level = settings.getString("ascendance.hub_level");
        default_gg_timer = settings.getInt("gg.default_timer_in_seconds");
        default_gg_coin_reward = settings.getInt("gg.default_coin_reward");
        max_event_leaderboard_size = settings.getInt("event.max_leaderboard_size");
        tutorial_level_name = settings.getString("levels.tutorial_level");
        prac_title = Utils.translate(settings.getString("practice-plate.title"));
        prac_type = Material.matchMaterial(settings.getString("practice-plate.type"));
        prac_hotbar_slot = settings.getInt("practice-plate.hotbar_slot");

        prac_item = new ItemStack(prac_type);
        ItemMeta meta = prac_item.getItemMeta();
        meta.setDisplayName(prac_title);
        prac_item.setItemMeta(meta);

        leave_title = Utils.translate(settings.getString("leave-item.title"));
        leave_type = Material.matchMaterial(settings.getString("leave-item.type"));
        leave_hotbar_slot = settings.getInt("leave-item.hotbar_slot");

        leave_item = new ItemStack(leave_type);
        meta = leave_item.getItemMeta();
        meta.setDisplayName(Utils.translate(leave_title));
        leave_item.setItemMeta(meta);

        radiant_minimum_bid = settings.getInt("bank.radiant.min_starting_bid");
        brilliant_minimum_bid = settings.getInt("bank.brilliant.min_starting_bid");
        legendary_minimum_bid = settings.getInt("bank.legendary.min_starting_bid");
        jackpot_length = settings.getInt("bank.jackpot.length");
        radiant_lock_minimum = settings.getInt("bank.radiant.min_lock");
        brilliant_lock_minimum = settings.getInt("bank.brilliant.min_lock");
        legendary_lock_minimum = settings.getInt("bank.legendary.min_lock");
        minimum_pay_amount = settings.getDouble("minimum_pay_amount");
        lock_chance = (float) settings.getDouble("bank.lock_chance");
        lock_minutes = settings.getInt("bank.lock_minutes");
        black_market_reset_calendar = Calendar.getInstance();
        String day = settings.getString("blackmarket.start_time.day");
        switch (day)
        {
            case "sunday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
            case "monday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "tuesday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                break;
            case "wednesday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                break;
            case "thursday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                break;
            case "friday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                break;
            case "saturday":
                black_market_reset_calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                break;
        }
        black_market_reset_calendar.set(Calendar.HOUR_OF_DAY, settings.getInt("bank.reset_time.hour"));

        custom_join_inventory = new HashMap<>();

        // hotbar length!
        for (int i = 0; i < 9; i++)
        {
            if (settings.isConfigurationSection("join_inventory." + i))
            {
                // set if type exists
                ItemStack itemStack;
                if (settings.isSet("join_inventory." + i + ".item.type"))
                    itemStack = new ItemStack(Material.matchMaterial(settings.getString("join_inventory." + i + ".item.material")), 1, (short) 3);
                else
                    itemStack = new ItemStack(Material.matchMaterial(settings.getString("join_inventory." + i + ".item.material")));

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Utils.translate(settings.getString("join_inventory." + i + ".item.title")));

                // lore
                List<String> lore = settings.getStringList("join_inventory." + i + ".item.lore");
                List<String> tempLore = new ArrayList<>();

                for (String string : lore)
                    tempLore.add(Utils.translate(string));

                itemMeta.setLore(tempLore);

                itemStack.setItemMeta(itemMeta);

                custom_join_inventory.put(i, itemStack);
            }
        }
        seconds_before_ending_from_no_bids = settings.getInt("blackmarket.seconds_before_ending_from_no_bids");
        blackmarket_min_player_count = settings.getInt("blackmarket.min_player_count");
        blackmarket_item_spawn_loc = settings.getString("blackmarket.item_spawn_location");
        blackmarket_tp_loc = settings.getString("blackmarket.tp_location");
        infinite_middle_loc = settings.getString("infinite.infinite_middle_loc");
        infinite_respawn_loc = settings.getString("infinite.infinite_respawn_loc");
        blackmarket_message_prefix = settings.getString("blackmarket.message_prefix");
        jackpot_force_remove_permission_cmd = settings.getString("bank.jackpot_force_remove_permission_cmd");
        sprint_starting_timer = settings.getInt("infinite.sprint.starting_timer");
        sprint_max_timer = settings.getInt("infinite.sprint.max_timer");
        sprint_time_gain = settings.getInt("infinite.sprint.default_time_gain");
        Set<String> keys = settings.getConfigurationSection("infinite.sprint.time_reduction_factors").getKeys(false);

        reduction_factors = new LinkedHashMap<>();
        for (String key : keys)
            reduction_factors.put(Integer.parseInt(key), (float) settings.getDouble("infinite.time_reduction_factors." + key + ".reduction"));

        timed_timer = settings.getInt("infinite.timed.timer");
        infinite_angle_bound = (float) settings.getDouble("infinite.generation.angle_bound");
        infinite_distance_min = (float) settings.getDouble("infinite.generation.distance_min");
        infinite_distance_bound = (float) settings.getDouble("infinite.generation.distance_bound");
        infinite_generation_y_min = settings.getInt("infinite.generation.y_min");
        infinite_generation_y_max = settings.getInt("infinite.generation.y_max");
        infinite_generation_positive_y_min = (float) settings.getDouble("infinite.generation.positive_y.min_modifier");
        infinite_generation_positive_y_max = (float) settings.getDouble("infinite.generation.positive_y.max_modifier");
        infinite_generation_positive_y_diff = (float) settings.getDouble("infinite.generation.positive_y.minimum_difference");
        infinite_generation_negative_y_min = (float) settings.getDouble("infinite.generation.negative_y.min_modifier");
        infinite_generation_negative_y_max = (float) settings.getDouble("infinite.generation.negative_y.max_modifier");
        infinite_generation_negative_y_diff = (float) settings.getDouble("infinite.generation.negative_y.minimum_difference");
        infinite_soft_border_radius_x = settings.getInt("infinite.soft_border_radius_x");
        infinite_soft_border_radius_z = settings.getInt("infinite.soft_border_radius_z");
        infinite_angle_outside_border_min = settings.getInt("infinite.generation.angle_outside_border_min");
        infinite_angle_outside_border_max = settings.getInt("infinite.generation.angle_outside_border_max");
    }
}