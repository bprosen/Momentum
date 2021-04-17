package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.menus.MenuManager;
import com.parkourcraft.parkour.data.menus.Menus_YAML;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public class Submit_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        // do submit gui
        if (a.length == 1) {
            Plot plot = Parkour.getPlotsManager().get(player.getName());
            // they have a plot
            if (plot != null) {
                // already submitted
                if (!plot.isSubmitted()) {

                    // submit map!
                    openMenu(player, "submit-plot");
                } else {
                    player.sendMessage(Utils.translate("&cYou have already submitted your plot!"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot to submit!"));
            }
        } else {
            // admin section
            if (player.hasPermission("pc-parkour.admin")) {
                // send list of plots in gui
                if (a.length == 1 && a[0].equalsIgnoreCase("list")) {

                    // open submitted plots list
                    openMenu(player, "submitted-plots");
                } else if (a.length == 2 && a[0].equalsIgnoreCase("accept")) {

                    String plotOwner = a[2];
                    Plot targetPlot = Parkour.getPlotsManager().get(plotOwner);

                    if (targetPlot != null) {
                        if (targetPlot.isSubmitted()) {

                            targetPlot.desubmit();
                            Parkour.getPlotsManager().addPlotToMenu(targetPlot);
                            player.sendMessage(Utils.translate("&7You accepted &4" + plotOwner + "&7's Plot"));

                            Player target = Bukkit.getPlayer(plotOwner);

                            if (target != null) {
                                target.sendMessage(Utils.translate("&cYour plot has been accepted, congratulations!"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + plotOwner + "&c's Plot is not submitted!"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&4" + plotOwner + " &cdoes not have a Plot"));
                    }

                } else if (a.length > 2 && a[0].equalsIgnoreCase("deny")) {

                    String plotOwner = a[2];

                    String[] split = Arrays.copyOfRange(a, 3, a.length);
                    // make sure it is not too long of a reason
                    if (split.length > 10) {
                        player.sendMessage(Utils.translate("&7Too long of a reason! Make it &c10 words &7or under"));
                        return true;
                    }

                    String reason = String.join(" ", split);

                    Plot targetPlot = Parkour.getPlotsManager().get(plotOwner);
                    if (targetPlot != null) {
                        if (targetPlot.isSubmitted()) {

                            targetPlot.desubmit();
                            player.sendMessage(Utils.translate("&cYou denied &4" + plotOwner + "&c's Plot"));

                            Player target = Bukkit.getPlayer(plotOwner);

                            if (target != null) {
                                target.sendMessage(Utils.translate("&cYour plot has been denied, try again soon!"));
                                target.sendMessage(Utils.translate("&7With reason: &c" + reason));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&4" + plotOwner + "&c's Plot is not submitted!"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&4" + plotOwner + " &cdoes not have a Plot"));
                    }
                }
            }
        }
        return true;
    }

    private void openMenu(Player player, String menuName) {
        MenuManager menuManager = Parkour.getMenuManager();

        if (menuManager.exists(menuName)) {

            Inventory inventory = menuManager.getInventory(menuName, 1);

            if (inventory != null) {
                player.openInventory(inventory);
                menuManager.updateInventory(player, player.getOpenInventory(), menuName, 1);
            } else {
                player.sendMessage(Utils.translate("&cError loading the inventory"));
            }
        } else {
            player.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
        }
    }
}
