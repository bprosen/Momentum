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

public class HotbarCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            boolean hadSword = Utils.getSwordIfExists(player) != null;
            boolean hadShield = Utils.getShieldIfExists(player) != null;

            // clear inv first
            for (int i = 0; i < player.getInventory().getSize(); i++)
            {
                ItemStack item = player.getInventory().getItem(i);

                if (item != null && item.getType() != Material.AIR)
                    player.getInventory().removeItem(item);
            }

            Utils.setHotbar(player);

            StatsManager statsManager = Momentum.getStatsManager();
            PlayerStats playerStats = statsManager.get(player);
            SettingsManager settingsManager = Momentum.getSettingsManager();

            // refresh stateful items
            if (playerStats.inLevel())
                Utils.addItemToHotbar(settingsManager.leave_item, player.getInventory(), settingsManager.leave_hotbar_slot);

            if (playerStats.inPracticeMode())
                Utils.addItemToHotbar(settingsManager.prac_item, player.getInventory(), settingsManager.prac_hotbar_slot);

            if (statsManager.containsHiddenPlayer(player))
                Utils.setDisabledPlayersItem(player.getInventory(), Utils.getSlotFromInventory(player.getInventory(), Utils.translate("&7Players » &aEnabled")));

            if (hadSword)
                Utils.addSword(playerStats);
            else if (hadShield)
                Utils.addShield(playerStats);

            player.sendMessage(Utils.translate("&7You have refreshed your hotbar"));
        }
        else
            sender.sendMessage(Utils.translate("&cConsole cannot do this"));
        return false;
    }
}
