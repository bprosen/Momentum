package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player && !sender.hasPermission("momentum.admin"))
            sender.sendMessage(Utils.translate("&cYou do not have permission to do this"));
        //
        // /bank load (true/false)
        // /bank help
        //
        else if (a.length == 2 && a[0].equalsIgnoreCase("load"))
        {
            boolean broadcast = Boolean.parseBoolean(a[1]);

            Momentum.getConfigManager().load("bank");
            sender.sendMessage(Utils.translate("&7Loaded &dbank.yml &7from disk"));
            Momentum.getBankManager().load();

            if (broadcast)
                Momentum.getBankManager().broadcastReset();
        }
        else
        {
            sender.sendMessage(Utils.translate("&d&lBank Help"));
            sender.sendMessage(Utils.translate(" &d/bank load [true/false]  &7Reloads bank info from file (true/false for broadcasting it)"));
            sender.sendMessage(Utils.translate(" &d/bank help  &7Displays this page"));
        }
        return false;
    }
}
