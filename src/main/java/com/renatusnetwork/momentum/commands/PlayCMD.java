package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PlayCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {

            Player player = (Player) sender;

            if (a.length >= 1)
            {
                // allow ability to get from title or name
                String levelName = a[0].toLowerCase();

                if (a.length > 1)
                {
                    String[] split = Arrays.copyOfRange(a, 0, a.length);
                    levelName = String.join(" ", split);
                }

                // if it does not get it from name, then attempt to get it from title
                Level level = Momentum.getLevelManager().get(levelName);
                if (level == null)
                    level = Momentum.getLevelManager().getFromTitle(levelName);

                // teleport to level if level is found
                if (level != null)
                    Momentum.getLevelManager().teleportToLevel(Momentum.getStatsManager().get(player), level);
                else
                    player.sendMessage(Utils.translate("&4" + levelName + " &cis not a level"));

            }
            else
            {
                player.sendMessage(Utils.translate("&cInvalid usage, do &4/play (levelName)"));
            }
        }
        return false;
    }
}
