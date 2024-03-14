package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.events.EventManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SwordShieldCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        EventManager eventManager = Momentum.getEventManager();

        // if running and participant, cancel
        if (!(eventManager.isEventRunning() && eventManager.isParticipant(player)))
        {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            SettingsManager settingsManager = Momentum.getSettingsManager();

            ItemStack swordItem = Utils.getSwordIfExists(player);
            boolean removedSword = removeItem(player, swordItem, settingsManager.sword_title);

            ItemStack shieldItem = Utils.getShieldIfExists(player);
            boolean removedShield = removeItem(player, shieldItem, settingsManager.shield_title);

            if (Momentum.getViaVersion().getPlayerVersion(UUID.fromString(playerStats.getUUID())) >= SettingsManager.PROTOCOL_1_9)
            {
                if (!removedShield)
                    Utils.addShield(playerStats);
            }
            else if (!removedSword)
                Utils.addSword(playerStats);
        }
        else
            player.sendMessage(Utils.translate("&cYou cannot do this"));

        return false;
    }

    private boolean removeItem(Player player, ItemStack itemStack, String itemTitle)
    {
        // take away item
        if (itemStack != null)
        {
            if (Utils.isItemFromTitle(player.getInventory().getItemInOffHand(), itemTitle))
                player.getInventory().setItemInOffHand(null);
            else
                player.getInventory().removeItem(itemStack);

            player.sendMessage(Utils.translate("&7You took away your " + itemTitle));
            return true;
        }
        return false;
    }
}