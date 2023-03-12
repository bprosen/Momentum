package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuItemAction;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
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
            PlayerStats playerStats = Parkour.getStatsManager().get(player);

            if (a.length == 0)
            {
                if (!playerStats.isInTutorial())
                {
                    // do menu tp
                    MenuItemAction.performLevelTeleport(playerStats, player, Parkour.getLevelManager().getTutorialLevel());

                    // if they made it into tutorial, toggle it on
                    if (playerStats.inLevel() && playerStats.getLevel().getName().equalsIgnoreCase(Parkour.getLevelManager().getTutorialLevel().getName()))
                        playerStats.toggleTutorial();
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot enter the tutorial while in the tutorial"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("skip"))
            {
                if (playerStats.isInTutorial())
                {
                    // tp to spawn
                    playerStats.toggleTutorial();
                    SpawnCMD.teleportToSpawn(playerStats);
                    player.sendMessage(Utils.translate("\n&7Welcome to Parkour!\n"));
                }
                else
                {
                    player.sendMessage(Utils.translate("&cYou cannot skip the tutorial when not in it"));
                }
            }
        }

        return false;
    }
}
