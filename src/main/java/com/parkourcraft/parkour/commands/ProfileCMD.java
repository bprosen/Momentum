package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.utils.Utils;
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

        if (a.length == 0) {

            MenuManager menuManager = Parkour.getMenuManager();

            String menuName = "profile";
            int pageNumber = 1;

            if (a.length == 3 && Utils.isInteger(a[2]))
                pageNumber = Integer.parseInt(a[2]);

            if (menuManager.exists(menuName)) {

                Inventory inventory = menuManager.getInventory(menuName, pageNumber);

                if (inventory != null) {
                    player.openInventory(inventory);
                    menuManager.updateInventory(player, player.getOpenInventory(), menuName, pageNumber);
                    Parkour.getStatsManager().loadProfile(Parkour.getStatsManager().get(player));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.1f, 2f);
                } else {
                    sender.sendMessage(Utils.translate("&cError loading the inventory"));
                }
            }
        }
        return false;
    }
}
