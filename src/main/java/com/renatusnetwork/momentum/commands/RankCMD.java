package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.ranks.RanksManager;
import com.renatusnetwork.momentum.data.ranks.RanksDB;
import com.renatusnetwork.momentum.data.ranks.RanksYAML;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RankCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        RanksManager ranksManager = Momentum.getRanksManager();

        if (player.hasPermission("momentum.admin")) {
            if (a.length == 0) {
                sendRank(player);
            } else if (a.length == 3 && a[0].equalsIgnoreCase("set")) {

                Player victim = Bukkit.getPlayer(a[1]);
                String rankName = a[2];

                if (victim == null) {
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    return true;
                }

                if (ranksManager.exists(rankName)) {
                    Rank rank = ranksManager.get(rankName);
                    Momentum.getStatsManager().get(victim).setRank(rank);
                    RanksDB.updateRank(victim.getUniqueId(), rank.getRankId());
                    player.sendMessage(Utils.translate("&7You set &c" + victim.getName() + "&7's rank to &c" + rank.getRankTitle()));
                } else {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {

                player.sendMessage(Utils.translate("&7Ranks Loaded: &c" + String.join("&7, &c",
                        Momentum.getRanksManager().getNames())));

            } else if (a.length >= 4 && a[0].equalsIgnoreCase("create")) {

                String rankName = a[1].toLowerCase();

                String[] split = Arrays.copyOfRange(a, 3, a.length);
                String rankTitle = String.join(" ", split);

                if (Utils.isDouble(a[2])) {
                    double rankUpPrice = Double.parseDouble(a[2]);
                    if (!ranksManager.exists(rankName)) {

                        // create in config
                        RanksYAML.create(rankName);
                        RanksYAML.setRankID(rankName, ranksManager.getRankList().size() + 1);
                        RanksYAML.setRankTitle(rankName, rankTitle);
                        RanksYAML.setRankUpPrice(rankName, rankUpPrice);

                        // create object
                        ranksManager.add(rankName);
                        player.sendMessage(Utils.translate("&7Created rank &c" + rankName));
                    } else {
                        player.sendMessage(Utils.translate("&4" + rankName + " &calready exists"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cThat is not a valid integer for rankup price"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("remove")) {

                String rankName = a[1].toLowerCase();

                if (ranksManager.exists(rankName)) {
                    if (Momentum.getRanksManager().get(rankName).getRankId() == 1) {
                        player.sendMessage(Utils.translate("&cYou cannot delete the default rank"));
                        return true;
                    }

                    // reset people in the rank
                    ranksManager.resetPlayersInRank(ranksManager.get(rankName));
                    // remove in config
                    RanksYAML.remove(rankName);
                    // remove object
                    ranksManager.remove(rankName);
                    player.sendMessage(Utils.translate("&7Removed rank &c" + rankName));
                } else {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cis not a rank"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {

                Momentum.getConfigManager().load("ranks");
                sender.sendMessage(Utils.translate("&7Loaded &cranks.yml &7from disk"));
                Momentum.getRanksManager().load();
                sender.sendMessage(Utils.translate("&7Loaded ranks from &cranks.yml&7, &c" +
                        Momentum.getRanksManager().getNames().size() + " &7total"));

            } else if (a.length == 3 && a[0].equalsIgnoreCase("setstage")) {

                Player victim = Bukkit.getPlayer(a[1]);

                if (victim == null) {
                    player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    return true;
                }

                if (Utils.isInteger(a[2])) {
                    int stage = Integer.parseInt(a[2]);

                    // can only be stage 1 or stage 2
                    if (stage == 1 || stage == 2) {
                        RanksDB.updateStage(victim.getUniqueId(), stage);
                        Momentum.getStatsManager().get(player).setRankUpStage(stage);
                        player.sendMessage(Utils.translate("&cYou updated &4" + victim.getName() + "'s Stage &cto &4" + stage));
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot set a stage that does not exist (1 or 2 only)"));
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("setprestiges")) {
                // check if they have joined parkour
                if (StatsDB.isPlayerInDatabase(a[1])) {

                    Player victim = Bukkit.getPlayer(a[1]);
                    if (Utils.isInteger(a[2])) {

                        int newPrestige = Integer.parseInt(a[2]);

                        if (newPrestige >= 0) {
                            // update ingame cache if theyre online
                            if (victim != null) {
                                PlayerStats victimStats = Momentum.getStatsManager().get(victim.getUniqueId().toString());
                                victimStats.setPrestiges(newPrestige);

                                if (victimStats.getPrestiges() > 0) {
                                    float prestigeMultiplier = Momentum.getSettingsManager().prestige_multiplier_per_prestige * victimStats.getPrestiges();

                                    if (prestigeMultiplier >= Momentum.getSettingsManager().max_prestige_multiplier)
                                        prestigeMultiplier = Momentum.getSettingsManager().max_prestige_multiplier;

                                    prestigeMultiplier = (float) (1.00 + (prestigeMultiplier / 100));

                                    victimStats.setPrestigeMultiplier(prestigeMultiplier);
                                }
                            }
                            // update in db
                            RanksDB.updatePrestiges(a[1], newPrestige);
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

    private void sendRank(Player player) {
        player.sendMessage(Utils.translate("&cYou are &6" +
                Momentum.getStatsManager().get(player).getRank().getRankTitle()));

        PlayerStats playerStats = Momentum.getStatsManager().get(player.getUniqueId().toString());

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
        player.sendMessage(getHelp(""));
        player.sendMessage(getHelp("help"));
        player.sendMessage(getHelp("list"));
        player.sendMessage(getHelp("load"));
        player.sendMessage(getHelp("create"));
        player.sendMessage(getHelp("remove"));
        player.sendMessage(getHelp("set"));
        player.sendMessage(getHelp("setstage"));
        player.sendMessage(getHelp("setprestiges"));
    }

    private void sendPlayerHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Help"));
        player.sendMessage(getHelp(""));
        player.sendMessage(getHelp("help"));
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "list":
                return Utils.translate("&c/ranks list  &7List all ranks");
            case "load":
                return Utils.translate("&c/ranks load  &7Loads ranks.yml then ranks");
            case "create":
                return Utils.translate("&c/ranks create <rankName> <rankUpPrice> <rankTitle>  &7Create a rank (can use spaces in <rankTitle>)");
            case "remove":
                return Utils.translate("&c/ranks remove <rankName>  &7Removes a rank from config/database and rank players down in the rank");
            case "set":
                return Utils.translate("&c/ranks set <player> <rankName>  &7Sets players rank");
            case "setstage":
                return Utils.translate("&c/ranks setstage <player> <stage>  &7Sets players stage in rankup (1/2)");
            case "setprestiges":
                return Utils.translate("&c/ranks setprestiges <player> <amount>  &7Sets players prestiges from database and cache");
            case "help":
                return Utils.translate("&c/ranks help  &7Displays this page");
            case "":
                return Utils.translate("&c/rank  &7Tells you your rank");
        }
        return "";
    }
}
