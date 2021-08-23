package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.SettingsManager;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class SwordCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        SettingsManager settingsManager = Parkour.getSettingsManager();

        if (a.length == 0) {

            ItemStack swordItem = Utils.getSwordIfExists(player.getInventory());

            // take away item
            if (swordItem != null) {
                player.getInventory().removeItem(swordItem);
                player.sendMessage(Utils.translate("&7You took away your &cSetup Sword"));
            } else {
                // create item and give
                swordItem = new ItemStack(settingsManager.sword_type);
                ItemMeta itemMeta = swordItem.getItemMeta();
                itemMeta.setDisplayName(Utils.translate(settingsManager.sword_title));
                itemMeta.setLore(new ArrayList<String>() {{ add(Utils.translate("&7Use this to help out with precise setup!")); }});
                swordItem.setItemMeta(itemMeta);

                player.getInventory().setItem(settingsManager.sword_hotbar_slot, swordItem);
                player.sendMessage(Utils.translate("&7You have been given a &cSetup Sword"));
            }
        }
        return false;
    }
}
