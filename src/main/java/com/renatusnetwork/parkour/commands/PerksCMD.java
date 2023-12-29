package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.perks.Perk;
import com.renatusnetwork.parkour.data.perks.PerkManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class PerksCMD implements CommandExecutor
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {

        PerkManager perkManager = Parkour.getPerkManager();

        if (sender.isOp())
        {
            if (a.length == 1 && a[0].equalsIgnoreCase("load"))
            {
                perkManager.load();
                sender.sendMessage(Utils.translate("&eLoaded &6" + perkManager.numPerks() + " &eperks into memory"));
            }
            else if (a.length >= 3 && a[0].equalsIgnoreCase("title"))
            {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null)
                {
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String title = String.join(" ", split);

                    perkManager.updateTitle(perk, title);
                    sender.sendMessage(Utils.translate("&7You have updated &c" + perkName + " &7title to &c" + perk.getFormattedTitle()));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("price"))
            {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null)
                {
                    if (Utils.isInteger(a[2]))
                    {
                        int price = Integer.parseInt(a[2]);

                        perkManager.updatePrice(perk, price);
                        sender.sendMessage(Utils.translate("&7You have updated &c" + perkName + " &7price to &c" + price));
                    }
                    else
                        sender.sendMessage(Utils.translate("&4" + a[2] + "&c is not a valid integer"));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("requiredpermission"))
            {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null)
                {
                    String requiredPermission = a[2].toLowerCase();

                    perkManager.updateRequiredPermission(perk, requiredPermission);
                    sender.sendMessage(Utils.translate("&7You have updated &c" + perkName + " &7required permission to &c" + requiredPermission));
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("infiniteblock"))
            {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null)
                {
                    String infiniteBlock = a[2].toUpperCase();
                    try
                    {
                        Material infiniteBlockMaterial = Material.matchMaterial(infiniteBlock);

                        perkManager.updateInfiniteBlock(perk, infiniteBlockMaterial);
                        sender.sendMessage(Utils.translate("&7You have updated &c" + perkName + " &7infinite block to &c" + infiniteBlock));
                    }
                    catch (IllegalArgumentException exception)
                    {
                        sender.sendMessage(Utils.translate("&4" + infiniteBlock + " &cis not a valid material"));
                    }
                }
            }
            else if (a.length == 3 && a[0].equalsIgnoreCase("setperk"))
            {
                PlayerStats playerStats = Parkour.getStatsManager().getByName(a[1]);

                if (playerStats != null)
                {
                    String perkName = a[2];
                    Perk perk = getPerk(perkName, sender);

                    if (perk != null)
                    {
                        if (!perk.hasAccessTo(playerStats))
                        {
                            perkManager.setPerk(perk, playerStats);
                            sender.sendMessage(Utils.translate(
                                    "&7You have given the perk &c" + perk.getFormattedTitle() + " &7to &c" + playerStats.getName()
                            ));
                        }
                        else
                            sender.sendMessage(Utils.translate("&4" + playerStats.getName() + " &cdoes not have perk &4" + perkName));
                    }
                }
                else
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
            }
            else
                sendHelp(sender);
        }
        else
            sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));

        return true;
    }

    private Perk getPerk(String perkName, CommandSender sender)
    {
        Perk perk = Parkour.getPerkManager().get(perkName);

        if (perk != null)
            sender.sendMessage(Utils.translate("&4" + perkName + " &cis not a perk"));

        return perk;
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&6&lPerks Help"));
        sender.sendMessage(Utils.translate("&e/perks title (perkName) (title)  &7Sets perk title (can be multiple words)"));
        sender.sendMessage(Utils.translate("&e/perks price (perkName) (price)  &7Sets perk price"));
        sender.sendMessage(Utils.translate("&e/perks requiredpermission (perkName) (permission)  &7Sets perk's required permission"));
        sender.sendMessage(Utils.translate("&e/perks infiniteblock (perkName) (material)  &7Sets infinite block"));
        sender.sendMessage(Utils.translate("&e/perks setperk (IGN) (perkName)  &7Sets perk armor, hat or infinite block to player if they have it"));
        sender.sendMessage(Utils.translate("&e/perks help  &7Displays this page"));
        sender.sendMessage(Utils.translate("&e/perks load  &7Loads from disk"));
    }
}
