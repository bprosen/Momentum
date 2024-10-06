package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HotbarCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // clear inv first
            player.getInventory().clear();
            player.getInventory().setItemInOffHand(null); // in case 1.8 doesnt clear item in offhand

            Utils.setHotbar(player);
            Utils.refreshHotbarState(player);

            player.sendMessage(Utils.translate("&7You have refreshed your hotbar"));
        } else {
            sender.sendMessage(Utils.translate("&cConsole cannot do this"));
        }
        return false;
    }
}
