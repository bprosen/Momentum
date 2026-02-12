package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.infinite.*;
import com.renatusnetwork.momentum.data.infinite.gamemode.Infinite;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.infinite.rewards.InfiniteReward;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InfiniteCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        InfiniteManager infiniteManager = Momentum.getInfiniteManager();

        if (player.hasPermission("momentum.admin") && a.length == 1 && a[0].equalsIgnoreCase("modes")) {
            sendTypes(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("quit")) {
            if (!Momentum.getStatsManager().get(player).isInInfinite()) {
                sender.sendMessage(Utils.translate("&cYou are not in infinite parkour!"));
                return false;
            }

            infiniteManager.endPK(player);
            sender.sendMessage(Utils.translate("&7You have ended your infinite run"));
        } else if (a.length >= 2 && a[0].equalsIgnoreCase("score")) {
            InfiniteType type = infiniteManager.getType(a[1].toUpperCase());
            if (type == null) {
                player.sendMessage(Utils.translate("&cInvalid infinite type &4" + a[1].toUpperCase() + "&c!"));
                sendTypes(player);
                return true;
            }

            // other target
            if (a.length == 3) {
                Player target = Bukkit.getPlayer(a[2]);
                if (target != null) {
                    int score = Momentum.getStatsManager().get(target).getBestInfiniteScore(type);
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7has a &c" + StringUtils.capitalize(type.toString().toLowerCase()) + " &7score of &6" + Utils.formatNumber(score)));
                } else {
                    // sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not online"));
                    int score = StatsDB.getPlayerInfiniteHighscore(a[2], type);
                    if (score != -1) {
                        sender.sendMessage(Utils.translate("&c" + a[2] + " &7has a &c" + StringUtils.capitalize(type.toString().toLowerCase()) + " &7score of &6" + Utils.formatNumber(score)));
                    }
                    else {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &ccould not be found"));
                    }
                }
                // self
            } else if (a.length == 2) {
                PlayerStats playerStats = Momentum.getStatsManager().get(player);
                sender.sendMessage(Utils.translate("&7You have a &c" + StringUtils.capitalize(type.toString().toLowerCase()) + " &7score of &6" + Utils.formatNumber(playerStats.getBestInfiniteScore(type))));
            }
            // admin command for editing score
        } else if (player.hasPermission("momentum.admin") && (a.length == 4 && a[0].equalsIgnoreCase("setscore"))) {
            if (Utils.isInteger(a[3])) {
                StatsManager statsManager = Momentum.getStatsManager();

                InfiniteType type = infiniteManager.getType(a[2].toUpperCase());
                if (type == null) {
                    player.sendMessage(Utils.translate("&cInvalid infinite type &4" + a[2].toUpperCase() + "&c!"));
                    sendTypes(player);
                    return true;
                }

                PlayerStats playerStats = statsManager.getByName(a[1]);
                int score = Integer.parseInt(a[3]);

                if (playerStats != null) {
                    statsManager.updateInfiniteScore(playerStats, type, score);
                    player.sendMessage(Utils.translate("&7You have set &c" + playerStats.getName() + "&7's &c" + type + " &7score to &6" + score));
                } else {
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                }
            } else {
                player.sendMessage(Utils.translate("&c" + a[3] + " &7is not an integer"));
            }
        } else if (player.hasPermission("momentum.admin") && (a.length == 1 && a[0].equalsIgnoreCase("loadrewards"))) {
            Momentum.getConfigManager().load("rewards");
            infiniteManager.loadAllRewards();
            player.sendMessage(Utils.translate("&cYou have reloaded Infinite Parkour Rewards"));

        } else if (player.hasPermission("momentum.admin") && (a.length == 3 && a[0].equalsIgnoreCase("mode"))) {
            String targetName = a[1];
            String mode = a[2];
            InfiniteType type = infiniteManager.getType(mode.toUpperCase());
            if (type == null) {
                player.sendMessage(Utils.translate("&cInvalid infinite type &4" + mode.toUpperCase() + "&c!"));
                sendTypes(player);
                return true;
            }

            Player target = Bukkit.getPlayer(targetName);

            if (target != null) {
                PlayerStats targetStats = Momentum.getStatsManager().get(target);
                infiniteManager.changeType(targetStats, type);
                player.sendMessage(Utils.translate("&7You changed &c" + targetName + "&7's infinite type to &4" + type));
            } else {
                player.sendMessage(Utils.translate("&4" + targetName + " &cis not online"));
            }
        } else if (a.length == 1 && a[0].equalsIgnoreCase("start")) {

            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            if (playerStats != null && playerStats.isLoaded()) {
                if (!playerStats.isInInfinite()) {
                    if (!playerStats.isSpectating()) {
                        if (!playerStats.isEventParticipant()) {
                            if (!playerStats.inRace()) {
                                if (!playerStats.inPracticeMode()) {
                                    if (!playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
                                        // if in elytra level, then toggle off
                                        if (playerStats.inLevel() && playerStats.getLevel().isElytra()) {
                                            Momentum.getStatsManager().toggleOffElytra(playerStats);
                                        }

                                        infiniteManager.startPK(playerStats, playerStats.getInfiniteType(), false);
                                    } else {
                                        player.sendMessage(Utils.translate("&cYou cannot start infinite from the plot world, do /spawn first"));
                                    }
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while you are in a race"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while you are in an event"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while you are spectating"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou are already in infinite parkour"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
            }
        } else if (a.length == 2 && a[0].equalsIgnoreCase("rewards")) {
            String typeName = a[1];
            try {
                // get rewards
                InfiniteType type = infiniteManager.getType(typeName.toUpperCase());
                if (type == null) {
                    player.sendMessage(Utils.translate("&cInvalid infinite type &4" + typeName.toUpperCase() + "&c!"));
                    sendTypes(player);
                    return true;
                }
                List<InfiniteReward> rewards = Momentum.getInfiniteManager().getRewards(type).getRewards();

                player.sendMessage(Utils.translate("&d&l" + StringUtils.capitalize(typeName.toLowerCase()) + " &5&lInfinite Rewards"));

                // if not empty continue
                if (!rewards.isEmpty()) {
                    PlayerStats playerStats = Momentum.getStatsManager().get(player);
                    if (playerStats != null) {

                        for (InfiniteReward reward : rewards) {
                            String msg = "&5" + Utils.formatNumber(reward.getScoreNeeded()) + " Score&7 » &d" + reward.getDisplay();

                            // send crossed out msg if their high score is more than the score needed
                            if (playerStats.getBestInfiniteScore(type) >= reward.getScoreNeeded()) {
                                msg = "&5&m" + Utils.formatNumber(reward.getScoreNeeded()) + " Score&7 » &d" + reward.getDisplay();
                            }

                            player.sendMessage(Utils.translate(msg));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cSomething went wrong... try again in a few seconds"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&dNo rewards available"));
                }
            } catch (IllegalArgumentException exception) {
                player.sendMessage(Utils.translate("&4" + typeName + " &cis not a infinite type"));
            }
        } else if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("help"))) {
            sendHelp(player);
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&5/infinite start  &7Starts Infinite Parkour"));
        player.sendMessage(Utils.translate("&5/infinite score <type> [IGN]  &7Tells you the score of yourself/someone else"));
        player.sendMessage(Utils.translate("&5/infinite rewards <type>  &7Tells you a list of the rewards for the type and if you have them (crossed out)"));
        player.sendMessage(Utils.translate("&5/infinite quit  &7Ends your current infinite run"));

        if (player.hasPermission("momentum.admin")) {
            player.sendMessage(Utils.translate("&5/infinite setscore <IGN> <type> <score>  &7Set the type's score of someone"));
            player.sendMessage(Utils.translate("&5/infinite loadrewards  &7Loads rewards from rewards.yml"));
            player.sendMessage(Utils.translate("&5/infinite mode <IGN> <type>  &7Set the mode of a player"));
            player.sendMessage(Utils.translate("&5/infinite modes  &7Lists the infinite modes"));
        }

        player.sendMessage(Utils.translate("&5/infinite help  &7Shows you this display"));
    }

    private void sendTypes(Player player) {
        player.sendMessage(Utils.translate("&5Infinite Parkour Types"));
        player.sendMessage(Utils.translate("&7CLASSIC"));
        player.sendMessage(Utils.translate("&7SPEEDRUN"));
        player.sendMessage(Utils.translate("&7TIMED"));
        player.sendMessage(Utils.translate("&7SPRINT"));
    }
}
