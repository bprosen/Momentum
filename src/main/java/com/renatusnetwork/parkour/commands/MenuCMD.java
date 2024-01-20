package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenuCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        MenuManager menuManager = Parkour.getMenuManager();

        if (sender instanceof Player && sender.isOp())
        {
            if (a.length == 1 && a[0].equalsIgnoreCase("list"))
                sender.sendMessage(Utils.translate("&7Menus: &2" + menuManager.getMenuNames()));
            else if (a.length >= 2 && a[0].equalsIgnoreCase("open"))
            {
                Player player = (Player) sender;

                String menuName = a[1];
                int pageNumber = 1;

                if (a.length == 3 && Utils.isInteger(a[2]))
                    pageNumber = Integer.parseInt(a[2]);

                if (menuManager.exists(menuName))
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    Inventory inventory = menuManager.getInventory(playerStats, menuName, pageNumber);

                    if (inventory != null)
                    {
                        player.openInventory(inventory);
                        menuManager.updateInventory(playerStats, player.getOpenInventory(), menuName, pageNumber);
                    }
                    else
                        sender.sendMessage(Utils.translate("&cError loading the inventory"));
                }
                else
                    sender.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("load"))
            {
                Parkour.getConfigManager().load("menus");

                sender.sendMessage(Utils.translate("&7Loaded &2menus.yml &7from disk"));
                menuManager.reload();
                sender.sendMessage(Utils.translate("&7Loaded menus from the config"));
            }
            else
                sendHelp(sender);
        }
        return true;
    }

    private static void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&2&lMenus Help"));
        sender.sendMessage(Utils.translate("&2/menu list  &7Lists the configured menus"));
        sender.sendMessage(Utils.translate("&2/menu open <menu> [page#]  &7Opens inventory menu"));
        sender.sendMessage(Utils.translate("&2/menu load  &7Loads menus from config"));
        sender.sendMessage(Utils.translate("&2/menu help  &7Displays this screen"));
    }
}
