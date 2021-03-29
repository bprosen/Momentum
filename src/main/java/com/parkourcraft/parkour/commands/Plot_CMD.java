package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class Plot_CMD implements CommandExecutor {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // send help
        if (a.length == 0) {

        // do create algorithm after
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("auto") || a[0].equalsIgnoreCase("create"))) {
            Parkour.getPlotsManager().createPlot(player);
        // teleport to plot
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("home") || a[0].equalsIgnoreCase("teleport"))) {
            Plot plot = Parkour.getPlotsManager().get(player.getName());

            // make sure they have a plot
            if (plot != null)
                player.teleport(plot.getSpawnLoc());
            else
                player.sendMessage(Utils.translate("&cYou do not have a plot to teleport to"));
        // visit someone else
        } else if (a.length == 2 && a[0].equalsIgnoreCase("visit")) {

        // clear stuff on plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("clear")) {

        // clear and delete their plot data
        } else if (a.length == 1 && a[0].equalsIgnoreCase("delete")) {
            // if they are confirming, delete it
            if (confirmMap.containsKey(player.getName())) {
                confirmMap.get(player.getName()).cancel();
                confirmMap.remove(player.getName());
                Parkour.getPlotsManager().deletePlot(player);
            // otherwise ask them to confirm it
            } else {
                confirmPlayer(player);
                player.sendMessage(Utils.translate("&cAre you sure? &7Type &c/plot delete &7again within 30 seconds to confirm"));
            }
        }
        return false;
    }

    private void confirmPlayer(Player player) {
        confirmMap.put(player.getName(), new BukkitRunnable() {
            public void run() {
                // make sure they are still in it
                if (confirmMap.containsKey(player.getName())) {
                    confirmMap.remove(player.getName());
                    player.sendMessage(Utils.translate("&cYou ran out of time to confirm"));
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 30));
    }
}
