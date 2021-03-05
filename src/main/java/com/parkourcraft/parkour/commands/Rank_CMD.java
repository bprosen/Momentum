package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.levels.Levels_YAML;
import com.parkourcraft.parkour.data.rank.Rank;
import com.parkourcraft.parkour.data.rank.RanksManager;
import com.parkourcraft.parkour.data.rank.Ranks_DB;
import com.parkourcraft.parkour.data.rank.Ranks_YAML;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Rank_CMD implements CommandExecutor {

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
                    Ranks_DB.updateRank(victim.getUniqueId(), rank.getRankId());
                    player.sendMessage(Utils.translate("&7You set &c" + victim.getName() + "&7's rank to &c" + rank.getRankTitle()));
                } else {
                    player.sendMessage(Utils.translate("&4" + rankName + " &cdoes not exist"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {

                player.sendMessage(Utils.translate("&7Ranks Loaded: " + String.join("&7, &c",
                        Parkour.getRanksManager().getNames())));

            } else if (a.length >= 4 && a[0].equalsIgnoreCase("create")) {

                String rankName = a[1].toLowerCase();

                String[] split = Arrays.copyOfRange(a, 3, a.length);
                String rankTitle = String.join(" ", split);

                if (Utils.isDouble(a[2])) {
                    double rankUpPrice = Double.parseDouble(a[2]);
                    if (!ranksManager.exists(rankName)) {

                        // create in config
                        Ranks_YAML.create(rankName);
                        Ranks_YAML.setRankID(rankName, ranksManager.getRankList().size() + 1);
                        Ranks_YAML.setRankTitle(rankName, rankTitle);
                        Ranks_YAML.setRankUpPrice(rankName, rankUpPrice);

                        // create object
                        ranksManager.add(rankName);
                        player.sendMessage(Utils.translate("&7Created rank &c" + rankName));
                    } else {
                        player.sendMessage(Utils.translate("&4" + rankName + " &calready exists"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cThat is not a valid integer for rankup price"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {

                Parkour.getConfigManager().load("ranks");
                sender.sendMessage(Utils.translate("&7Loaded &cranks.yml &7from disk"));
                Parkour.getRanksManager().load();
                sender.sendMessage(Utils.translate("&7Loaded ranks from &cranks.yml&7, &c" +
                        Parkour.getRanksManager().getNames().size() + " &7total"));

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
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(Utils.translate("&c&lRanks Admin Help"));
        player.sendMessage(getHelp(""));
        player.sendMessage(getHelp("help"));
        player.sendMessage(getHelp("list"));
        player.sendMessage(getHelp("load"));
        player.sendMessage(getHelp("create"));
        player.sendMessage(getHelp("set"));
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
            case "set":
                return Utils.translate("&c/ranks set <player> <rankName>  &7Sets players rank");
            case "help":
                return Utils.translate("&c/ranks help  &7Displays this page");
            case "":
                return Utils.translate("&c/rank  &7Tells you your rank");
        }
        return "";
    }
}
