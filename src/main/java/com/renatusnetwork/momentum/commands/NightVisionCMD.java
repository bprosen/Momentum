package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player)
        {

            Player player = (Player) sender;
            StatsManager statsManager = Momentum.getStatsManager();

            if (a.length == 0)
            {
                PlayerStats playerStats = statsManager.get(player);

                if (playerStats.isLoaded())
                {
                    if (!playerStats.hasNightVision())
                    { // enable

                        playerStats.setNightVision(true);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
                        sender.sendMessage(Utils.translate("&aYou have enabled Night Vision"));
                    }
                    else
                    { // disable

                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

                        playerStats.setNightVision(false);
                        player.sendMessage(Utils.translate("&cYou have disabled Night Vision"));
                    }
                    // update db
                    StatsDB.updatePlayerNightVision(playerStats.getUUID(), playerStats.hasNightVision());
                }
                else
                {
                    sender.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
                }
            }
        }
        return true;
    }
}
