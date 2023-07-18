package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.*;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.storage.ConfigManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;

public class InfiniteCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        InfinitePKManager infinitePKManager = Parkour.getInfinitePKManager();

        if (a.length >= 1 && a[0].equalsIgnoreCase("score")) {
            // other target
            if (a.length == 2) {

                if (InfinitePKDB.hasScore(a[1])) {

                    int score = InfinitePKDB.getScoreFromName(a[1]);
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has a score of &6" + Utils.formatNumber(score)));
                } else {
                    sender.sendMessage(Utils.translate("&c" + a[1] + " &7has not played &6Infinite Parkour &7before (score of 0)"));
                }
            // self
            } else if (a.length == 1) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null && playerStats.getInfinitePKScore() > 0)
                    sender.sendMessage(Utils.translate("&7You have a score of &6" + Utils.formatNumber(playerStats.getInfinitePKScore())));
                else
                    sender.sendMessage(Utils.translate("&7You have yet to play &6Infinite Parkour &7(score of 0)"));
            // leaderboard
            } else if (a.length == 3 && a[1].equalsIgnoreCase("lb")) {

                if (Utils.isInteger(a[2])) {
                    int position = Integer.parseInt(a[2]);
                    // can cast with confidence
                    InfinitePKLBPosition lbPosition = (InfinitePKLBPosition) infinitePKManager.getLeaderboard().values().toArray()[position - 1];

                    if (lbPosition != null) {
                        player.sendMessage(Utils.translate(
                                "&c" + lbPosition.getName() + " &7is at &6spot " + position + " &7with a score of &6" + lbPosition.getScore())
                        );
                    } else {
                        player.sendMessage(Utils.translate("&7Something went wrong... invalid position &6" + position));
                    }
                } else {
                    player.sendMessage(Utils.translate("&c" + a[2] + " &7is not a position on the leaderboard! (1-10)"));
                }
            }
        // admin command for removing leaderboard position
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 3 && a[0].equalsIgnoreCase("setscore"))) {
            if (Utils.isInteger(a[2])) {
                if (InfinitePKDB.hasScore(a[1])) {
                    int score = Integer.parseInt(a[2]);

                    infinitePKManager.updateScore(a[1], score);
                    // can run in async
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            infinitePKManager.loadLeaderboard();
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin()); // load lb

                    player.sendMessage(Utils.translate("&7You have set &c" + a[1] + "&7's score to &6" + score));
                } else {
                    player.sendMessage(Utils.translate("&c" + a[1] + " &7has not joined the server yet"));
                }
            } else {
                player.sendMessage(Utils.translate("&c" + a[2] + " &7is not an integer"));
            }
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 1 && a[0].equalsIgnoreCase("setportalrespawn"))) {

            Location loc = player.getLocation();
            String locString = player.getWorld().getName() + ":" +
                    loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" +
                    loc.getYaw() + ":" + loc.getPitch();

            // set and save
            ConfigManager configManager = Parkour.getConfigManager();
            configManager.get("settings").set("infinitepk.portal_respawn", locString);
            configManager.save("settings");
            Parkour.getSettingsManager().load(configManager.get("settings"));
            player.sendMessage(Utils.translate("&7You set the portal respawn to your location"));
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 1 && a[0].equalsIgnoreCase("loadrewards"))) {

            Parkour.getInfinitePKManager().clearRewards();
            InfiniteRewardsYAML.loadRewards();
            player.sendMessage(Utils.translate("&cYou have reloaded Infinite Parkour Rewards"));

        } else if (a.length == 1 && a[0].equalsIgnoreCase("start")) {

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            if (!playerStats.isInInfinitePK()) {
                if (!playerStats.isSpectating()) {
                    if (!playerStats.isEventParticipant()) {
                        if (!playerStats.inRace()) {
                            if (!playerStats.inPracticeMode()) {
                                // if in elytra level, then toggle off
                                if (playerStats.inLevel() && playerStats.getLevel().isElytraLevel())
                                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                                infinitePKManager.startPK(playerStats, false);
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
        } else if (a.length == 1 && a[0].equalsIgnoreCase("rewards")) {

            LinkedHashMap<Integer, InfinitePKReward> rewards = Parkour.getInfinitePKManager().getRewards();
            player.sendMessage(Utils.translate("&5&lInfinite Parkour Rewards"));

            // if not empty continue
            if (!rewards.isEmpty()) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                int position = 1;

                for (InfinitePKReward reward : rewards.values()) {

                    String msg = "&7" + position + " &5" + reward.getScoreNeeded() + " Score &7- &d" + reward.getName();

                    // send crossed out msg if their high score is more than the score needed
                    if (playerStats.getInfinitePKScore() >= reward.getScoreNeeded())
                        msg = "&7" + position + " &5&m" + reward.getScoreNeeded() + " Score&7 - &d" + reward.getName();

                    player.sendMessage(Utils.translate(msg));
                    position++;
                }
            } else {
                player.sendMessage(Utils.translate("&dNo rewards available"));
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
        player.sendMessage(Utils.translate("&5/infinite score [IGN]  &7Tells you the score of yourself/someone else"));
        player.sendMessage(Utils.translate("&5/infinite score lb <position>  &7Tells you the score of someone in <position> on the leaderboard"));
        player.sendMessage(Utils.translate("&5/infinite rewards  &7Tells you a list of the rewards and if you have them (crossed out)"));

        if (player.hasPermission("rn-parkour.admin")) {
            player.sendMessage(Utils.translate("&5/infinite setscore <IGN> <score>  &7Set the score of someone"));
            player.sendMessage(Utils.translate("&5/infinite setportalrespawn  &7Sets the portal respawn to your location"));
            player.sendMessage(Utils.translate("&5/infinite loadrewards  &7Loads rewards from rewards.yml"));
        }

        player.sendMessage(Utils.translate("&5/infinite help  &7Shows you this display"));
    }
}
