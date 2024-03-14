package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.menus.MenuManager;
import com.renatusnetwork.momentum.data.ranks.RanksYAML;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class RankupCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        PlayerStats playerStats = Momentum.getStatsManager().get(player);
        MenuManager menuManager = Momentum.getMenuManager();

        if (a.length == 0) {
            if (playerStats.isLastRank()) {
                player.sendMessage(Utils.translate("&cYou are at last rank!"));
                return true;
            }

            String menuName = null;
            // stage 1, meaning coin rankup part
            if (playerStats.getRankUpStage() == 1)
                menuName = "coin-rankup";
            // stage 2, meaning level rankup part
            else if (playerStats.getRankUpStage() == 2) {
                // get if it is a single level style rankup (expert and up)
                if (RanksYAML.isSingleLevelRankup(playerStats.getRank().getRankName()))
                    menuName = "single-level-rankup";
                else
                    menuName = "double-level-rankup";
            }

            if (menuManager.exists(menuName)) {

                Inventory inventory = menuManager.getInventory(menuName, 1);

                if (inventory != null) {
                    player.openInventory(inventory);
                    menuManager.updateInventory(player, player.getOpenInventory(), menuName, 1);
                } else {
                    sender.sendMessage(Utils.translate("&cError loading the inventory"));
                }
            } else {
                sender.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
            }
        }
        return false;
    }
}
