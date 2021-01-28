package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Perks_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            Parkour.getConfigManager().load("perks");
            sender.sendMessage("Loaded perks.yml from disk");
            Parkour.getPerkManager().load();
            sender.sendMessage("Loaded all perks into memory");
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
        }
        return true;
    }
}
