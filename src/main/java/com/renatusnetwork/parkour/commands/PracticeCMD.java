package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PracticeCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 0) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (playerStats.getLevel() != null) {
                if (!playerStats.inRace()) {
                    if (playerStats.getPlayerToSpectate() == null) {
                        if (!playerStats.isEventParticipant()) {

                            // if it is a dropper level, disable /prac
                            if (playerStats.inLevel() && playerStats.getLevel().isDropperLevel()) {
                                player.sendMessage(Utils.translate("&cPractice is disabled in &3&lDropper &clevels"));
                                return true;
                            }

                            // check practice location first, if not null, reset, then check if they are on ground, if they are, enable, if not, cancel
                            if (playerStats.getPracticeLocation() != null) {
                                PracticeHandler.resetPlayer(player, true);
                            } else if (player.isOnGround()) {
                                playerStats.setPracticeMode(player.getLocation());
                                playerStats.disableLevelStartTime();
                                player.sendMessage(Utils.translate("&aYou have enabled practice mode and a temporary checkpoint has been set"));
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot enable practice mode while in the air"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot enter practice mode while in /spectator"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while racing"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot enter practice mode when not in a level"));
            }
        }
        return false;
    }
}
