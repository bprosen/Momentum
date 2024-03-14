package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PreviewCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (a.length == 1 && a[0].equalsIgnoreCase("leave"))
                Momentum.getStatsManager().get(player).resetPreviewLevel();
            else
                sender.sendMessage(Utils.translate("&cInvalid arguments, try &4/preview leave"));
        }
        return false;
    }
}
