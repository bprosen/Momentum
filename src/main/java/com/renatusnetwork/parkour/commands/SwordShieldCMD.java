package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.UUID;

public class SwordShieldCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        EventManager eventManager = Parkour.getEventManager();

        // if running and participant, cancel
        if (!(eventManager.isEventRunning() && eventManager.isParticipant(player)))
        {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            SettingsManager settingsManager = Parkour.getSettingsManager();

            ItemStack swordItem = Utils.getSwordIfExists(player);
            boolean removedSword = removeItem(player, swordItem, settingsManager.sword_title);

            ItemStack shieldItem = Utils.getShieldIfExists(player);
            boolean removedShield = removeItem(player, shieldItem, settingsManager.shield_title);

            if (Parkour.getViaVersion().getPlayerVersion(UUID.fromString(playerStats.getUUID())) >= SettingsManager.PROTOCOL_1_9)
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