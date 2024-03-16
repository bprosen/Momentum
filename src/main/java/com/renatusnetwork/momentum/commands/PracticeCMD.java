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
            if (playerStats == null || !playerStats.isLoaded())
            {
                player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
                return false;
            }

            if (playerStats.isSpectating())
            {
                player.sendMessage(Utils.translate("&cYou cannot enter practice mode while in /spectator"));
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

            /*
             check practice location first, if not null then reset or
             then check if they are on ground then enable or cancel
             */

            // case of /unprac
            if (label.equalsIgnoreCase("unprac"))
            {
                if (playerStats.inPracticeMode())
                    statsManager.resetPracticeMode(playerStats, true);
                else
                    player.sendMessage(Utils.translate("&cYou are not in practice mode"));
            }
            // in the case of /prac
            else if (playerStats.inPracticeMode())
                statsManager.resetPracticeMode(playerStats, true);
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
        else if (label.equalsIgnoreCase("prac") && a.length == 1 && (a[0].equalsIgnoreCase("go") || a[0].equalsIgnoreCase("tp")))
                Momentum.getCheckpointManager().teleportToPracticeCheckpoint(playerStats);
        return false;
    }
}
