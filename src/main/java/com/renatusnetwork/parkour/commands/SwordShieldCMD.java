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
                    addShield(playerStats);
            }
            else if (!removedSword)
                addSword(playerStats);
        }
        else
            player.sendMessage(Utils.translate("&cYou cannot do this"));

        return false;
    }

    private void addSword(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack swordItem;
        LinkedHashMap<Integer, ItemStack> swords = settingsManager.setup_swords;

        // create item and give
        if (swords.containsKey(playerStats.getPrestiges()))
            swordItem = swords.get(playerStats.getPrestiges());
        else
            swordItem = swords.get(swords.size() - 1); // its linked so safe to assume

        player.getInventory().setItem(settingsManager.sword_hotbar_slot, swordItem);
        player.sendMessage(Utils.translate("&7You have been given a " + settingsManager.sword_title));
    }

    private void addShield(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        SettingsManager settingsManager = Parkour.getSettingsManager();

        ItemStack shieldItem = new ItemStack(Material.SHIELD);
        ItemMeta meta = shieldItem.getItemMeta();
        meta.setDisplayName(Utils.translate(settingsManager.shield_title));

        if (playerStats.hasPrestiges())
            Utils.addGlow(meta);

        shieldItem.setItemMeta(meta);

        player.getInventory().setItem(settingsManager.sword_hotbar_slot, shieldItem);
        player.sendMessage(Utils.translate("&7You have been given a " + settingsManager.shield_title));
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