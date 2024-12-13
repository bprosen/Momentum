package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.bank.BankDB;
import com.renatusnetwork.momentum.data.bank.items.BankItemType;
import com.renatusnetwork.momentum.data.modifiers.Modifier;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class BankCMD implements CommandExecutor {
    // /bank reset
    // /bank item create (name)
    // /bank item title (title)
    // /bank item description (description)
    // /bank item type (type)
    // /bank item modifier (modifier)
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (sender.isOp()) {
            if (a.length == 1 && a[0].equalsIgnoreCase("reset")) {
                if (!Momentum.getBankManager().resetBank()) {
                    sender.sendMessage(Utils.translate("&cSomething went wrong trying to reset the bank"));
                }
            } else {
                if (a.length > 1 && a[0].equalsIgnoreCase("item")) {
                    String itemCMDType = a[1];

                    if (a.length == 3 && itemCMDType.equalsIgnoreCase("create")) {
                        String itemName = a[2].toLowerCase();

                        BankDB.getItem(itemName).thenAccept(result -> {
                            if (result.isEmpty()) {
                                BankDB.insertItem(itemName);
                                sender.sendMessage(Utils.translate("&7You have created the item &c" + itemName));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + itemName + " &calready exists"));
                            }
                        });
                    } else if (a.length > 3 && itemCMDType.equalsIgnoreCase("title")) {
                        String itemName = a[2].toLowerCase();
                        String[] split = Arrays.copyOfRange(a, 3, a.length);
                        String title = String.join(" ", split);

                        BankDB.getItem(itemName).thenAccept(result -> {
                            if (!result.isEmpty()) {
                                BankDB.updateTitle(itemName, title);
                                sender.sendMessage(Utils.translate("&7You have updated &c" + itemName + "&7's title to &c" + title));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + itemName + " &cdoes not exist"));
                            }
                        });
                    } else if (a.length > 3 && itemCMDType.equalsIgnoreCase("description")) {
                        String itemName = a[2].toLowerCase();
                        String[] split = Arrays.copyOfRange(a, 3, a.length);
                        String description = String.join(" ", split);

                        BankDB.getItem(itemName).thenAccept(result -> {
                            if (!result.isEmpty()) {
                                BankDB.updateDescription(itemName, description);
                                sender.sendMessage(Utils.translate("&7You have updated &c" + itemName + "&7's description to:"));
                                sender.sendMessage(Utils.translate(description));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + itemName + " &cdoes not exist"));
                            }
                        });
                    } else if (a.length == 4 && itemCMDType.equalsIgnoreCase("type")) {
                        String itemName = a[2].toLowerCase();
                        String bankItemType = a[3].toUpperCase();

                        if (!Momentum.getBankManager().isType(bankItemType)) {
                            String typesString = Arrays.toString(BankItemType.values());

                            sender.sendMessage(Utils.translate(
                                    "&4" + bankItemType + " &cis not a valid bank item type, the types are: &4" + typesString.substring(1, typesString.length() - 1)
                            ));
                            return false;
                        }

                        BankItemType type = BankItemType.valueOf(bankItemType);

                        BankDB.getItem(itemName).thenAccept(result -> {
                            if (!result.isEmpty()) {
                                BankDB.updateType(itemName, type);
                                sender.sendMessage(Utils.translate("&7You have updated &c" + itemName + "&7's type to &4" + type.name()));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + itemName + " &cdoes not exist"));
                            }
                        });
                    } else if (a.length == 4 && itemCMDType.equalsIgnoreCase("modifier")) {
                        String itemName = a[2].toLowerCase();
                        String modifierName = a[3];
                        Modifier modifier = Momentum.getModifiersManager().getModifier(modifierName);

                        if (modifier == null) {
                            sender.sendMessage(Utils.translate("&4" + modifierName + " &cis not a modifier"));
                            return false;
                        }

                        BankDB.getItem(itemName).thenAccept(result -> {
                            if (!result.isEmpty()) {
                                BankDB.updateModifier(itemName, modifier.getName());
                                sender.sendMessage(Utils.translate("&7You have updated &c" + itemName + "&7's modifier to " + modifier.getTitle()));
                            } else {
                                sender.sendMessage(Utils.translate("&4" + itemName + " &cdoes not exist"));
                            }
                        });
                    } else {
                        sendHelp(sender);
                    }
                } else {
                    sendHelp(sender);
                }
            }
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&4&lBank Help"));
        sender.sendMessage(Utils.translate("&c/bank reset  &7Resets the bank week"));
        sender.sendMessage(Utils.translate("&c/bank item create (name)  &7Creates a new bank item"));
        sender.sendMessage(Utils.translate("&c/bank item title (name) (title)  &7Sets the bank item's title (can be multiple words)"));
        sender.sendMessage(Utils.translate("&c/bank item description (name) (description)  &7Sets the bank item's description (can be multiple words)"));
        sender.sendMessage(Utils.translate("&c/bank item type (name) (type)  &7Sets the bank item's type"));
        sender.sendMessage(Utils.translate("&c/bank item modifier (name) (modifier)  &7Sets the bank item's modifier"));
        sender.sendMessage(Utils.translate("&c/bank help  &7Displays this page"));
    }
}
