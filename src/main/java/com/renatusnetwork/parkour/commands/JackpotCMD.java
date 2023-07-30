package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.bank.BankManager;
import com.renatusnetwork.parkour.data.bank.types.Jackpot;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.menus.MenuItemAction;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JackpotCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            BankManager bankManager = Parkour.getBankManager();

            if (a.length == 1 && a[0].equalsIgnoreCase("play"))
            {
                if (bankManager.isJackpotRunning())
                {
                    Jackpot jackpot = Parkour.getBankManager().getJackpot();

                    if (!jackpot.hasCompleted(player.getName()))
                        // tp to level
                        MenuItemAction.performLevelTeleport(Parkour.getStatsManager().get(player), player, jackpot.getLevel());
                }
                else
                {
                    player.sendMessage(Utils.translate("&cThe jackpot is not running currently"));
                }
            }
            else if (player.hasPermission("rn-parkour.admin"))
            {
                if (a.length == 1 && a[0].equalsIgnoreCase("start"))
                {
                    if (!bankManager.isJackpotRunning())
                        bankManager.startJackpot();
                    else
                        player.sendMessage(Utils.translate("&cThere is already a jackpot running, do &4/jackpot end &cto stop it!"));
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("end"))
                {
                    if (bankManager.isJackpotRunning())
                        bankManager.endJackpot();
                    else
                        player.sendMessage(Utils.translate("&cThere is no jackpot running, do &4/jackpot start &cto start one!"));
                }
                else if (a.length == 3 && a[0].equalsIgnoreCase("choose"))
                {
                    String levelName = a[1];
                    String bonus = a[2];

                    Level level = Parkour.getLevelManager().get(levelName);

                    if (level != null)
                    {
                        if (Utils.isInteger(bonus))
                        {
                            if (!level.isFeaturedLevel() && !level.isAscendanceLevel() && !level.isRankUpLevel())
                            {
                                int bonusAmount = Integer.parseInt(bonus);

                                if (!bankManager.isJackpotRunning())
                                    bankManager.chooseJackpot(level, bonusAmount);
                                else
                                    player.sendMessage(Utils.translate("&cThere is already a jackpot running, do &4/jackpot end &cto stop it!"));
                            }
                            else
                            {
                                player.sendMessage(Utils.translate("&cThe level cannot be the featured, in ascendance or a rankup..."));
                            }
                        }
                        else
                        {
                            player.sendMessage(Utils.translate("&4" + bonus + " &cis not a valid integer"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + levelName + " &cis not a valid level"));
                    }
                }
                else
                {
                    sendHelp(player);
                }
            }
            else
            {
                sendHelp(player);
            }
        }
        return false;
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&2/jackpot play  &7Enters the currently running jackpot level"));

        if (player.hasPermission("rn-parkour.admin"))
        {
            player.sendMessage(Utils.translate("&2/jackpot start  &7Automatically chooses and starts a jackpot based off of bank's balance"));
            player.sendMessage(Utils.translate("&2/jackpot end  &7Ends any currently running jackpot"));
            player.sendMessage(Utils.translate("&2/jackpot choose <level name> <bonus amount>  &7Chooses a jackpot from level and bonus amount"));
        }

        player.sendMessage(Utils.translate("&2/jackpot help  &7Displays this screen"));
    }
}
