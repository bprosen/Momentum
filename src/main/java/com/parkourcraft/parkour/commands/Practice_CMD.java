package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Practice_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 0) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            if (playerStats.inPracticeMode()) {
                // disable
            } else {
                // enable
            }
        }
        return false;
    }
}
