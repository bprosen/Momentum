package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.SettingsManager;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PracticeCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        StatsManager statsManager = Momentum.getStatsManager();
        PlayerStats playerStats = statsManager.get(player);

        if (a.length == 0)
        {
            if (validate(playerStats, player))
            {
                /*
                 check practice location first, if not null then reset or
                 then check if they are on ground then enable or cancel
                 */
                // case of /unprac (can only unprac with /unprac)
                if (label.equalsIgnoreCase("unprac"))
                {
                    if (playerStats.inPracticeMode())
                        statsManager.resetPracticeMode(playerStats, true);
                    else
                        player.sendMessage(Utils.translate("&cYou are not in practice mode"));
                }
                // in the case of /prac
                else if (playerStats.inPracticeMode())
                {
                    if (player.isOnGround())
                    {
                        playerStats.setPracticeCheckpoint(player.getLocation(), true);
                        player.sendMessage(Utils.translate("&7Practice checkpoint set"));
                    }
                    else
                        player.sendMessage(Utils.translate("&cYou cannot set practice checkpoint while in the air"));
                }
                else if (player.isOnGround())
                {
                    playerStats.setPracticeMode(player.getLocation());

                    SettingsManager settingsManager = Momentum.getSettingsManager();
                    Utils.addItemToHotbar(settingsManager.prac_item, player.getInventory(), settingsManager.prac_hotbar_slot);

                    player.sendMessage(Utils.translate("&7You have turned practice mode &aOn"));
                }
                else
                    player.sendMessage(Utils.translate("&cYou cannot enable practice mode while in the air"));
            }
        }
        else if ((label.equalsIgnoreCase("prac") || label.equalsIgnoreCase("practice")) && a.length == 1)
        {
            if (a[0].equalsIgnoreCase("go") || a[0].equalsIgnoreCase("tp"))
                Momentum.getCheckpointManager().teleportToPracticeCheckpoint(playerStats);
            else if (a[0].equalsIgnoreCase("history") || a[0].equalsIgnoreCase("hist"))
            {
                if (validate(playerStats, player))
                {
                    if (playerStats.inPracticeMode())
                        Momentum.getMenuManager().openInventory(playerStats, "practice_history", true);
                    else
                        player.sendMessage(Utils.translate("&cYou are not in practice mode"));
                }

            }
            else
                sendHelp(sender);
        }
        else
            sendHelp(sender);
        return false;
    }

    private boolean validate(PlayerStats playerStats, Player player)
    {
        if (playerStats == null || !playerStats.isLoaded())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
            return false;
        }

        if (playerStats.isSpectating())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in /spectator"));
            return false;
        }

        if (playerStats.isPreviewingLevel())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while previewing a level"));
            return false;
        }

        if (playerStats.isEventParticipant())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            return false;
        }

        if (playerStats.isInBlackMarket())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
            return false;
        }

        if (playerStats.isInInfinite())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
            return false;
        }

        if (playerStats.isAttemptingMastery())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in mastery"));
            return false;
        }

        if (playerStats.inLevel() && playerStats.getLevel().isDropper())
        {
            player.sendMessage(Utils.translate("&cPractice is disabled in &3&lDropper &clevels"));
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&6&lPractice Help"));
        sender.sendMessage(Utils.translate("&e/prac  &7Sets your practice location"));
        sender.sendMessage(Utils.translate("&e/unprac  &7Disables practice mode"));
        sender.sendMessage(Utils.translate("&e/prac go  &7Teleports to your practice location"));
        sender.sendMessage(Utils.translate("&e/prac history  &7Displays your history of locations to teleport back to"));
        sender.sendMessage(Utils.translate("&e/prac help  &7Displays this page"));
    }
}
