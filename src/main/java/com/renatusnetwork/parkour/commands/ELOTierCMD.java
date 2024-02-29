package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.elo.ELOTier;
import com.renatusnetwork.parkour.data.elo.ELOTiersManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ELOTierCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        /*
            /elotier create (name)
            /elotier title (name) (title)
            /elotier requiredelo (name) (elo)
            /elotier next (name) (nextName)
            /elotier set (player) (tier)
         */
        if (sender.hasPermission("rn-parkour.admin"))
        {
            ELOTiersManager eloTiersManager = Parkour.getELOTiersManager();

            if (a.length == 2 && a[0].equalsIgnoreCase("create"))
            {
                String name = a[1].toLowerCase();
                ELOTier tier = Parkour.getELOTiersManager().get(name);

                if (tier == null)
                {
                    eloTiersManager.create(name);
                    sender.sendMessage(Utils.translate("&7You have created the ELO tier &c" + name));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + name + " &calready exists"));
            }
            else if (a.length == 2 && a[0].equalsIgnoreCase("loadxpbar"))
            {
                PlayerStats playerStats = Parkour.getStatsManager().getByName(a[1]);
                if (playerStats != null && playerStats.isLoaded())
                {
                    playerStats.loadELOToXPBar();
                    sender.sendMessage(Utils.translate("&7You have loaded &c" + playerStats.getDisplayName() + "&7's XP bar based on their ELO and ELO tier"));
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[1] + "&c is not online or loaded"));
            }
            else if (a.length >= 3 && a[0].equalsIgnoreCase("title"))
            {
                String name = a[1].toLowerCase();
                ELOTier tier = get(sender, name);

                if (tier != null)
                {
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String newTitle = String.join(" ", split);

                    eloTiersManager.updateTitle(tier, newTitle);
                    sender.sendMessage(Utils.translate("&7You have set &c" + name + " &7title to &c" + tier.getTitle()));
                }
            }
            else if (a.length == 3)
            {
                if (a[0].equalsIgnoreCase("requiredelo"))
                {
                    String name = a[1].toLowerCase();
                    ELOTier tier = get(sender, name);

                    if (tier != null)
                    {
                        if (Utils.isInteger(a[2]))
                        {
                            int requiredELO = Integer.parseInt(a[2]);

                            eloTiersManager.updateRequiredELO(tier, requiredELO);
                            sender.sendMessage(Utils.translate("&7You have set &c" + name + " &7required ELO to &c" + Utils.formatNumber(tier.getRequiredELO())));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an integer"));
                    }
                }
                else if (a[0].equalsIgnoreCase("previous") || a[0].equalsIgnoreCase("next"))
                {
                    String name = a[1].toLowerCase();
                    ELOTier tier = get(sender, name);

                    if (tier != null)
                    {
                        String next = a[2];
                        ELOTier otherTier = get(sender, next);

                        if (otherTier != null)
                        {
                            if (a[0].equalsIgnoreCase("previous"))
                            {
                                eloTiersManager.updatePreviousELOTier(tier, next);
                                sender.sendMessage(Utils.translate("&7You have set &c" + name + " &7previous tier to &c" + next));
                            }
                            else
                            {
                                eloTiersManager.updateNextELOTier(tier, next);
                                sender.sendMessage(Utils.translate("&7You have set &c" + name + " &7next tier to &c" + next));
                            }
                        }
                    }
                }
                else if (a[0].equalsIgnoreCase("set"))
                {
                    PlayerStats playerStats = Parkour.getStatsManager().getByName(a[1]);
                    if (playerStats != null && playerStats.isLoaded())
                    {
                        ELOTier tier = get(sender, a[2]);

                        if (tier != null)
                        {
                            Parkour.getStatsManager().updateELOTier(playerStats, tier);
                            sender.sendMessage(Utils.translate("&7You have set &c" + playerStats.getName() + "&7's ELO tier to &a" + tier.getTitle()));
                        }
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + a[1] + "&c is not online or loaded"));
                }
                else
                    sendHelp(sender);
            }
            else
                sendHelp(sender);
        }
        return false;
    }

    private ELOTier get(CommandSender sender, String name)
    {
        ELOTier eloTier = Parkour.getELOTiersManager().get(name);

        if (eloTier == null)
            sender.sendMessage(Utils.translate("&4" + name + " &cis not a ELO tier"));

        return eloTier;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&2&lELO Tier Help"));
        sender.sendMessage(Utils.translate("&a/elotier create (name)  &7Creates an ELO tier"));
        sender.sendMessage(Utils.translate("&a/elotier title (name) (title)  &7Sets a tier's title"));
        sender.sendMessage(Utils.translate("&a/elotier requiredelo (name) (requiredELO)  &7Sets a tier's required ELO"));
        sender.sendMessage(Utils.translate("&a/elotier next (name) (nextTier)  &7Sets a tier's next name"));
        sender.sendMessage(Utils.translate("&a/elotier previous (name) (previousTier)  &7Sets a tier's previous name"));
        sender.sendMessage(Utils.translate("&a/elotier set (player) (name)  &7Set a player's tier"));
        sender.sendMessage(Utils.translate("&a/elotier loadxpbar (player)  &7Recalculates and loads new xp bar on ELO/ELO tier"));
        sender.sendMessage(Utils.translate("&a/elotier help  &7Shows this page"));
    }
}
