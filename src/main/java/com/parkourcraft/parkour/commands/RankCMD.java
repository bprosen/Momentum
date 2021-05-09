package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.rank.Rank;
import com.parkourcraft.parkour.data.rank.RanksManager;
import com.parkourcraft.parkour.data.rank.RanksDB;
import com.parkourcraft.parkour.data.rank.RanksYAML;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
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
        RanksManager ranksManager = Parkour.getRanksManager();

        if (player.hasPermission("pc-parkour.admin")) {
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
                    Parkour.getStatsManager().get(victim).setRank(rank);
                    RanksDB.updateRank(victim.getUniqueId(), rank.getRankId());
                    player.sendMessage(Utils.translate("&7You set &c" + victim.getName() + "&7's rank to &c" + rank.getRankTitle()));
                } else {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {

                player.sendMessage(Utils.translate("&7Ranks Loaded: &c" + String.join("&7, &c",
                        Parkour.getRanksManager().getNames())));

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
                    if (Parkour.getRanksManager().get(rankName).getRankId() == 1) {
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

                Parkour.getConfigManager().load("ranks");
                sender.sendMessage(Utils.translate("&7Loaded &cranks.yml &7from disk"));
                Parkour.getRanksManager().load();
                sender.sendMessage(Utils.translate("&7Loaded ranks from &cranks.yml&7, &c" +
                        Parkour.getRanksManager().getNames().size() + " &7total"));

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
                        Parkour.getStatsManager().get(player).setRankUpStage(stage);
                        player.sendMessage(Utils.translate("&cYou updated &4" + victim.getName() + "'s Stage &cto &4" + stage));
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot set a stage that does not exist (1 or 2 only)"));
                    }
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
                Parkour.getStatsManager().get(player).getRank().getRankTitle()));

        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());

        if (playerStats.getPrestiges() > 0)
            player.sendMessage(Utils.translate("&cYou have prestiged &6" + playerStats.getPrestiges() + " times"));
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
            case "help":
                return Utils.translate("&c/ranks help  &7Displays this page");
            case "":
                return Utils.translate("&c/rank  &7Tells you your rank");
        }
        return "";
    }
}
