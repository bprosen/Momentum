package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Checkpoint_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (a.length == 0) {
            teleportPlayerToCheckpoint(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("teleport")) {
            teleportPlayerToCheckpoint(player);
        } else {
            sendHelp(player);
        }
        return false;
    }

    private void teleportPlayerToCheckpoint(Player player) {
        if (Parkour.getStatsManager().get(player).getCheckpoint() != null) {
            Parkour.getCheckpointManager().teleportPlayer(player);
            player.sendMessage(Utils.translate("&eYou have been teleported to your checkpoint"));
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a saved checkpoint"));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(Utils.translate("&cCheckpoint Help"));
        player.sendMessage(Utils.translate("&c/checkpoint  &7teleports you to your previous checkpoint"));
        player.sendMessage(Utils.translate("&c/checkpoint  &7teleports you to your previous checkpoint"));
    }
}
