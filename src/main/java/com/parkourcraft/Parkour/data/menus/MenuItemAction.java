package com.parkourcraft.Parkour.data.menus;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.*;
import com.parkourcraft.Parkour.data.levels.LevelObject;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class MenuItemAction {

    private static void runCommands(Player player, List<String> commands, List<String> consoleCommands) {
        for (String command : commands)
            player.performCommand(command.replace("%player%", player.getName()));

        for (String command : consoleCommands)
            Bukkit.dispatchCommand(
                    Parkour.getPlugin().getServer().getConsoleSender(),
                    command.replace("%player%", player.getName())
            );
    }

    public static void perform(Player player, MenuItem menuItem) {
        String itemType = menuItem.getType();
        boolean commands = menuItem.getCommands().size() > 0 || menuItem.getConsoleCommands().size() > 0;

        if (itemType.equals("perk")) {
            Perk perk = PerkManager.get(menuItem.getTypeValue());

            if (perk != null) {
                PlayerStats playerStas = StatsManager.get(player);

                if (commands
                        && perk.hasRequirements(playerStas, player)) {
                    player.closeInventory();
                    runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
                } else if (!playerStas.hasPerk(perk.getName())
                            && perk.getPrice() > 0) {
                        int playerBalance = (int) Parkour.economy.getBalance(player);

                        if (playerBalance > perk.getPrice()) {
                            Parkour.economy.withdrawPlayer(player, perk.getPrice());
                            PerkManager.bought(playerStas, perk);
                            MenuManager.updateInventory(player, player.getOpenInventory());
                        }
                }
            }
        } else {
            if (itemType.equals("level")) {
                PlayerStats playerStats = StatsManager.get(player);
                LevelObject level = LevelManager.get(menuItem.getTypeValue());

                int playerLevelCompletions = playerStats.getLevelCompletionsCount(menuItem.getTypeValue());

                if (playerLevelCompletions < level.getMaxCompletions()) {
                    player.closeInventory();
                    player.teleport(level.getStartLocation());
                    player.sendMessage(
                            ChatColor.GRAY + "You were teleported to the beginning of "
                                    + level.getFormattedTitle()
                    );
                }
            } else if (itemType.equals("teleport")) {
                Location location = LocationManager.get(menuItem.getTypeValue());

                if (location != null) {
                    player.closeInventory();
                    player.teleport(location);
                }
            } else if (itemType.equals("open")) {
                Menu menu = MenuManager.getMenuFromStartingChars(menuItem.getTypeValue());

                if (menu != null) {
                    int pageeNumber = Utils.getTrailingInt(menuItem.getTypeValue());

                    Inventory inventory = MenuManager.getInventory(menu.getName(), pageeNumber);

                    if (inventory != null) {
                        player.closeInventory();
                        player.openInventory(inventory);
                        MenuManager.updateInventory(player, player.getOpenInventory(), menu.getName(), pageeNumber);
                    }
                }
            }

            if (commands)
                runCommands(player, menuItem.getCommands(), menuItem.getConsoleCommands());
        }
    }
}
