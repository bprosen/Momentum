package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PC_Parkour_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            Parkour.getConfigManager().load("settings");
            Parkour.getSettingsManager().load(Parkour.getConfigManager().get("settings"));
            sender.sendMessage("Loaded settings.yml from disk");
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
        }
        return true;
    }

}
