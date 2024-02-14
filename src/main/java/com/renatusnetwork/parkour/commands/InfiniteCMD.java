package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.infinite.*;
import com.renatusnetwork.parkour.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteReward;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteRewards;
import com.renatusnetwork.parkour.data.infinite.rewards.InfiniteRewardsYAML;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class InfiniteCMD implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        InfiniteManager infiniteManager = Parkour.getInfiniteManager();

        if (a.length >= 2 && a[0].equalsIgnoreCase("score"))
        {
            InfiniteType type = InfiniteType.valueOf(a[1].toUpperCase());
            // other target
            if (a.length == 3)
            {
                Player target = Bukkit.getPlayer(a[2]);
                if (target != null)
                {
                    int score = Parkour.getStatsManager().get(target).getBestInfiniteScore(type);
                    sender.sendMessage(Utils.translate("&c" + a[2] + " &7has a &c" + StringUtils.capitalize(type.toString().toLowerCase()) + " &7score of &6" + Utils.formatNumber(score)));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not online"));
            // self
            }
            else if (a.length == 2)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                sender.sendMessage(Utils.translate("&7You have a &c" + StringUtils.capitalize(type.toString().toLowerCase()) + " &7score of &6" + Utils.formatNumber(playerStats.getBestInfiniteScore(type))));
            }
        // admin command for removing leaderboard position
        } else if (player.hasPermission("rn-parkour.admin") && (a.length == 4 && a[0].equalsIgnoreCase("setscore"))) {
            if (Utils.isInteger(a[3])) {
                InfiniteType type = InfiniteType.valueOf(a[2].toUpperCase());
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        // run db in async
                        if (StatsDB.isPlayerInDatabase(a[1]))
                        {
                            int score = Integer.parseInt(a[3]);

                            infiniteManager.updateScore(a[1], type, score);

                            player.sendMessage(Utils.translate("&7You have set &c" + a[1] + "&7's &c" + type + " &7score to &6" + score));
                        } else {
                            player.sendMessage(Utils.translate("&c" + a[1] + " &7has not joined the server yet"));
                        }
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            } else {
                player.sendMessage(Utils.translate("&c" + a[3] + " &7is not an integer"));
            }
        }
        else if (player.hasPermission("rn-parkour.admin") && (a.length == 1 && a[0].equalsIgnoreCase("loadrewards")))
        {
            Parkour.getConfigManager().load("rewards");
            infiniteManager.loadAllRewards();
            player.sendMessage(Utils.translate("&cYou have reloaded Infinite Parkour Rewards"));

        }
        else if (player.hasPermission("rn-parkour.admin") && (a.length == 3 && a[0].equalsIgnoreCase("mode")))
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
            if (!playerStats.isInInfinite())
            {
                if (!playerStats.isSpectating())
                {
                    if (!playerStats.isEventParticipant())
                    {
                        if (!playerStats.inRace())
                        {
                            if (!playerStats.inPracticeMode())
                            {
                                if (!playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))
                                {
                                    // if in elytra level, then toggle off
                                    if (playerStats.inLevel() && playerStats.getLevel().isElytra())
                                        Parkour.getStatsManager().toggleOffElytra(playerStats);

                                    infiniteManager.startPK(playerStats, playerStats.getInfiniteType(), false);
                                }
                                else
                                    player.sendMessage(Utils.translate("&cYou cannot start infinite from the plot world, do /spawn first"));
                            }
                            else
                                player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                        }
                        else
                            player.sendMessage(Utils.translate("&cYou cannot do this while you are in a race"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot do this while you are in an event"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot do this while you are spectating"));
            }
            else
                player.sendMessage(Utils.translate("&cYou are already in infinite parkour"));
        }
        else if (a.length == 2 && a[0].equalsIgnoreCase("rewards"))
        {
            String typeName = a[1];
            try
            {
                // get rewards
                InfiniteType type = InfiniteType.valueOf(typeName.toUpperCase());
                List<InfiniteReward> rewards = Parkour.getInfiniteManager().getRewards(type).getRewards();

                player.sendMessage(Utils.translate("&d&l" + StringUtils.capitalize(typeName.toLowerCase()) + " &5&lInfinite Rewards"));

                // if not empty continue
                if (!rewards.isEmpty())
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    if (playerStats != null)
                    {

                        for (InfiniteReward reward : rewards)
                        {
                            String msg = "&5" + Utils.formatNumber(reward.getScoreNeeded()) + " Score&7 » &d" + reward.getDisplay();

                            // send crossed out msg if their high score is more than the score needed
                            if (playerStats.getBestInfiniteScore(type) >= reward.getScoreNeeded())
                                msg = "&5&m" + Utils.formatNumber(reward.getScoreNeeded()) + " Score&7 » &d" + reward.getDisplay();

                            player.sendMessage(Utils.translate(msg));
                        }
                    }
                    else
                        player.sendMessage(Utils.translate("&cSomething went wrong... try again in a few seconds"));
                }
                else
                    player.sendMessage(Utils.translate("&dNo rewards available"));
            }
            catch (IllegalArgumentException exception)
            {
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
        player.sendMessage(Utils.translate("&5/infinite rewards <type> &7Tells you a list of the rewards for the type and if you have them (crossed out)"));

        if (player.hasPermission("rn-parkour.admin"))
        {
            player.sendMessage(Utils.translate("&5/infinite setscore <IGN> <type> <score>  &7Set the type's score of someone"));
            player.sendMessage(Utils.translate("&5/infinite loadrewards  &7Loads rewards from rewards.yml"));
            player.sendMessage(Utils.translate("&5/infinite mode <IGN> <type>  &7Set the mode of a player"));
        }

        player.sendMessage(Utils.translate("&5/infinite help  &7Shows you this display"));
    }
}
