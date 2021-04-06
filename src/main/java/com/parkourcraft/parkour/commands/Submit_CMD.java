package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

                } else {
                    player.sendMessage(Utils.translate("&cYou have already submitted your plot!"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot to submit!"));
            }
        }
        return true;
    }
}
