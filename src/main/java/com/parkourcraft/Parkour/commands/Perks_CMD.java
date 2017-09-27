package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.data.PerkManager;
import com.parkourcraft.Parkour.storage.local.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Perks_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        FileManager.load("perks");
        sender.sendMessage("Loaded perks.yml from disk");

        PerkManager.loadAll();
        sender.sendMessage("Loaded all perks into memory");

        return true;
    }
}
