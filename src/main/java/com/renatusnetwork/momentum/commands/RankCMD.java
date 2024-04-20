package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.ranks.RanksManager;
import com.renatusnetwork.momentum.data.ranks.RanksDB;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class RankCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        RanksManager ranksManager = Momentum.getRanksManager();

        if (player.hasPermission("momentum.admin"))
        {
            if (a.length == 0) {
                if (label.equalsIgnoreCase("ranks"))
                    sendRanks(player);
                else
                    sendRank(player);
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("set"))
            {
                PlayerStats victim = Momentum.getStatsManager().getByName(a[1]);
                String rankName = a[2];

                if (victim != null)
                {
                    if (ranksManager.exists(rankName))
                    {
                        Rank rank = ranksManager.get(rankName);
                        victim.setRank(rank);
                        StatsDB.updateRank(victim.getUUID(), rank.getName());
                        player.sendMessage(Utils.translate("&7You set &c" + victim.getName() + "&7's rank to &c" + rank.getTitle()));
                    }
                    else
                        player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("list"))
            {

                player.sendMessage(Utils.translate("&7Ranks Loaded: &c" + String.join("&7, &c",
                        Momentum.getRanksManager().getNames())));

            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("create"))
            {
                String rankName = a[1].toLowerCase();

                if (!ranksManager.exists(rankName))
                {
                    // create in db
                    RanksDB.addRank(rankName);
                    // create object
                    ranksManager.add(rankName);
                    player.sendMessage(Utils.translate("&7Created rank &c" + rankName));
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + rankName + " &calready exists"));
                }
            }
            else if (a.length >= 2 && a[0].equalsIgnoreCase("title"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String title = String.join(" ", split);

                    // update in db
                    RanksDB.updateTitle(rankName, title);
                    // update object
                    rank.setTitle(title);
                    player.sendMessage(Utils.translate("&7Set &c" + rankName + "&7's title to &c" + title));
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("rankuplevel"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    String levelName = a[2].toLowerCase();
                    Level level = Momentum.getLevelManager().get(levelName);

                    if (level != null)
                    {
                        // update in db
                        RanksDB.updateRankupLevel(rankName, levelName);
                        // update object
                        rank.setRankupLevel(level);
                        player.sendMessage(Utils.translate("&7Set &c" + rankName + "&7's rank up level to &c" + level.getTitle()));
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + levelName + " &cdoes not exist"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("nextrank"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    String nextRankName = a[2].toLowerCase();
                    Rank nextRank = ranksManager.get(nextRankName);

                    if (nextRank != null)
                    {
                        // update in db
                        RanksDB.updateNextRank(rankName, nextRankName);
                        // update object
                        rank.setNextRank(nextRankName);
                        player.sendMessage(Utils.translate("&7Set &c" + rankName + "&7's next rank to &c" + nextRank.getName()));
                    }
                    else
                        player.sendMessage(Utils.translate("&4" + nextRankName + " &cdoes not exist"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("remove"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    if (!rankName.equalsIgnoreCase(Momentum.getSettingsManager().default_rank))
                    {
                        // reset people in the rank
                        ranksManager.resetPlayersInRank(rank);
                        // remove object
                        ranksManager.remove(rankName);
                        RanksDB.removeRank(rankName);
                        player.sendMessage(Utils.translate("&7Removed rank &c" + rankName));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot delete the default rank"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + rankName + " &cis not a rank"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("load"))
            {
                new BukkitRunnable() {
                    @Override
                    public void run()
                    {
                        Momentum.getRanksManager().load();
                        sender.sendMessage(Utils.translate("&7Loaded &c" + Momentum.getRanksManager().getNames().size() + " ranks"));
                    }
                }.runTaskAsynchronously(Momentum.getPlugin());
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("prestiges"))
            {
                PlayerStats victim = Momentum.getStatsManager().getByName(a[1]);

                if (victim != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int newPrestige = Integer.parseInt(a[2]);

                        if (newPrestige >= 0)
                        {
                            ranksManager.updatePrestiges(victim, newPrestige);

                            if (victim.hasPrestiges())
                            {
                                float prestigeMultiplier = Momentum.getSettingsManager().prestige_multiplier_per_prestige * victim.getPrestiges();

                                if (prestigeMultiplier >= Momentum.getSettingsManager().max_prestige_multiplier)
                                    prestigeMultiplier = Momentum.getSettingsManager().max_prestige_multiplier;

                                prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                                victim.setPrestigeMultiplier(prestigeMultiplier);
                            }
                            player.sendMessage(Utils.translate("&cYou have changed &4" + a[1] + "&c's prestiges to &6" + newPrestige));

                        }
                        else
                            player.sendMessage(Utils.translate("&cDon't try to set someone into the negatives please :)"));
                    }
                    else
                        player.sendMessage(Utils.translate("&4" + a[2] + " &cis not a valid integer"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("help"))
                sendAdminHelp(player);
            else
                sendAdminHelp(player);
        }
        else if (a.length == 0) {
            if (label.equalsIgnoreCase("ranks"))
                sendRanks(player);
            else
                sendRank(player);
        }
        else
            sendPlayerHelp(player);

        return false;
    }

    private void sendRank(Player player)
    {
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        if (playerStats.hasRank())
        {
            player.sendMessage(Utils.translate("&cYou are &6" + playerStats.getRank().getTitle()));

            if (playerStats.hasPrestiges())
            {

                // add an s if its not one because im OCD with this
                String endingString = "prestige";
                if (playerStats.getPrestiges() > 1)
                    endingString += "s";

                player.sendMessage(Utils.translate("&cYou have &6" + playerStats.getPrestiges() + " " + endingString));
            }
        }
        else if (player.hasPermission("momentum.admin"))
            sendAdminHelp(player);
        else
            sendPlayerHelp(player);
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Admin Help"));
        player.sendMessage(Utils.translate("&c/rank  &7Tells you your rank"));
        player.sendMessage(Utils.translate("&c/ranks list  &7List all ranks"));
        player.sendMessage(Utils.translate("&c/ranks load  &7Reloads ranks"));
        player.sendMessage(Utils.translate("&c/ranks create <rankName>  &7Create a rank"));
        player.sendMessage(Utils.translate("&c/ranks title <rankName> <title>  &7Set's a ranks title (can have spaces)"));
        player.sendMessage(Utils.translate("&c/ranks rankuplevel <rankName> <rankupLevel>  &7Set a rank's rankup level"));
        player.sendMessage(Utils.translate("&c/ranks nextrank <rankName> <nextRank>  &7Sets a rank's next rank"));
        player.sendMessage(Utils.translate("&c/ranks remove <rankName>  &7Removes a rank and sets players in that rank to default"));
        player.sendMessage(Utils.translate("&c/ranks set <player> <rankName>  &7Sets players rank"));
        player.sendMessage(Utils.translate("&c/ranks prestiges <player> <amount>  &7Sets players prestiges from database and cache"));
        player.sendMessage(Utils.translate("&c/ranks help  &7Displays this page"));
    }

    private void sendPlayerHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Help"));
        player.sendMessage(Utils.translate("&c/rank  &7Tells you your rank"));
        player.sendMessage(Utils.translate("&c/ranks help  &7Displays this page"));
    }

    private void sendRanks(Player player) {
        Rank rank = Momentum.getRanksManager().get(Momentum.getSettingsManager().default_rank);
        String msg = "&7" + rank.getTitle();
        rank = Momentum.getRanksManager().get(rank.getNextRank());
        while (rank != null) {
            msg += " &8-> &7" + rank.getTitle();
            rank = Momentum.getRanksManager().get(rank.getNextRank());
        }
        player.sendMessage(Utils.translate(msg));
    }
}
