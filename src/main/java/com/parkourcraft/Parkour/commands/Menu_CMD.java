package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.data.MenuManager;
import com.parkourcraft.Parkour.data.StatsManager;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.local.FileManager;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Menu_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (a.length > 0) {
            if (a[0].equalsIgnoreCase("list")) {
                sender.sendMessage(
                        ChatColor.GRAY + "Menus: "
                                + ChatColor.GREEN + String.join(
                                ChatColor.GRAY + ", " + ChatColor.GREEN,
                                MenuManager.getMenuNames()
                        )
                );
            } else if (a[0].equalsIgnoreCase("open")) {
                if (sender instanceof Player) {
                    if (a.length > 1) {
                        String menuName = a[1];
                        int pageNumber = 1;

                        if (a.length == 3
                                && Utils.isInteger(a[2]))
                                pageNumber = Integer.parseInt(a[2]);

                        if (MenuManager.exists(menuName)) {
                            Player player = ((Player) sender).getPlayer();
                            PlayerStats playerStats = StatsManager.get(player);

                            Inventory inventory = MenuManager.getInventory(menuName, pageNumber);

                            if (inventory != null) {
                                player.openInventory(inventory);
                                MenuManager.updateInventory(player, player.getOpenInventory(), menuName);
                            } else
                                sender.sendMessage(ChatColor.RED + "Error loading the inventory");
                        } else
                            sender.sendMessage(
                                    ChatColor.GRAY + "'" +
                                    ChatColor.RED + menuName +
                                    ChatColor.GRAY + "' is not an existing menu"
                            );
                    } else {
                        sender.sendMessage(ChatColor.RED + "Incorrect parameters");
                        sender.sendMessage(getHelp("open"));
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "Must be in-game to run this command");
            } else if (a[0].equalsIgnoreCase("load")) {
                FileManager.load("menus");

                sender.sendMessage(
                        ChatColor.GRAY + "Loaded " +
                        ChatColor.GREEN + "menus.yml" +
                        ChatColor.GRAY + " from disk"
                );

                MenuManager.loadMenus();
                sender.sendMessage(ChatColor.GRAY + "Loaded menus from the config");
            } else {
                sender.sendMessage(
                        ChatColor.RED + "'" +
                        ChatColor.DARK_RED + a[0] +
                        ChatColor.RED + "' is an unknown parameter"
                );
                sendHelp(sender);
            }
        } else
            sendHelp(sender);

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("list"));
        sender.sendMessage(getHelp("open"));
        sender.sendMessage(getHelp("load"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("list"))
            return ChatColor.GREEN + "/menu list" +
                    ChatColor.GRAY + " Lists the configured menus";
        else if (cmd.equalsIgnoreCase("open"))
            return ChatColor.GREEN + "/menu open <menu> [page#]" +
                    ChatColor.GRAY + " Opens inventory menu";
        else if (cmd.equalsIgnoreCase("load"))
            return ChatColor.GREEN + "/menu load" +
                    ChatColor.GRAY + " Loads menus from config";
        return "";
    }

}
