package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
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

        if (a.length == 0) {
            Parkour.getCheckpointManager().teleportPlayer(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("teleport")) {
            Parkour.getCheckpointManager().teleportPlayer(player);
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&cCheckpoint Help"));
        player.sendMessage(Utils.translate("&c/checkpoint  &7teleports you to your previous checkpoint"));
        player.sendMessage(Utils.translate("&c/checkpoint  &7teleports you to your previous checkpoint"));
    }
}
