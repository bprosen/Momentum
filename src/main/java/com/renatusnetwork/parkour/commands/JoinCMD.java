package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.menus.MenuItemAction;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {

            Player player = (Player) sender;
            if (a.length == 1)
            {
                Player target = Bukkit.getPlayer(a[0]);

                if (target != null)
                {
                    PlayerStats playerStats = Parkour.getStatsManager().get(player);
                    PlayerStats targetStats = Parkour.getStatsManager().get(target);

                    if (targetStats.inLevel())
                    {
                        Level level = targetStats.getLevel();

                        // since ascendance is a free-roam map...
                        if (!level.isAscendanceLevel())
                        {
                            if (!level.isRankUpLevel())
                            {
                                if (!level.isEventLevel())
                                {
                                    if (!level.isRaceLevel())
                                    {
                                        boolean teleport = true;

                                        // not all levels have a price, so do a boolean switch
                                        if (level.getPrice() > 0 && !playerStats.hasBoughtLevel(level.getName()) && playerStats.getLevelCompletionsCount(level.getName()) <= 0)
                                        {
                                            teleport = false;
                                            player.sendMessage(Utils.translate("&cYou first need to buy " + level.getFormattedTitle() + " &cbefore doing &4/join " + targetStats.getPlayerName()));
                                        }

                                        // if still allowed, tp them!
                                        if (teleport)
                                            MenuItemAction.performLevelTeleport(playerStats, player, level);
                                    }
                                    else
                                    {
                                        player.sendMessage(Utils.translate("&cYou cannot /join a Race level"));
                                    }
                                }
                                else
                                {
                                    player.sendMessage(Utils.translate("&cYou cannot /join an Event level"));
                                }
                            }
                            else
                            {
                                player.sendMessage(Utils.translate("&cYou cannot /join a Rankup level"));
                            }
                        }
                        else
                        {
                            player.sendMessage(Utils.translate("&cYou cannot /join an Ascendance level"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + targetStats.getPlayerName() + " &cis not in a level"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + a[0] + " &cis not online"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cInvalid usage, do &4/join (playerName)"));
            }
        }
        return false;
    }
}
