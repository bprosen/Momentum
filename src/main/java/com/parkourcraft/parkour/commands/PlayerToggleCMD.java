package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerToggleCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (a.length == 0)
            if (PlayerHider.containsPlayer(player)) {
                PlayerHider.showPlayer(player, false);
                player.sendMessage(Utils.translate("&aYou have turned on players"));
            } else {
                PlayerHider.hidePlayer(player, false);
                player.sendMessage(Utils.translate("&cYou have turned off players"));
            }
        return true;
    }
}
