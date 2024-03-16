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

                if (playerStats == null || !playerStats.isLoaded())
                {
                    sender.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
                    return false;
                }

                statsManager.toggleNightVision(playerStats);
                playerStats.sendMessage(Utils.translate("&cYou have turned night vision " + (playerStats.hasNightVision() ? "&aOn" : "&cOff")));
            }
        }
        return true;
    }
}
