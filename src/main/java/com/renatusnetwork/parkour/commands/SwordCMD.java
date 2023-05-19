package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class SwordCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        SettingsManager settingsManager = Parkour.getSettingsManager();

        if (a.length == 0) {
            EventManager eventManager = Parkour.getEventManager();
            // if running and participant, cancel
            if (eventManager.isEventRunning() && eventManager.isParticipant(player)) {
                player.sendMessage(Utils.translate("&cYou cannot do this"));
                return true;
            }

            PlayerStats playerStats = Parkour.getStatsManager().get(player);
            ItemStack swordItem = Utils.getSwordIfExists(playerStats, player.getInventory());

            // take away item
            if (swordItem != null) {
                player.getInventory().removeItem(swordItem);
                player.sendMessage(Utils.translate("&7You took away your &cSetup Sword"));
            } else {
                LinkedHashMap<Integer, ItemStack> swords = settingsManager.setup_swords;

                // create item and give
                if (swords.containsKey(playerStats.getPrestiges()))
                    swordItem = swords.get(playerStats.getPrestiges());
                else
                    swordItem = swords.get(swords.size() - 1); // its linked so safe to assume

                player.getInventory().setItem(settingsManager.sword_hotbar_slot, swordItem);
                player.sendMessage(Utils.translate("&7You have been given a &cSetup Sword"));
            }
        }
        return false;
    }
}
