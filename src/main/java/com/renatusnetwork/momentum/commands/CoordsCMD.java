package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CoordsCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.translate("&cConsole cannot run this command"));
            return true;
        }

        Player player = (Player) sender;
        int precision = 3;

        if (args.length == 1) {
            if (Utils.isInteger(args[0])) {
                // simple clamp
                precision = Math.min(5, Math.max(0, Integer.parseInt(args[0])));
            } else {
                player.sendMessage(Utils.translate("&4" + args[0] + " &cis not a valid integer"));
                return false;
            }
        } else if (args.length > 1) {
            sendHelp(sender);
            return false;
        }

        sendCoords(player, precision);
        return false;
    }

    private void sendCoords(Player player, int precision) {
        Location loc = player.getLocation();
        String x = Utils.formatDecimal(loc.getX(), false, precision, precision);
        String y = Utils.formatDecimal(loc.getY(), false, precision, precision);
        String z = Utils.formatDecimal(loc.getZ(), false, precision, precision);
        String f = Utils.formatDecimal(loc.getYaw(), false, precision, precision);
        player.sendMessage("");
        player.sendMessage(Utils.translate("&5&lx: &d" + x));
        player.sendMessage(Utils.translate("&5&ly: &d" + y));
        player.sendMessage(Utils.translate("&5&lz: &d" + z));
        player.sendMessage(Utils.translate("&5&lf: &d" + f));
        player.sendMessage("");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&6/coords [precision]"));
    }
}
