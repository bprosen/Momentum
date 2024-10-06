package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.elo.ELOTier;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.perks.Perk;
import com.renatusnetwork.momentum.data.perks.PerkManager;
import com.renatusnetwork.momentum.data.perks.PerksArmorType;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;

public class PerksCMD implements CommandExecutor {

    /*
        /perk load
        /perk title (perkName) (title)
        /perk price (perkName) (price)
        /perk requiredpermission (perkName) (permission)
        /perk infiniteblock (perkName) (material)
        /perk addrequiredlevel (perkName) (levelName)
        /perk removerequiredlevel (perkName) (levelName)
        /perk addarmor (perkName) (armorType) (material)
        /perk removearmor (perkName) (armorType)
        /perk armortitle (perkName) (armorType) (title)
        /perk armorglow (perkName) (armorType)
        /perk armormaterial (perkName) (armorType) (material)
        /perk armormaterialtype (perkName) (armorType) (materialType)
        /perk armorcolor (perkName) (armorType) (color)
        /perk masterylevels (perkName)
        /perk requiredelotier (perkName) (eloTier)
        /perks addcommand (perkName) (command)
        /perks removecommand (perkName) (command)
        /perks commands (perkName)
        /perk help
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        PerkManager perkManager = Momentum.getPerkManager();

        if (sender.isOp()) {
            if (a.length == 2 && a[0].equalsIgnoreCase("create")) {
                String perkName = a[1];
                // helper method
                Perk perk = perkManager.get(perkName);

                if (perk == null) {
                    perkManager.create(perkName);
                    sender.sendMessage(Utils.translate("&7You have created the perk &a" + perkName));
                } else {
                    sender.sendMessage(Utils.translate("&4" + perkName + " &cis not a perk"));
                }

            } else if (a.length == 2 && a[0].equalsIgnoreCase("remove")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    perkManager.remove(perk);
                    sender.sendMessage(Utils.translate("&7You have removed the perk &a" + perkName));
                }
            } else if (a.length == 1 && a[0].equalsIgnoreCase("load")) {
                perkManager.load();
                sender.sendMessage(Utils.translate("&eLoaded &6" + perkManager.numPerks() + " &eperks into memory"));
            } else if (a.length >= 3 && a[0].equalsIgnoreCase("title")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // title can be multiple words
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String title = String.join(" ", split);

                    perkManager.updateTitle(perk, title);
                    sender.sendMessage(Utils.translate("&7You have updated &c" + perkName + " &7title to &c" + perk.getTitle()));
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("price")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // can only set the price if it is an int...
                    if (Utils.isInteger(a[2])) {
                        int price = Integer.parseInt(a[2]);

                        perkManager.updatePrice(perk, price);
                        sender.sendMessage(Utils.translate("&7You have updated &c" + perk.getTitle() + " &7price to &c" + price));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + a[2] + "&c is not a valid integer"));
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("requiredpermission")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    String requiredPermission = a[2].toLowerCase(); // force lowercase standard2

                    perkManager.updateRequiredPermission(perk, requiredPermission);
                    sender.sendMessage(Utils.translate("&7You have updated &c" + perk.getTitle() + " &7required permission to &c" + requiredPermission));
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("infiniteblock")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    Material infiniteBlock = getMaterialType(a[2].toUpperCase(), sender);

                    if (infiniteBlock != null) {
                        perkManager.updateInfiniteBlock(perk, infiniteBlock);
                        sender.sendMessage(Utils.translate("&7You have updated &c" + perk.getTitle() + " &7infinite block to &c" + infiniteBlock));
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("addrequiredlevel")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    String requiredLevel = a[2].toLowerCase();
                    Level level = Momentum.getLevelManager().get(requiredLevel);

                    if (level != null) {
                        // we can only add if it doesn't exist
                        if (!perk.alreadyRequiresLevel(level)) {
                            perkManager.addRequiredLevel(perk, level);
                            sender.sendMessage(Utils.translate("&7You have added &c" + level.getTitle() + "&7 to &c" + perk.getTitle() + "&7's required levels"));
                        } else {
                            sender.sendMessage(Utils.translate("&c" + level.getTitle() + "&7 is already a required level for &c" + perk.getTitle()));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&c" + requiredLevel + " is not a valid level"));
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("removerequiredlevel")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    String requiredLevel = a[2].toLowerCase();
                    Level level = Momentum.getLevelManager().get(requiredLevel);

                    if (level != null) {
                        // we can only remove if it exists
                        if (perk.alreadyRequiresLevel(level)) {
                            perkManager.removeRequiredLevel(perk, level);
                            sender.sendMessage(Utils.translate("&7You have remove &c" + level.getTitle() + "&7 from &c" + perk.getTitle() + "&7's required levels"));
                        } else {
                            sender.sendMessage(Utils.translate("&c" + level.getTitle() + "&7 is not a required level for &c" + perk.getTitle()));
                        }
                    } else {
                        sender.sendMessage(Utils.translate("&c" + requiredLevel + " is not a valid level"));
                    }
                }
            } else if (a.length == 4 && a[0].equalsIgnoreCase("addarmor")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType armorType = getArmorType(a[2].toUpperCase(), sender);

                    if (armorType != null) {
                        // helper method
                        Material materialType = getMaterialType(a[3].toUpperCase(), sender);

                        if (materialType != null) {
                            // can only add if it doesn't exists
                            if (!perk.hasArmorItem(armorType)) {
                                perkManager.addArmorPiece(perk, armorType, materialType);
                                sender.sendMessage(Utils.translate(
                                        "&7You have added armor piece of type &6" + armorType.name() +
                                        "&7 with material &6" + materialType.name() + " &7to &6" + perk.getTitle()
                                ));
                            } else {
                                sender.sendMessage(Utils.translate("&c" + perk.getTitle() + " &calready has an armor piece on &4" + armorType));
                            }
                        }
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("removearmor")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType type = getArmorType(a[2].toUpperCase(), sender);

                    if (type != null) {
                        // can only update if it exists
                        if (perk.hasArmorItem(type)) {
                            perkManager.removeArmorPiece(perk, type);
                            sender.sendMessage(Utils.translate(
                                    "&7You have remove armor piece of type &6" + type.name() + "&7to &6" + perk.getTitle()
                            ));
                        } else {
                            sender.sendMessage(Utils.translate("&c" + perk.getTitle() + " &cdoes not have an armor piece on &4" + type.name()));
                        }
                    }
                }
            } else if (a.length >= 4 && a[0].equalsIgnoreCase("armortitle")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType type = getArmorType(a[2].toUpperCase(), sender);

                    if (type != null) {
                        // titles can be multiple words
                        String[] split = Arrays.copyOfRange(a, 3, a.length);
                        String title = String.join(" ", split);

                        // can only update if it exists
                        if (perk.hasArmorItem(type)) {
                            perkManager.updateArmorTitle(perk, type, title);
                            sender.sendMessage(Utils.translate(
                                    "&7You have updated &c" + perkName + "&7's armor piece &c" + type.name() + "&7's title to &c" + title
                            ));
                        } else {
                            sender.sendMessage(Utils.translate("&4" + perk.getTitle() + " &cdoes not have an armor item on piece &4" + type.name()));
                        }
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("armorglow")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType type = getArmorType(a[2].toUpperCase(), sender);

                    if (type != null) {
                        // we can only update it if it exists
                        if (perk.hasArmorItem(type)) {
                            boolean glow = perkManager.updateArmorGlow(perk, type); // get the result of the glow toggle for printing
                            sender.sendMessage(Utils.translate(
                                    "&7You have updated &c" + perkName + "&7's armor piece &c" + type.name() + "&7's glow to &c" + glow
                            ));
                        } else {
                            sender.sendMessage(Utils.translate("&4" + perk.getTitle() + " &cdoes not have an armor item on piece &4" + type.name()));
                        }
                    }
                }
            } else if (a.length == 4 && a[0].equalsIgnoreCase("armormaterial")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType armorType = getArmorType(a[2].toUpperCase(), sender);

                    if (armorType != null) {
                        // helper method
                        Material materialType = getMaterialType(a[3].toUpperCase(), sender);

                        if (materialType != null) {
                            // we can only update it if it exists
                            if (perk.hasArmorItem(armorType)) {
                                perkManager.updateArmorMaterial(perk, armorType, materialType);
                                sender.sendMessage(Utils.translate(
                                        "&7You have updated armor piece of type &6" + armorType.name() +
                                        "&7 with new material &6" + materialType.name() + " &7to &6" + perk.getTitle()
                                ));
                            } else {
                                sender.sendMessage(Utils.translate("&c" + perk.getTitle() + " &cdoes not have an armor piece on &4" + armorType));
                            }
                        }
                    }
                }
            } else if (a.length == 4 && a[0].equalsIgnoreCase("armormaterialtype")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType armorType = getArmorType(a[2].toUpperCase(), sender);

                    if (armorType != null) {
                        String materialType = a[3];
                        // material types in 1.12 are shorts that can be casted from ints
                        if (Utils.isInteger(materialType)) {
                            int material = Integer.parseInt(materialType);

                            if (perk.hasArmorItem(armorType)) {
                                perkManager.updateArmorMaterialType(perk, armorType, material);
                                sender.sendMessage(Utils.translate(
                                        "&7You have updated armor piece of type &6" + armorType.name() +
                                        "&7 with new type value &6" + material + " &7to &6" + perk.getTitle()
                                ));
                            } else {
                                sender.sendMessage(Utils.translate("&c" + perk.getTitle() + " &cdoes not have an armor piece on &4" + armorType));
                            }
                        }
                    }
                }
            } else if (a.length == 4 && a[0].equalsIgnoreCase("armorcolor")) {
                String perkName = a[1];
                // helper method
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // helper method
                    PerksArmorType armorType = getArmorType(a[2].toUpperCase(), sender);

                    if (armorType != null) {
                        String colorString = a[3].toUpperCase();
                        Color colorType = Utils.getColorFromString(colorString);
                        // material types in 1.12 are shorts that can be casted from ints
                        if (colorType != null) {
                            if (perk.hasArmorItem(armorType)) {
                                perkManager.updateArmorColor(perk, armorType, colorType, colorString);
                                sender.sendMessage(Utils.translate(
                                        "&7You have updated armor color of type &6" + armorType.name() +
                                        "&7 with new color &6" + colorString + " &7to &6" + perk.getTitle()
                                ));
                            } else {
                                sender.sendMessage(Utils.translate("&c" + perk.getTitle() + " &cdoes not have an armor piece on &4" + armorType));
                            }
                        }
                    }
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("masterylevels")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    perkManager.updateRequiresMasteryLevels(perk);
                    sender.sendMessage(Utils.translate("&7You have toggled &c" + perk.getTitle() + "&7 requiring mastery level completions to &a" + perk.requiresMasteryLevels()));
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("requiredelotier")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    ELOTier eloTier = Momentum.getELOTiersManager().get(a[2]);
                    if (eloTier != null) {
                        perkManager.updateRequiredELOTier(perk, eloTier);
                        sender.sendMessage(Utils.translate("&7You have set &c" + perk.getTitle() + "&7 required ELO tier to &a" + perk.getRequiredELOTier().getTitle()));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + a[2] + " &cis not an ELO tier"));
                    }
                }
            } else if (a.length >= 3 && a[0].equalsIgnoreCase("addcommand")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // title can be multiple words
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String command = String.join(" ", split);

                    if (!perk.hasCommand(command)) {
                        perkManager.addCommand(perk, command);
                        sender.sendMessage(Utils.translate("&2You have added the command to perk &a" + perk.getTitle()));
                        sender.sendMessage(Utils.translate("&7" + command));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + perk.getTitle()) + " &calready has the command &7" + command);
                    }
                }
            } else if (a.length >= 3 && a[0].equalsIgnoreCase("removecommand")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    // title can be multiple words
                    String[] split = Arrays.copyOfRange(a, 2, a.length);
                    String command = String.join(" ", split);

                    if (perk.hasCommand(command)) {
                        perkManager.removeCommand(perk, command);
                        sender.sendMessage(Utils.translate("&2You have remove the command to perk &a" + perk.getTitle()));
                        sender.sendMessage(Utils.translate("&7" + command));
                    } else {
                        sender.sendMessage(Utils.translate("&4" + perk.getTitle()) + " &cdoes not have the command &7" + command);
                    }
                }
            } else if (a.length == 2 && a[0].equalsIgnoreCase("commands")) {
                String perkName = a[1];
                Perk perk = getPerk(perkName, sender);

                if (perk != null) {
                    HashSet<String> commands = perk.getCommands();

                    sender.sendMessage(Utils.translate("&6" + perk.getTitle() + "&e's Commands"));

                    if (commands.isEmpty()) {
                        sender.sendMessage(Utils.translate("&7None"));
                    } else {
                        for (String command : commands) {
                            sender.sendMessage(Utils.translate("&7" + command));
                        }
                    }
                }
            } else if (a.length == 3 && a[0].equalsIgnoreCase("setperk")) {
                PlayerStats playerStats = Momentum.getStatsManager().getByName(a[1]);

                if (playerStats != null) {
                    String perkName = a[2];
                    Perk perk = getPerk(perkName, sender);

                    if (perk != null) {
                        // cannot be set if they do not have access
                        if (perk.hasAccess(playerStats)) {
                            perkManager.setPerk(perk, playerStats);
                            sender.sendMessage(Utils.translate(
                                    "&7You have given the perk &c" + perk.getTitle() + " &7to &c" + playerStats.getName()
                            ));
                        } else {
                            sender.sendMessage(Utils.translate("&4" + playerStats.getName() + " &cdoes not have perk &4" + perkName));
                        }
                    }
                } else {
                    sender.sendMessage(Utils.translate("&4" + a[1] + " &cis not online"));
                }
            } else {
                sendHelp(sender);
            }
        } else {
            sender.sendMessage(Utils.translate("&cYou do not have permission to use this command"));
        }

        return true;
    }

    private PerksArmorType getArmorType(String armorType, CommandSender sender) {
        // helper function for getting the armor type and printing a universal message
        try {
            return PerksArmorType.valueOf(armorType);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Utils.translate(
                    "&4" + armorType + " &cis not a valid armor type, options: &4" +
                    Arrays.toString(PerksArmorType.values())
            ));
        }
        return null;
    }

    private Material getMaterialType(String materialType, CommandSender sender) {
        // helper function for getting the material and printing a universal message
        try {
            return Material.matchMaterial(materialType);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Utils.translate(
                    "&4" + materialType + " &cis not a valid material type"
            ));
        }
        return null;
    }

    private Perk getPerk(String perkName, CommandSender sender) {
        // helper function for getting the perk and printing a universal message
        Perk perk = Momentum.getPerkManager().get(perkName);

        if (perk == null) {
            sender.sendMessage(Utils.translate("&4" + perkName + " &cis not a perk"));
        }

        return perk;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&6&lPerks Help"));
        sender.sendMessage(Utils.translate("&e/perks create (perkName)  &7Creates a perk"));
        sender.sendMessage(Utils.translate("&e/perks remove (perkName)  &7Removes a perk"));
        sender.sendMessage(Utils.translate("&e/perks title (perkName) (title)  &7Sets perk title (can be multiple words)"));
        sender.sendMessage(Utils.translate("&e/perks price (perkName) (price)  &7Sets perk price"));
        sender.sendMessage(Utils.translate("&e/perks requiredpermission (perkName) (permission)  &7Sets perk's required permission"));
        sender.sendMessage(Utils.translate("&e/perks infiniteblock (perkName) (material)  &7Sets infinite block"));
        sender.sendMessage(Utils.translate("&e/perks addrequiredlevel (perkName) (levelName)  &7Adds a required level for that perk"));
        sender.sendMessage(Utils.translate("&e/perks removerequiredlevel (perkName) (levelName)  &7Removes a required level for that perk"));
        sender.sendMessage(Utils.translate("&e/perks addarmor (perkName) (armorPiece) (material)  &7Adds a armor piece for that type"));
        sender.sendMessage(Utils.translate("&e/perks removearmor (perkName) (armorPiece)  &7Removed a armor piece for that type"));
        sender.sendMessage(Utils.translate("&e/perks armortitle (perkName) (armorPiece) (title)  &7Updates title for an existing armor piece, can be multiple words"));
        sender.sendMessage(Utils.translate("&e/perks armorglow (perkName) (armorPiece)  &7Toggles glow on the armor piece"));
        sender.sendMessage(Utils.translate("&e/perks armormaterial (perkName) (armorPiece) (material)  &7Updates material on existing armor piece"));
        sender.sendMessage(Utils.translate("&e/perks armormaterialtype (perkName) (armorPiece) (type)  &7Updates material type on existing armor piece"));
        sender.sendMessage(Utils.translate("&e/perks armorcolor (perkName) (armorPiece) (color)  &7Updates color of that armor piece"));
        sender.sendMessage(Utils.translate("&e/perks setperk (IGN) (perkName)  &7Sets perk armor, hat or infinite block to player if they have it"));
        sender.sendMessage(Utils.translate("&e/perks masterylevels (perkName)  &7Toggles if the perk requires mastery completions instead of normal level completions"));
        sender.sendMessage(Utils.translate("&e/perks requiredelotier (perkName) (eloTier)  &7Sets the required ELO tier for the perk"));
        sender.sendMessage(Utils.translate("&e/perks addcommand (perkName) (command)  &7Adds a command to a perk to give out on setting (command can be multiple words)"));
        sender.sendMessage(Utils.translate("&e/perks removecommand (perkName) (command)  &7Removes a command to a perk to give out on setting (command can be multiple words)"));
        sender.sendMessage(Utils.translate("&e/perks commands (perkName)  &7Lists the commands for a perka"));
        sender.sendMessage(Utils.translate("&e/perks help  &7Displays this page"));
        sender.sendMessage(Utils.translate("&e/perks load  &7Loads from db"));
    }
}
