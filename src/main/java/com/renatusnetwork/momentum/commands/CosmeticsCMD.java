package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CosmeticsCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        Momentum.getMenuManager().openInventory(Momentum.getStatsManager().get(player), "cosmetics", true);
        return false;
    }
}
