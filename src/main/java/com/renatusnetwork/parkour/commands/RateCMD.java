package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.RatingDB;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class RateCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
        {
            return true;
        }

        Player player = (Player) sender;

        if (a.length >= 1) {

            // allow ability to get from title or name
            String[] split = Arrays.copyOfRange(a, 0, a.length);
            String levelName = String.join(" ", split);

            // if it does not get it from name, then attempt to get it from title
            Level level = Parkour.getLevelManager().getNameThenTitle(levelName);

            if (level != null)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (playerStats.hasCompleted(level))
                {
                    if (!level.hasRated(playerStats.getName()))
                    {
                        // menu
                        String menuName = "rate_level";
                        MenuManager menuManager = Parkour.getMenuManager();

                        if (menuManager.exists(menuName))
                        {
                            Inventory inventory = menuManager.getInventory(playerStats, menuName, 1);
                            if (inventory != null)
                            {
                                // copy it into new inv with new title
                                Inventory newInv = Bukkit.createInventory(null, inventory.getSize(), Utils.translate(
                                        inventory.getTitle().replace("%level_name%", level.getTitle())));
                                newInv.setContents(inventory.getContents());

                                player.openInventory(newInv);
                                menuManager.updateInventory(playerStats, player.getOpenInventory(), menuName, 1);
                            }
                            else
                                player.sendMessage(Utils.translate("&cError loading the inventory"));
                        }
                        else
                            player.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
                    }
                    else
                        sender.sendMessage(Utils.translate("&cYou have already rated &4" + level.getTitle()));
                }
                else
                    player.sendMessage(Utils.translate(
                            "&cYou have not completed &c" + level.getTitle() + "&c to be able to rate it"
                    ));
            }
            else
                player.sendMessage(Utils.translate("&cNo level named &4" + levelName + " &cexists"));

        }
        return false;
    }
}
