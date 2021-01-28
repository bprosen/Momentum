package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class SetArmor_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        // setarmor <head/chest/leg/feet> MATERIAL [itemType] [leatherArmorColor]

        if (sender.isOp()) {
            sender.sendMessage(Utils.translate("&aThe following is only sent to OPs"));
            sender.sendMessage("/setarmor <head/chest/leg/feet> MATERIAL [itemType] [leatherArmorColor]");
        }

        if (sender instanceof Player && a.length >= 1) {
            Player player = (Player) sender;

            String bodyPart = a[0];
            String material = "AIR";
            int type = 0;
            int leatherArmorColor = -1;

            if (a.length >= 2)
                material = a[1];
            if (a.length >= 3 && Utils.isInteger(a[2]))
                type = Integer.parseInt(a[2]);
            if (a.length >= 4 && Utils.isInteger(a[3]))
                leatherArmorColor = Integer.parseInt(a[3]);

            Material itemMaterial = Material.getMaterial(material);

            if (itemMaterial != null) {
                ItemStack item = new ItemStack(itemMaterial, 1, (byte) type);

                if (leatherArmorColor > 0) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                    meta.setColor(Color.fromRGB(leatherArmorColor));
                    item.setItemMeta(meta);
                }

                if (item != null) {
                    String permission = "parkour.setarmor." + material.toLowerCase() + "." + type;

                    if (leatherArmorColor > 0)
                        permission += "." + leatherArmorColor;

                    if (player.isOp() || player.hasPermission(permission) || material.equals("AIR")) {
                        if (bodyPart.equals("head"))
                            player.getInventory().setHelmet(item);
                        else if (bodyPart.equals("chest"))
                            player.getInventory().setChestplate(item);
                        else if (bodyPart.equals("leg"))
                            player.getInventory().setLeggings(item);
                        else if (bodyPart.equals("feet"))
                            player.getInventory().setBoots(item);

                        if (player.isOp() && !material.equals("AIR"))
                            sender.sendMessage(Utils.translate("&ePermission: " + permission));
                    }
                }
            }
        }
        return true;
    }
}
