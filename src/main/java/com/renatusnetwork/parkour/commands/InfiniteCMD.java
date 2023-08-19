package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.*;
import com.renatusnetwork.parkour.data.infinite.types.InfiniteType;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
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
        InfiniteManager infiniteManager = Parkour.getInfiniteManager();

        if (a.length >= 2 && a[0].equalsIgnoreCase("score"))
        {
            InfiniteType type = InfiniteType.valueOf(a[1].toUpperCase());
            // other target
            if (a.length == 3)
            {
                if (InfiniteDB.hasScore(type, a[2]))
                {
                    int score = InfiniteDB.getScoreFromName(type, a[2]);
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7has a score of &6" + Utils.formatNumber(score)));
                }
                else
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7has not played &6Infinite Parkour &7before (score of 0)"));
            // self
            }
            else if (a.length == 2)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null && playerStats.getBestInfiniteScore() > 0)
                    sender.sendMessage(Utils.translate("&7You have a score of &6" + Utils.formatNumber(playerStats.getBestInfiniteScore())));
                else
                    sender.sendMessage(Utils.translate("&7You have yet to play &6Infinite Parkour &7(score of 0)"));
            }
        // admin command for removing leaderboard position
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 4 && a[0].equalsIgnoreCase("setscore"))) {
            if (Utils.isInteger(a[3])) {
                InfiniteType type = InfiniteType.valueOf(a[2].toUpperCase());
                if (InfiniteDB.hasScore(type, a[1])) {
                    int score = Integer.parseInt(a[3]);

                    infiniteManager.updateScore(a[1], type, score);
                    infiniteManager.loadLeaderboard(type);

                    player.sendMessage(Utils.translate("&7You have set &c" + a[1] + "&7's &c" + type + " &7score to &6" + score));
                } else {
                    player.sendMessage(Utils.translate("&c" + a[1] + " &7has not joined the server yet"));
                }
            } else {
                player.sendMessage(Utils.translate("&c" + a[3] + " &7is not an integer"));
            }
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 1 && a[0].equalsIgnoreCase("loadrewards"))) {

            Parkour.getInfiniteManager().clearRewards();
            InfiniteRewardsYAML.loadRewards();
            player.sendMessage(Utils.translate("&cYou have reloaded Infinite Parkour Rewards"));

        }
        else if (player.hasPermission("rn-parkour.admin") && (a.length == 3 && a[0].equalsIgnoreCase("setmode")))
        {
            String targetName = a[1];
            String mode = a[2];
            InfiniteType infiniteType = InfiniteType.valueOf(mode.toUpperCase());

            Player target = Bukkit.getPlayer(targetName);

            if (target != null)
            {
                PlayerStats targetStats = Parkour.getStatsManager().get(target);
                infiniteManager.changeType(targetStats, infiniteType);
                player.sendMessage(Utils.translate("&7You changed &c" + targetName + "&7's infinite type to &4" + infiniteType));
            }
            else
            {
                player.sendMessage(Utils.translate("&4" + targetName + " &cis not online"));
            }
        } else if (a.length == 1 && a[0].equalsIgnoreCase("start")) {

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            if (!playerStats.isInInfinite()) {
                if (!playerStats.isSpectating()) {
                    if (!playerStats.isEventParticipant()) {
                        if (!playerStats.inRace()) {
                            if (!playerStats.inPracticeMode()) {
                                // if in elytra level, then toggle off
                                if (playerStats.inLevel() && playerStats.getLevel().isElytraLevel())
                                    Parkour.getStatsManager().toggleOffElytra(playerStats);

                                infiniteManager.startPK(playerStats, playerStats.getInfiniteType(), false);
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

            LinkedHashMap<Integer, InfiniteReward> rewards = Parkour.getInfiniteManager().getRewards();
            player.sendMessage(Utils.translate("&5&lInfinite Parkour Rewards"));

            // if not empty continue
            if (!rewards.isEmpty()) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                int position = 1;

                for (InfiniteReward reward : rewards.values()) {

                    String msg = "&7" + position + " &5" + reward.getScoreNeeded() + " Score &7- &d" + reward.getName();

                    // send crossed out msg if their high score is more than the score needed
                    if (playerStats.getBestInfiniteScore() >= reward.getScoreNeeded())
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
        player.sendMessage(Utils.translate("&5/infinite score <type> [IGN]  &7Tells you the score of yourself/someone else"));
        player.sendMessage(Utils.translate("&5/infinite rewards  &7Tells you a list of the rewards and if you have them (crossed out)"));

        if (player.hasPermission("rn-parkour.admin"))
        {
            player.sendMessage(Utils.translate("&5/infinite setscore <IGN> <type> <score>  &7Set the type's score of someone"));
            player.sendMessage(Utils.translate("&5/infinite loadrewards  &7Loads rewards from rewards.yml"));
            player.sendMessage(Utils.translate("&5/infinite setmode <IGN> <type>  &7Set the mode of a player"));
        }

        player.sendMessage(Utils.translate("&5/infinite help  &7Shows you this display"));
    }
}
