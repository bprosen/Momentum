package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.modifiers.Modifier;
import com.renatusnetwork.parkour.data.modifiers.ModifiersDB;
import com.renatusnetwork.parkour.data.modifiers.boosters.Booster;
import com.renatusnetwork.parkour.data.modifiers.discounts.Discount;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;

public class ModifierCMD implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a)
    {
        if (!(sender instanceof ConsoleCommandSender))
        {
            Player player = (Player) sender;

            if (player.hasPermission("rn-parkour.admin"))
            {
                /*
                    /modifier add (player) (modifierName)
                    /modifier remove (player) (modifierName)
                    /modifier list (player)
                    /modifier list
                    /modifier load
                 */
                if (a.length == 3 && (a[0].equalsIgnoreCase("add") || a[0].equalsIgnoreCase("remove")))
                {
                    String playerName = a[1];
                    String modifierName = a[2];
                    Player target = Bukkit.getPlayer(playerName);

                    if (target != null)
                    {
                        Modifier modifier = Parkour.getModifiersManager().getModifier(modifierName);

                        if (modifier != null)
                        {
                            PlayerStats playerStats = Parkour.getStatsManager().get(player);

                            if (a[0].equalsIgnoreCase("add"))
                            {
                                if (!playerStats.hasModifier(modifier.getType()))
                                {
                                    Parkour.getModifiersManager().addModifier(playerStats, modifier);
                                    player.sendMessage(Utils.translate("&7You have added a &c" + modifier.getDisplayName() + " &cModifier &7to &c" + target.getName()));
                                }
                                else
                                    player.sendMessage(Utils.translate("&4" + target.getName() + " &calready has a &4" + modifier.getType() + " &cactive!"));
                            }
                            else
                            {
                                // if they have it, we can remove
                                if (playerStats.hasModifierByName(modifier))
                                {
                                    Parkour.getModifiersManager().removeModifier(playerStats, modifier);
                                    player.sendMessage(Utils.translate("&7You have removed a &c" + modifier.getDisplayName() + " &cModifier &7from &c" + target.getName()));
                                }
                                else
                                    player.sendMessage(Utils.translate("&4" + target.getName() + " &cdoes not have the modifier &4" + modifier.getName()));
                            }
                        }
                        else
                        {
                            player.sendMessage(Utils.translate("&4" + modifierName + " &cis not a modifier"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                    }
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("list"))
                {
                    player.sendMessage(Utils.translate("&4List of Modifiers"));
                    listModifiers(player, Parkour.getModifiersManager().getModifiers());
                }
                else if (a.length == 2 && a[0].equalsIgnoreCase("list"))
                {
                    String playerName = a[1];
                    Player target = Bukkit.getPlayer(playerName);

                    if (target != null)
                    {
                        // list modifiers that the player has
                        player.sendMessage(Utils.translate("&4List of &c" + target.getName() + "&4's Modifiers"));
                        listModifiers(player, Parkour.getStatsManager().get(player).getModifiers());
                    }
                    else
                    {
                        player.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                    }
                }
                else if (a.length == 1 && a[0].equalsIgnoreCase("load"))
                {
                    Parkour.getConfigManager().load("modifiers");
                    Parkour.getModifiersManager().load();
                    player.sendMessage(Utils.translate("&4modifiers.yml &chas been reloaded"));

                    HashMap<String, PlayerStats> players = Parkour.getStatsManager().getPlayerStats();

                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            // thread safety
                            synchronized (players)
                            {
                                // load all players modifiers
                                for (PlayerStats playerStats : players.values())
                                    ModifiersDB.loadModifiers(playerStats);
                            }
                        }
                    }.runTaskAsynchronously(Parkour.getPlugin());
                }
                else
                {
                    sendHelp(player);
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou cannot do this"));
            }
        }
        return false;
    }

    private void listModifiers(Player player, Collection<Modifier> modifiers)
    {

        for (Modifier modifier : modifiers)
        {
            String modifierString = "&c" + modifier.getName() + " &7(" + modifier.getType() + " ";

            if (modifier instanceof Booster)
            {
                Booster booster = (Booster) modifier;
                modifierString += "Multiplier " + booster.getMultiplier() + ")";
            }
            else if (modifier instanceof Discount)
            {
                Discount discount = (Discount) modifier;
                modifierString += "Discount " + discount.getDiscount() + ")";
            }

            // send with info about modifier
            player.sendMessage(Utils.translate(" &c" + modifierString));
        }
    }

    private void sendHelp(Player player)
    {
        player.sendMessage(Utils.translate("&4&lModifiers Help"));
        player.sendMessage(Utils.translate(" &c/modifier add (playerName) (modifierName)  &7Add a modifier to a player"));
        player.sendMessage(Utils.translate(" &c/modifier remove (playerName) (modifierName)  &7Remove a modifier to a player"));
        player.sendMessage(Utils.translate(" &c/modifier list (player)  &7Lists the modifiers a player has"));
        player.sendMessage(Utils.translate(" &c/modifier list  &7Lists modifiers"));
        player.sendMessage(Utils.translate(" &c/modifier load  &7Reloads from config"));
    }
}
