package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.gameplay.PracticeHandler;
import com.parkourcraft.parkour.utils.Utils;
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

            if (playerStats.getLevel() != null) {
                if (player.isOnGround()) {
                    if (!playerStats.inRace()) {
                        if (playerStats.getPracticeLocation() != null) {
                            PracticeHandler.resetPlayer(player, true);
                        } else {
                            playerStats.setPracticeMode(player.getLocation());
                            player.sendMessage(Utils.translate("&aYou have enabled practice mode and a temporary checkpoint has been set"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot enable/disable practice mode while falling"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot enter practice mode when not in a level"));
            }
        }
        return false;
    }
}
