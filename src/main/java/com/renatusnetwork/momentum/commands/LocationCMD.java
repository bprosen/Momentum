package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.locations.LocationManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocationCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender instanceof Player && sender.isOp()) {
            Player player = (Player) sender;
            LocationManager locationManager = Momentum.getLocationManager();

            if (a.length == 2 && a[0].equalsIgnoreCase("tp")) {
                String locationName = a[1];

                if (locationManager.exists(locationName)) {
                    locationManager.teleport(player, locationName);
                    player.sendMessage(Utils.translate("&7Attempted to teleport you to &2" + locationName));
                } else {
                    player.sendMessage(Utils.translate("&7The location &2" + locationName + " &7does not exist"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("set")) {
                String locationName = a[1];

                if (!locationManager.exists(locationName)) {
                    locationManager.set(locationName, player.getLocation());
                    player.sendMessage(Utils.translate("&7Set location &2" + locationName));
                } else {
                    player.sendMessage(Utils.translate("&7The location &2" + locationName + " &7already exists"));
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("delete")) {
                String locationName = a[1];

                if (locationManager.exists(locationName)) {
                    locationManager.remove(locationName);
                    player.sendMessage(Utils.translate("&7Deleted location &2" + locationName));
                } else {
                    player.sendMessage(Utils.translate("&7The location &2" + locationName + " &7does not exist"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                Momentum.getLocationManager().load();
                player.sendMessage(Utils.translate("&7Loaded &2" + Utils.formatNumber(Momentum.getLocationManager().numLocations()) + " &7locations"));
            } else {
                sendHelp(sender);
            }
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&aLocation help"));
        sender.sendMessage(Utils.translate("&2/loc tp <location>  &7Teleports you to a location"));
        sender.sendMessage(Utils.translate("&2/loc set <location>  &7Sets location from your current position"));
        sender.sendMessage(Utils.translate("&2/loc delete <location>  &7Deletes location"));
        sender.sendMessage(Utils.translate("&2/loc load  &7Loads from disk"));
    }
}
