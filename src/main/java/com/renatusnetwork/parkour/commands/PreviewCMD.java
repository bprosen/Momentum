package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import jdk.internal.org.jline.reader.impl.UndoTree;
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
                Parkour.getStatsManager().get(player).resetPreviewLevel();
            else
                sender.sendMessage(Utils.translate("&cInvalid arguments, try &4/preview leave"));
        }
        return false;
    }
}
