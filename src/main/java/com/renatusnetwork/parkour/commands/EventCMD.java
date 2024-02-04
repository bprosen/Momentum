package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.events.types.*;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class EventCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        EventManager eventManager = Parkour.getEventManager();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        /*
            Player Section
         */

        // join event
        if (a.length == 1 && a[0].equalsIgnoreCase("join")) {

            // run through all the conditions that would not allow them to join
            if (eventManager.isEventRunning()) {
                if (!eventManager.isParticipant(player)) {
                    if (!playerStats.inPracticeMode()) {
                        if (!playerStats.isSpectating()) {
                            if (!playerStats.inRace()) {
                                if (!Parkour.getPlayerHiderManager().containsPlayer(player)) {
                                    if (!playerStats.isInInfinite()) {
                                        if (!eventManager.isEliminated(player)) {
                                            if (!playerStats.isInBlackMarket()) {
                                                if (!(eventManager.isRisingWaterEvent() && ((RisingWaterEvent) eventManager.getRunningEvent()).isStartCoveredInWater())) {
                                                    if (playerStats.inLevel() && playerStats.getLevel().isElytra())
                                                        Parkour.getStatsManager().toggleOffElytra(playerStats);

                                                    // remove sword item if they have it and the mode is pvp
                                                    if (eventManager.isPvPEvent())
                                                    {
                                                        ItemStack swordItem = Utils.getSwordIfExists(player);
                                                        ItemStack shieldItem = Utils.getShieldIfExists(player);

                                                        if (swordItem != null)
                                                            player.getInventory().removeItem(swordItem);
                                                        if (shieldItem != null)
                                                            player.getInventory().removeItem(shieldItem);
                                                    }

                                                    eventManager.addParticipant(player);
                                                } else {
                                                    player.sendMessage(Utils.translate("&7The water has already passed the spawn location! " +
                                                            "&cTherefore you cannot join this event"));
                                                }
                                            } else {
                                                player.sendMessage(Utils.translate("&cYou cannot join this event while in the Black Market"));
                                            }
                                        } else {
                                            player.sendMessage(Utils.translate("&cYou cannot join this event when you were eliminated!"));
                                        }
                                    } else {
                                        player.sendMessage(Utils.translate("&cYou cannot do this while in Infinite Parkour"));
                                    }
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot do this while you are hiding players"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou are already in this event! &7Type &c/event leave &7to quit!"));
                }
            } else {
                player.sendMessage(Utils.translate("&cThere is no event running!"));
            }
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("leave") ||
                                     a[0].equalsIgnoreCase("quit"))) {

            if (eventManager.isEventRunning()) {

                if (eventManager.isParticipant(player))
                    eventManager.removeParticipant(player, false);
                else
                    player.sendMessage(Utils.translate("&cYou are not in this event! &7Type &c/event join &7to join!"));

            } else {
                player.sendMessage(Utils.translate("&cThere is no event running!"));
            }
        // send help
        } else if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("help")))
            sendHelp(sender);
        /*
            Admin section
         */
        else if (player.hasPermission("rn-parkour.admin")) {
            if (a.length == 2 && (a[0].equalsIgnoreCase("start") || a[0].equalsIgnoreCase("choose"))) {

                if (!eventManager.isEventRunning()) {
                    try {
                        EventType eventType = EventType.valueOf(a[1].toUpperCase());
                        List<Level> levels = Parkour.getLevelManager().getEventLevelsFromType(eventType);

                        if (levels.isEmpty())
                            player.sendMessage(Utils.translate("&cThere are no levels for the type &4" + eventType.name()));
                        else {
                            Level level = levels.get(new Random().nextInt(levels.size()));

                            switch (eventType) {
                                case PVP:
                                    eventManager.startEvent(new PvPEvent(level));
                                    break;
                                case RISING_WATER:
                                    eventManager.startEvent(new RisingWaterEvent(level));
                                    break;
                                case FALLING_ANVIL:
                                    eventManager.startEvent(new FallingAnvilEvent(level));
                                    break;
                                case ASCENT:
                                    eventManager.startEvent(new AscentEvent(level));
                                    break;
                                case MAZE:
                                    eventManager.startEvent(new MazeEvent(level));
                                    break;
                            }
                        }

                    } catch (IllegalArgumentException e) {
                        player.sendMessage(Utils.translate("&cInvalid event type!"));
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot start an event when there is already one active"));
                }
                // force end an event
            } else if (a.length == 1 && (a[0].equalsIgnoreCase("stop") ||
                    a[0].equalsIgnoreCase("end"))) {

                if (eventManager.isEventRunning())
                    eventManager.endEvent(null, true, false);
                else
                    player.sendMessage(Utils.translate("&cYou cannot end an event that is not running!"));
            }
        }
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&b/event join  &7Join an active event"));
        sender.sendMessage(Utils.translate("&b/event leave  &7Leave an active event"));

        // send admin commands if they have permission
        if (sender.hasPermission("rn-parkour.admin"))
        {

            sender.sendMessage(Utils.translate("&b/event start <type> &7Starts an event based on type"));
            sender.sendMessage(Utils.translate("&b/event stop  &7Stops the running event"));
        }
        sender.sendMessage(Utils.translate("&b/event help  &7Sends this display"));
    }
}
