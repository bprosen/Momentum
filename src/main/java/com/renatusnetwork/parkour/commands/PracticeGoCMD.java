package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PracticeGoCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {

            Player player = (Player) sender;
            if (a.length == 0)
                Parkour.getCheckpointManager().teleportToPrac(Parkour.getStatsManager().get(player));
        }
        return false;
    }
}
