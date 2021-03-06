package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Rankup_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        MenuManager menuManager = Parkour.getMenuManager();

        if (a.length == 0) {
            if (Parkour.getStatsManager().get(player).isLastRank()) {
                player.sendMessage(Utils.translate("&cYou are at last rank!"));
                return true;
            }

            String menuName = "rankup";
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
