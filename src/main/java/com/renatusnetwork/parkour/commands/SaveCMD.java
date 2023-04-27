package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;

            if (a.length == 0)
            {
                PlayerStats playerStats = Parkour.getStatsManager().get(player);

                if (!playerStats.isInInfinitePK())
                {
                    if (playerStats.inLevel())
                    {
                        Level level = playerStats.getLevel();

                        if (!level.isAscendanceLevel())
                        {
                            if (!playerStats.isInTutorial())
                            {
                                if (!playerStats.isEventParticipant())
                                {
                                    if (playerStats.getPlayerToSpectate() == null)
                                    {
                                        if (playerStats.getPracticeLocation() == null)
                                        {
                                            if (!playerStats.inRace())
                                            {
                                                if (player.isOnGround())
                                                {
                                                    if (player.getLocation().clone().add(0, 1, 0).getBlock().getType() == Material.AIR)
                                                    {
                                                        // passed all checks then they can save!

                                                        // remove here
                                                        if (playerStats.hasSave(level.getName()))
                                                            Parkour.getSavesManager().removeSave(playerStats, level);

                                                        // add here
                                                        Parkour.getSavesManager().addSave(playerStats, player.getLocation(), level);

                                                        Utils.teleportToSpawn(playerStats); // tp to spawn

                                                        player.sendMessage(Utils.translate("&7You have saved your location on &c" + level.getFormattedTitle()));
                                                        player.sendMessage(Utils.translate("&aWhen you come back to &c" + level.getFormattedTitle() + "&a, you will teleport at your save"));
                                                    }
                                                    else
                                                    {
                                                        player.sendMessage(Utils.translate("&cYou cannot use /save when in a block"));
                                                    }
                                                }
                                                else
                                                {
                                                    player.sendMessage(Utils.translate("&cYou cannot save while in the air"));
                                                }
                                            }
                                            else
                                            {
                                                player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                                            }
                                        }
                                        else
                                        {
                                            player.sendMessage(Utils.translate("&cYou cannot do this while in /prac"));
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
                                    }
                                }
                                else
                                {
                                    player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                                }
                            }
                            else
                            {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial"));
                            }
                        }
                        else
                        {
                            player.sendMessage(Utils.translate("&cYou cannot do this while in ascendance"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&cYou are not in a level"));
                    }
                }
                else
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cInvalid args, use &4/save"));
            }
        }
        return false;
    }
}
