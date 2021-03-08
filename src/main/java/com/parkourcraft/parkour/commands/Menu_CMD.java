package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Menu_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        MenuManager menuManager = Parkour.getMenuManager();

        if (a.length > 0) {
            if (a[0].equalsIgnoreCase("list")) {
                if (sender.isOp())
                    sender.sendMessage(Utils.translate("&7Menus: &2" + String.join("&7, &2",
                                       menuManager.getMenuNames())));

            } else if (a[0].equalsIgnoreCase("open")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (a.length > 1) {
                        String menuName = a[1];
                        int pageNumber = 1;

                        if (a.length == 3 && Utils.isInteger(a[2]))
                                pageNumber = Integer.parseInt(a[2]);

                        if (menuManager.exists(menuName)) {

                            Inventory inventory = menuManager.getInventory(menuName, pageNumber);

                            if (inventory != null) {
                                player.openInventory(inventory);
                                menuManager.updateInventory(player, player.getOpenInventory(), menuName, pageNumber);
                            } else {
                                sender.sendMessage(Utils.translate("&cError loading the inventory"));
                            }
                        } else {
                            sender.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&cIncorrect parameters"));
                        sender.sendMessage(getHelp("open"));
                    }
                } else
                    sender.sendMessage(Utils.translate("&cMust be in-game to run this command"));
            } else if (a[0].equalsIgnoreCase("load")) {
                if (sender.isOp()) {
                    Parkour.getConfigManager().load("menus");

                    sender.sendMessage(Utils.translate("&7Loaded &2menus.yml &7from disk"));
                    menuManager.load();
                    sender.sendMessage(Utils.translate("&7Loaded menus from the config"));
                } else {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
                }
            } else {
                if (sender.isOp()) {
                    sender.sendMessage(Utils.translate("&c'&4" + a[0] + "&c' is an unknown parameter"));
                    sendHelp(sender);
                } else {
                    sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
                }
            }
        } else {
            if (sender.isOp())
                sendHelp(sender);
            else
                sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
        }

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("open"));
        sender.sendMessage(getHelp("load"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("list"))
            return Utils.translate("&2/menu list  &7Lists the configured menus");
        else if (cmd.equalsIgnoreCase("open"))
            return Utils.translate("&2/menu open <menu> [page#]  &7Opens inventory menu");
        else if (cmd.equalsIgnoreCase("load"))
            return Utils.translate("&2/menu load  &7Loads menus from config");
        return "";
    }

}
