package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
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
            PlayerStats playerStats = Momentum.getStatsManager().get(player);

            if (playerStats == null || !playerStats.isLoaded())
            {
                player.sendMessage(Utils.translate("&cYou cannot do this while your stats are loading"));
                return false;
            }

            if (a.length == 0)
            {
                if (playerStats.isInInfinite())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
                    return false;
                }

                if (!playerStats.inLevel())
                {
                    player.sendMessage(Utils.translate("&cYou are not in a level"));
                    return false;
                }

                Level level = playerStats.getLevel();

                if (level.isAscendance())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in ascendance"));
                    return false;
                }

                if (playerStats.isInTutorial())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial"));
                    return false;
                }

                if (playerStats.isEventParticipant())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                    return false;
                }

                if (playerStats.isSpectating())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in spectator"));
                    return false;
                }

                if (playerStats.isPreviewingLevel())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while previewing a level"));
                    return false;
                }

                if (playerStats.inPracticeMode())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in /prac"));
                    return false;
                }

                if (playerStats.inRace())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                    return false;
                }

                if (playerStats.isAttemptingMastery())
                {
                    player.sendMessage(Utils.translate("&cYou cannot do this while attempting mastery"));
                    return false;
                }

                if (!player.isOnGround())
                {
                    player.sendMessage(Utils.translate("&cYou cannot save while in the air"));
                    return false;
                }

                if (player.getLocation().clone().add(0, 1, 0).getBlock().getType() != Material.AIR)
                {
                    player.sendMessage(Utils.translate("&cYou cannot use /save when in a block"));
                    return false;
                }

                // passed all checks then they can save!
                Momentum.getSavesManager().saveLevel(playerStats, level, player.getLocation());
                Momentum.getLocationManager().teleportToSpawn(playerStats, player); // tp to spawn

                player.sendMessage(Utils.translate("&7You have saved your location on &c" + level.getTitle()));
                player.sendMessage(Utils.translate("&aWhen you come back to &c" + level.getTitle() + "&a, you will teleport to your save"));
            }
            else if (a.length == 1 && a[0].equalsIgnoreCase("toggle"))
            {
                Momentum.getStatsManager().toggleAutoSave(playerStats);
                player.sendMessage(Utils.translate("&7You have turned auto save " + (playerStats.hasAutoSave() ? "&aOn" : "&cOff")));
            }
        }
        return false;
    }
}
