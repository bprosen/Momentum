package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ProfileCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length >= 0) {

            MenuManager menuManager = Parkour.getMenuManager();

            String menuName = "profile";
            int pageNumber = 1;

            if (menuManager.exists(menuName))
            {
                PlayerStats playerStats;

                // get properly through if-else
                if (a.length >= 1)
                    playerStats = Parkour.getStatsManager().getByName(a[0]);
                else
                    playerStats = Parkour.getStatsManager().get(player);

                if (playerStats != null) {

                    Inventory inventory = menuManager.getInventory(playerStats, menuName, pageNumber);

                    if (inventory != null) {
                        player.openInventory(inventory);
                        menuManager.updateInventory(playerStats, player.getOpenInventory(), menuName, pageNumber);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);

                    } else {
                        sender.sendMessage(Utils.translate("&cError loading the inventory"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[0] + " &cis not online!"));
                }
            }
        }
        return false;
    }
}
