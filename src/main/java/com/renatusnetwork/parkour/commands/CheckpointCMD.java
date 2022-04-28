package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckpointCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("teleport"))) {
            Parkour.getCheckpointManager().teleportToCP(Parkour.getStatsManager().get(player));
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&cCheckpoint Help"));
        player.sendMessage(Utils.translate("&c/checkpoint [teleport] &7Teleports you to your previous checkpoint"));
    }
}
