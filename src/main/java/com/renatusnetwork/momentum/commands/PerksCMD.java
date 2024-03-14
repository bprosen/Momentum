package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PerksCMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender.isOp()) {
            if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                Momentum.getConfigManager().load("perks");
                sender.sendMessage(Utils.translate("&eLoaded perks.yml from disk"));
                Momentum.getPerkManager().load();
                sender.sendMessage(Utils.translate("&eLoaded all perks into memory"));

            } else if (a.length == 3 && a[0].equalsIgnoreCase("setperk")) {
                Player player = Bukkit.getPlayer(a[1]);
                String perkName = a[2];

                if (player == null) {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                    return true;
                }

                Perk perk = Momentum.getPerkManager().get(perkName);
                if (perk == null) {
                    sender.sendMessage(Utils.translate("&4" + perkName + " &cis not a perk"));
                    return true;
                }

                PlayerStats playerStats = Momentum.getStatsManager().get(player);
                if (!perk.hasRequirements(playerStats, player)) {
                    sender.sendMessage(Utils.translate("&4" + player.getName() + " &cdoes not have perk &4" + perkName));
                    return true;
                }

                Momentum.getPerkManager().setPerk(perk, playerStats);
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {
                String perkList = "&7Perks List:";

                int iteration = 1;
                for (Perk perk : Momentum.getPerkManager().getPerks().values()) {
                    if ((iteration + 1) == Momentum.getPerkManager().getPerks().size())
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
