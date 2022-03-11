package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.data.stats.StatsDB;
import com.renatusnetwork.parkour.data.stats.StatsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class NightVisionCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        StatsManager statsManager = Parkour.getStatsManager();

        PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION,Integer.MAX_VALUE,0);

        if (a.length == 0) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            List<PotionEffect> potionEffects = playerStats.getLevel().getPotionEffects();

            // Enable

            if (!playerStats.hasNVstatus()) { // Status is off

                if (potionEffects.size() == 0) { // Level doesn't have effects
                    playerStats.setVisionStatus(true);
                    player.addPotionEffect(nightVision);
                    sender.sendMessage(Utils.translate("&aYou have been given night vision."));
                    return true;
                } else { // Level does have effects
                    boolean containsVision = false;
                    for (PotionEffect p : potionEffects) { // Check if one of those effects is night vision
                        if (p.getType() == PotionEffectType.NIGHT_VISION) {
                            containsVision = true;
                        }
                    }
                    if (containsVision) { // Level has vision but still sets stats to true
                        playerStats.setVisionStatus(true);
                        sender.sendMessage(Utils.translate("&aYou enabled night vision!"));
                    } else { // Level has effects but not vision, so give them the sight and set stats to true
                        playerStats.setVisionStatus(true);
                        player.addPotionEffect(nightVision);
                        sender.sendMessage(Utils.translate("&aYou have been given night vision."));
                    }
                }
                StatsDB.updatePlayerNightVision(playerStats);
            }

            // Disable

            if (playerStats.hasNVstatus()) {
                statsManager.clearEffects(player);
                playerStats.setVisionStatus(false);
                sender.sendMessage(Utils.translate("&4You have disabled night vision."));
            }

        }


    return true;
    }
}
