package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        PlayerStats playerStats;

        // get properly through if-else
        if (a.length >= 1)
            playerStats = Momentum.getStatsManager().getByName(a[0]);
        else
            playerStats = Momentum.getStatsManager().get(player);

        if (playerStats != null && playerStats.isLoaded())
            Momentum.getMenuManager().openInventory(playerStats, player, "profile", true);
        else
            sender.sendMessage(Utils.translate("&4" + a[0] + " &cis not online or stats are not loaded yet"));
        return false;
    }
}
