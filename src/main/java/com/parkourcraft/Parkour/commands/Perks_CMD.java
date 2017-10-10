package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.PerkManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Perks_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            FileManager.load("perks");
            sender.sendMessage("Loaded perks.yml from disk");

            Parkour.perks.load();
            sender.sendMessage("Loaded all perks into memory");
        } else
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");


        return true;
    }
}
