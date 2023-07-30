package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PracticeCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (a.length == 0) {

            if (!(playerStats.getLevel() == null && !player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world))) {
                if (!playerStats.inRace()) {
                    if (!playerStats.isSpectating()) {
                        if (!playerStats.isEventParticipant()) {
                            if (!playerStats.isInInfinitePK()) {
                                // if it is a dropper level, disable /prac
                                if (playerStats.inLevel() && playerStats.getLevel().isDropperLevel()) {
                                    player.sendMessage(Utils.translate("&cPractice is disabled in &3&lDropper &clevels"));
                                    return true;
                                }

                            /*
                             check practice location first, if not null then reset or
                             then check if they are on ground then enable or cancel
                             */

                                // case of /unprac
                                if (label.equalsIgnoreCase("unprac"))
                                {
                                    if (playerStats.inPracticeMode())
                                        PracticeHandler.resetPlayer(playerStats, true);
                                    else
                                        player.sendMessage(Utils.translate("&cYou are not in practice mode"));
                                }
                                // in the case of /prac
                                else if (playerStats.inPracticeMode())
                                    PracticeHandler.resetPlayer(playerStats, true);
                                else if (player.isOnGround())
                                {
                                    playerStats.setPracticeMode(player.getLocation());

                                    SettingsManager settingsManager = Parkour.getSettingsManager();
                                    // create item and give
                                    ItemStack pracItem = new ItemStack(settingsManager.prac_type);
                                    ItemMeta itemMeta = pracItem.getItemMeta();
                                    itemMeta.setDisplayName(Utils.translate(settingsManager.prac_title));
                                    pracItem.setItemMeta(itemMeta);

                                    player.getInventory().setItem(settingsManager.prac_hotbar_slot, pracItem);

                                    player.sendMessage(Utils.translate("&aYou have enabled practice mode and a temporary checkpoint has been set"));
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot enable practice mode while in the air"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in infinite parkour"));
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
        } else if (label.equalsIgnoreCase("prac") && a.length == 1 && (a[0].equalsIgnoreCase("go") || a[0].equalsIgnoreCase("tp"))) {
                Parkour.getCheckpointManager().teleportToPracCP(playerStats);
        }
        return false;
    }
}
