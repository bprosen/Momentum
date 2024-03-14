package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CheckpointCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("teleport")))
        {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            Momentum.getCheckpointManager().teleportToCP(playerStats);
        }
        else if (player.hasPermission("momentum.admin") && a.length == 2 && a[0].equalsIgnoreCase("list"))
        {
            String playerName = a[1];

            Player target = Bukkit.getPlayer(playerName);

            if (target != null)
            {
                PlayerStats targetStats = Momentum.getStatsManager().get(target);

                String printStr = "&6" + target.getName() + " &7has checkpoints: ";

                // loop through
                for (Map.Entry<String, Location> entry : targetStats.getCheckpoints().entrySet())
                {
                    Location location = entry.getValue();

                    printStr += "&6" + entry.getKey() + " &e(" + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + ") ";
                }
                player.sendMessage(Utils.translate(printStr));
            }
            else
                player.sendMessage(Utils.translate("&c" + target.getName() + " is not online"));
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&cCheckpoint Help"));
        player.sendMessage(Utils.translate("&c/checkpoint [teleport] &7Teleports you to your previous checkpoint"));

        if (player.hasPermission("momentum.admin"))
            player.sendMessage(Utils.translate("&c/checkpoint list (player) &7Lists checkpoints the player has"));
    }
}
