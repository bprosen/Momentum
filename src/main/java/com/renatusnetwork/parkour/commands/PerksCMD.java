package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PerksCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                Parkour.getConfigManager().load("perks");
                sender.sendMessage("Loaded perks.yml from disk");
                Parkour.getPerkManager().load();
                sender.sendMessage("Loaded all perks into memory");
            } else if (a.length == 3 && a[0].equalsIgnoreCase("setperk")) {
                Player player = Bukkit.getPlayer(a[1]);
                String perkName = a[2];

                if (player == null) {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    return true;
                }

                Perk perk = Parkour.getPerkManager().get(perkName);
                if (perk == null) {
                    sender.sendMessage(Utils.translate("&4" + perkName + " &cis not a perk"));
                    return true;
                }

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                if (!perk.hasRequirements(playerStats, player)) {
                    sender.sendMessage(Utils.translate("&4" + player.getName() + " &cdoes not have perk &4" + perkName));
                    return true;
                }

                Parkour.getPerkManager().setPerk(perk, player);
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {
                String perkList = "&7Perks List:";

                int iteration = 1;
                for (Perk perk : Parkour.getPerkManager().getPerks().values()) {
                    if ((iteration + 1) == Parkour.getPerkManager().getPerks().size())
                        perkList += " &6" + perk.getName();
                    else {
                        perkList += " &6" + perk.getName() + "&7,";
                        iteration++;
                    }
                }
                sender.sendMessage(Utils.translate(perkList));
            } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
            } else {
                sendHelp(sender);
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&6&lPerks Help"));
        sender.sendMessage(Utils.translate("&e/perks setperk (IGN) (perkName)  &7Sets perk armor/hat to player if they have it"));
        sender.sendMessage(Utils.translate("&e/perks load  &7Loads from disk"));
    }
}
