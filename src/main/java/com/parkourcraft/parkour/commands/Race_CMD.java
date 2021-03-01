package com.parkourcraft.parkour.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Race_CMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 0) {
            // send help
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            // send help
        } else if (a.length == 1) {
            // send/accept race request
        } else if (a.length == 2) {
            // send race request with bet
        } else {
            // send help
        }

        return false;
    }
}
