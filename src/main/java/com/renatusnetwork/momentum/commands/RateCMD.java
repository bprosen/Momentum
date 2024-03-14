package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.menus.MenuManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RateCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (a.length >= 1)
        {
            // allow ability to get from title or name
            String[] split = Arrays.copyOfRange(a, 0, a.length);
            String levelName = String.join(" ", split);

            // if it does not get it from name, then attempt to get it from title
            Level level = Momentum.getLevelManager().getNameThenTitle(levelName);

            if (level != null)
            {
                PlayerStats playerStats = Momentum.getStatsManager().get(player);

                if (playerStats.hasCompleted(level))
                {
                    MenuManager menuManager = Momentum.getMenuManager();

                    menuManager.addChoosingRating(playerStats, level);
                    // menu
                    menuManager.openInventory(playerStats, "rate_level", true);
                }
                else
                    player.sendMessage(Utils.translate(
                            "&cYou have not completed &c" + level.getTitle() + "&c to be able to rate it"
                    ));
            }
            else
                player.sendMessage(Utils.translate("&cNo level named &4" + levelName + " &cexists"));

        }
        return false;
    }
}
