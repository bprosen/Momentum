package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.data.modifiers.ModifierType;
import com.renatusnetwork.momentum.data.modifiers.ModifiersManager;
import com.renatusnetwork.momentum.data.modifiers.bonuses.Bonus;
import com.renatusnetwork.momentum.data.modifiers.boosters.Booster;
import com.renatusnetwork.momentum.data.modifiers.discounts.Discount;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collection;

public class ModifierCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender.hasPermission("momentum.admin")) {
            /*
                /modifier create (modifierName) (type) (modifierValue) (title)
                /modifier title (modifier) (title)
                /modifier add (player) (modifierName)
                /modifier remove (player) (modifierName)
                /modifier list (player)
                /modifier list
                /modifier load
             */
            ModifiersManager modifiersManager = Momentum.getModifiersManager();

            if (a.length >= 5 && a[0].equalsIgnoreCase("create")) {
                String modifierName = a[1];
                String modifierType = a[2].toUpperCase();
                String modifierValue = a[3];

                String[] split = Arrays.copyOfRange(a, 4, a.length);
                String title = String.join(" ", split);

                if (!modifiersManager.exists(modifierName)) {
                    ModifierType type = parseType(modifierType, sender);

                    if (type != null) {
                        float modifier = parseModifierValue(modifierValue, type, sender);

                        if (modifier > 0.00f) {
                            // create now
                            modifiersManager.create(modifierName, type, title, modifier);
                            sender.sendMessage(Utils.translate(
                                    "&7You have created the modifier &6" + modifierName + "&7(" + title + "&7) with type &6" + modifierType + "&7 and modifier of &6" + modifier
                            ));
                        } else {
                            sender.sendMessage(Utils.translate("&cYou cannot set a modifier of 0"));
                        }
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + modifierName + " &calready exists"));
                }
            } else if (a.length >= 3 && a[0].equalsIgnoreCase("title")) {
                Modifier modifier = modifiersManager.getModifier(a[1]);
                String[] split = Arrays.copyOfRange(a, 2, a.length);
                String title = String.join(" ", split);

                if (modifier != null) {
                    modifiersManager.updateTitle(modifier, title);
                    sender.sendMessage(Utils.translate("&7You have updated &6" + modifier.getName() + "&7's title to &6" + title));
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cdoes not exist"));
                }
            } else if (a.length == 3 && (a[0].equalsIgnoreCase("add") || a[0].equalsIgnoreCase("remove"))) {
                String playerName = a[1];
                String modifierName = a[2];
                PlayerStats playerStats = Momentum.getStatsManager().getByName(playerName);

                if (playerStats != null) {
                    Modifier modifier = Momentum.getModifiersManager().getModifier(modifierName);

                    if (modifier != null) {
                        if (a[0].equalsIgnoreCase("add")) {
                            if (!playerStats.hasModifier(modifier.getType())) {
                                Momentum.getStatsManager().addModifier(playerStats, modifier);
                                sender.sendMessage(Utils.translate("&7You have added a &c" + modifier.getTitle() + " &cModifier &7to &c" + playerStats.getName()));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + playerStats.getName() + " &calready has a &4" + modifier.getType() + " &cactive!"));
                            }
                        } else {
                            // if they have it, we can remove
                            if (playerStats.hasModifierByName(modifier)) {
                                Momentum.getStatsManager().removeModifier(playerStats, modifier);
                                sender.sendMessage(Utils.translate("&7You have removed a &c" + modifier.getTitle() + " &cModifier &7from &c" + playerStats.getName()));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + playerStats.getName() + " &cdoes not have the modifier &4" + modifier.getName()));
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&4" + modifierName + " &cis not a modifier"));
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("list")) {
                sender.sendMessage(Utils.translate("&4List of Modifiers"));
                listModifiers(sender, Momentum.getModifiersManager().getModifiers());
            } else if (a.length == 2 && a[0].equalsIgnoreCase("list")) {
                String playerName = a[1];
                PlayerStats targetStats = Momentum.getStatsManager().getByName(playerName);

                if (targetStats != null) {
                    // list modifiers that the player has
                    sender.sendMessage(Utils.translate("&4List of &c" + targetStats.getName() + "&4's Modifiers"));
                    listModifiers(sender, targetStats.getModifiers());
                } else {
                    sender.sendMessage(Utils.translate("&4" + playerName + " &cis not online"));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                Momentum.getModifiersManager().load();
                sender.sendMessage(Utils.translate("&4Modifiers has been reloaded"));

                Collection<PlayerStats> players = Momentum.getStatsManager().getOnlinePlayers();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // thread safety
                        synchronized (players) {
                            // load all players modifiers
                            for (PlayerStats playerStats : players) {
                                StatsDB.loadModifiers(playerStats);
                            }
                        }
                    }
                }.runTaskAsynchronously(Momentum.getPlugin());
            } else {
                sendHelp(sender);
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou cannot do this"));
        }
        return false;
    }

    private void listModifiers(CommandSender sender, Collection<Modifier> modifiers) {

        for (Modifier modifier : modifiers) {
            String modifierString = "&c" + modifier.getName() + " &7(" + modifier.getType() + " ";

            if (modifier instanceof Booster) {
                Booster booster = (Booster) modifier;
                modifierString += "Multiplier " + booster.getMultiplier() + ")";
            } else if (modifier instanceof Discount) {
                Discount discount = (Discount) modifier;
                modifierString += "Discount " + discount.getDiscount() + ")";
            } else if (modifier instanceof Bonus) {
                Bonus bonus = (Bonus) modifier;
                modifierString += "Bonus " + bonus.getBonus() + ")";
            }

            // send with info about modifier
            sender.sendMessage(Utils.translate(" &c" + modifierString));
        }
    }

    private ModifierType parseType(String argument, CommandSender sender) {
        try {
            return ModifierType.valueOf(argument);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Utils.translate("&4" + argument + " &cis not a valid type, select from"));
            sender.sendMessage(Utils.translate("&7" + Arrays.toString(ModifierType.values())));
            return null;
        }
    }

    private float parseModifierValue(String modifierValue, ModifierType type, CommandSender sender) {
        float modifier;

        // if it is a bonus, we want to make sure they typed an integer
        if (type == ModifierType.RECORD_BONUS) {
            if (Utils.isInteger(modifierValue)) {
                modifier = Integer.parseInt(modifierValue);
            } else {
                // return if failed
                sender.sendMessage(Utils.translate("&4" + modifierValue + " &cis not an integer"));
                return -1.00f;
            }
        } else {
            if (Utils.isFloat(modifierValue)) {
                modifier = Float.parseFloat(modifierValue);
            } else {
                sender.sendMessage(Utils.translate("&4" + modifierValue + " &cis not a decimal number"));
                return -1.00f;
            }
        }

        return modifier;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&4&lModifiers Help"));
        sender.sendMessage(Utils.translate(" &c/modifier create (modifierName) (type) (modifierValue) (title)  &7Creates a modifier - title can have spaces"));
        sender.sendMessage(Utils.translate(" &c/modifier title (modifierName) (title)  &7Changes a modifier's table - can have spaces"));
        sender.sendMessage(Utils.translate(" &c/modifier add (playerName) (modifierName)  &7Add a modifier to a player"));
        sender.sendMessage(Utils.translate(" &c/modifier remove (playerName) (modifierName)  &7Remove a modifier to a player"));
        sender.sendMessage(Utils.translate(" &c/modifier list (player)  &7Lists the modifiers a player has"));
        sender.sendMessage(Utils.translate(" &c/modifier list  &7Lists modifiers"));
        sender.sendMessage(Utils.translate(" &c/modifier load  &7Reloads from config"));
    }
}
