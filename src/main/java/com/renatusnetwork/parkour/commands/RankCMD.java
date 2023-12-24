package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.ranks.RanksManager;
import com.renatusnetwork.parkour.data.ranks.RanksDB;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class RankCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        RanksManager ranksManager = Parkour.getRanksManager();

        if (player.hasPermission("rn-parkour.admin")) {
            if (a.length == 0) {
                sendRank(player);
            } else if (a.length == 3 && a[0].equalsIgnoreCase("setrank")) {

                Player victim = Bukkit.getPlayer(a[1]);
                String rankName = a[2];

                if (victim == null) {
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    return true;
                }

                if (ranksManager.exists(rankName))
                {
                    Rank rank = ranksManager.get(rankName);
                    Parkour.getStatsManager().get(victim).setRank(rank);
                    StatsDB.updateRank(victim.getUniqueId().toString(), rank.getName());
                    player.sendMessage(Utils.translate("&7You set &c" + victim.getName() + "&7's rank to &c" + rank.getTitle()));
                } else {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {

                player.sendMessage(Utils.translate("&7Ranks Loaded: &c" + String.join("&7, &c",
                        Parkour.getRanksManager().getNames())));

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
            else if (a.length >= 2 && a[0].equalsIgnoreCase("settitle"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    String[] split = Arrays.copyOfRange(a, 1, a.length);
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
            else if (a.length == 3 && a[0].equalsIgnoreCase("setrankuplevel"))
            {
                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    String levelName = a[2].toLowerCase();
                    Level level = Parkour.getLevelManager().get(levelName);

                    if (level != null)
                    {
                        // update in db
                        RanksDB.updateRankupLevel(rankName, levelName);
                        // update object
                        rank.setRankupLevel(level);
                        player.sendMessage(Utils.translate("&7Set &c" + rankName + "&7's rank up level to &c" + level.getFormattedTitle()));
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
            else if (a.length == 3 && a[0].equalsIgnoreCase("setnextrank"))
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
                        player.sendMessage(Utils.translate("&7Set &c" + rankName + "&7's next rank to &c" + nextRank.getTitle()));
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + nextRankName + " &cdoes not exist"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("remove")) {

                String rankName = a[1].toLowerCase();
                Rank rank = ranksManager.get(rankName);

                if (rank != null)
                {
                    if (!rankName.equalsIgnoreCase(Parkour.getSettingsManager().default_rank))
                    {
                        // reset people in the rank
                        ranksManager.resetPlayersInRank(rank);
                        // remove object
                        ranksManager.remove(rankName);
                        RanksDB.removeRank(rankName);
                        player.sendMessage(Utils.translate("&7Removed rank &c" + rankName));
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&cYou cannot delete the default rank"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cis not a rank"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                new BukkitRunnable() {
                    @Override
                    public void run()
                    {
                        Parkour.getConfigManager().load("ranks");
                        sender.sendMessage(Utils.translate("&7Loaded &cranks.yml &7from disk"));
                        Parkour.getRanksManager().load();
                        sender.sendMessage(Utils.translate("&7Loaded ranks from &cranks.yml&7, &c" +
                                Parkour.getRanksManager().getNames().size() + " &7total"));
                    }
                }.runTaskAsynchronously(Parkour.getPlugin());
            } else if (a.length == 3 && a[0].equalsIgnoreCase("setprestiges")) {
                // check if they have joined parkour
                if (StatsDB.isPlayerInDatabase(a[1])) {

                    Player victim = Bukkit.getPlayer(a[1]);
                    if (Utils.isInteger(a[2])) {

                        int newPrestige = Integer.parseInt(a[2]);

                        if (newPrestige >= 0) {
                            // update ingame cache if theyre online
                            if (victim != null) {
                                PlayerStats victimStats = Parkour.getStatsManager().get(victim.getUniqueId().toString());
                                victimStats.setPrestiges(newPrestige);

                                if (victimStats.getPrestiges() > 0) {
                                    float prestigeMultiplier = Parkour.getSettingsManager().prestige_multiplier_per_prestige * victimStats.getPrestiges();

                                    if (prestigeMultiplier >= Parkour.getSettingsManager().max_prestige_multiplier)
                                        prestigeMultiplier = Parkour.getSettingsManager().max_prestige_multiplier;

                                    prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                                    victimStats.setPrestigeMultiplier(prestigeMultiplier);
                                }
                            }
                            // update in db
                            RanksDB.updatePrestigesFromName(a[1], newPrestige);
                            player.sendMessage(Utils.translate("&cYou have changed &4" + a[1] + "&c's Prestiges to &6" + newPrestige));

                        } else {
                            player.sendMessage(Utils.translate("&cDon't try to set someone into the negatives please :)"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&4" + a[2] + " &cis not a valid integer"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cdoes not exist in database!"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
                sendAdminHelp(player);
            } else {
                sendAdminHelp(player);
            }
        } else if (a.length == 0) {
            sendRank(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendPlayerHelp(player);
        } else {
            sendPlayerHelp(player);
        }

        return false;
    }

    private void sendRank(Player player)
    {
        player.sendMessage(Utils.translate("&cYou are &6" + Parkour.getStatsManager().get(player).getRank().getTitle()));

        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());

        if (playerStats.getPrestiges() > 0) {

            // add an s if its not one because im OCD with this
            String endingString = "time";
            if (playerStats.getPrestiges() > 1)
                endingString += "s";

            player.sendMessage(Utils.translate("&cYou have prestiged &6" + playerStats.getPrestiges() + " " + endingString));
        }
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Admin Help"));
        player.sendMessage(Utils.translate("&c/rank  &7Tells you your rank"));
        player.sendMessage(Utils.translate("&c/ranks list  &7List all ranks"));
        player.sendMessage(Utils.translate("&c/ranks load  &7Reloads ranks"));
        player.sendMessage(Utils.translate("&c/ranks create <rankName>  &7Create a rank"));
        player.sendMessage(Utils.translate("&c/ranks settitle <rankName> <title>  &7Set's a ranks title (can have spaces)"));
        player.sendMessage(Utils.translate("&c/ranks setrankuplevel <rankName> <rankupLevel>  &7Set a rank's rankup level"));
        player.sendMessage(Utils.translate("&c/ranks setnextrank <rankName> <nextRank>  &7Sets a rank's next rank"));
        player.sendMessage(Utils.translate("&c/ranks remove <rankName>  &7Removes a rank and sets players in that rank to default"));
        player.sendMessage(Utils.translate("&c/ranks setrank <player> <rankName>  &7Sets players rank"));
        player.sendMessage(Utils.translate("&c/ranks setprestiges <player> <amount>  &7Sets players prestiges from database and cache"));
        player.sendMessage(Utils.translate("&c/ranks help  &7Displays this page"));
    }

    private void sendPlayerHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Help"));
        player.sendMessage(Utils.translate("&c/rank  &7Tells you your rank"));
        player.sendMessage(Utils.translate("&c/ranks help  &7Displays this page"));
    }
}
