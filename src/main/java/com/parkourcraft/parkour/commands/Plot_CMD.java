package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Plot_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // send help
        if (a.length == 0) {
            Parkour.getPSubmittedManager().createPlot(player);
        // do create algorithm after
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("auto") || a[0].equalsIgnoreCase("create"))) {

        // teleport to plot
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("home") || a[0].equalsIgnoreCase("teleport"))) {

        // visit someone else
        } else if (a.length == 2 && a[0].equalsIgnoreCase("visit")) {

        // clear stuff on plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("clear")) {

        // clear and delete their plot data
        } else if (a.length == 1 && a[0].equalsIgnoreCase("delete")) {

        }
        return false;
    }
}
