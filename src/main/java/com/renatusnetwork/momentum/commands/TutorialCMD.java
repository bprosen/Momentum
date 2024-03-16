package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.menus.MenuItemAction;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            if (playerStats == null || !playerStats.isLoaded())
            {
                player.sendMessage(Utils.translate("&cYou cannot do this while your stats are loading"));
                return false;
            }

            if (a.length == 0)
            {
                if (playerStats.isInTutorial())
                {
                    player.sendMessage(Utils.translate("&cYou cannot enter the tutorial while in the tutorial"));
                    return false;
                }

                // do menu tp
                MenuItemAction.performLevelTeleport(playerStats, Momentum.getLevelManager().getTutorialLevel());

                // if they made it into tutorial, toggle it on
                if (playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(Momentum.getLevelManager().getTutorialLevel().getName()))
                    playerStats.setTutorial(true);
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("skip"))
            {
                if (!playerStats.isInTutorial())
                {
                    player.sendMessage(Utils.translate("&cYou cannot skip the tutorial when not in it"));
                    return false;
                }

                // tp to spawn
                playerStats.setTutorial(false);
                Momentum.getLocationManager().teleportToSpawn(playerStats, player);
                player.sendMessage("");
                player.sendMessage(Utils.translate("&b&lWelcome to Parkour!"));
                player.sendMessage("");
            }
        }

        return false;
    }
}
