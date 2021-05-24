package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.events.EventManager;
import com.parkourcraft.parkour.data.events.EventType;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.PlayerHider;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            Admin Section
         */
        if (player.hasPermission("pc-parkour.admin")) {
            // join event
            if (a.length == 1 && a[0].equalsIgnoreCase("join")) {

                // run through all the conditions that would not allow them to join
                if (eventManager.isEventRunning()) {
                    if (eventManager.get(player.getUniqueId().toString()) == null) {
                        if (playerStats.getPracticeLocation() == null) {
                            if (playerStats.getPlayerToSpectate() == null) {
                                if (!playerStats.inRace()) {
                                    if (!PlayerHider.containsPlayer(player)) {
                                        if (!eventManager.isEliminated(player)) {

                                            // run a check if the event is rising water to see if where they would spawn has been covered by water
                                            if (eventManager.isStartCoveredInWater()) {
                                                player.sendMessage(Utils.translate("&7The water has already passed the spawn location! " +
                                                                                        "&cTherefore you cannot join this event"));
                                                return true;
                                            }

                                            eventManager.addParticipant(player);
                                        } else {
                                            player.sendMessage(Utils.translate("&cYou cannot join this event when you were eliminated!"));
                                        }
                                    } else {
                                        player.sendMessage(Utils.translate("&cYou cannot do this while you are hiding players"));
                                    }
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
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
            // leave event
            } else if (a.length == 1 && (a[0].equalsIgnoreCase("leave") ||
                                         a[0].equalsIgnoreCase("quit"))) {

                if (eventManager.isEventRunning()) {

                    if (eventManager.get(player.getUniqueId().toString()) != null)
                        eventManager.removeParticipant(player, false);
                    else
                        player.sendMessage(Utils.translate("&cYou are not in this event! &7Type &c/event join &7to join!"));

                } else {
                    player.sendMessage(Utils.translate("&cThere is no event running!"));
                }
            // start event
            } else if (a.length == 2 && (a[0].equalsIgnoreCase("start") ||
                                         a[0].equalsIgnoreCase("choose"))) {

                if (!eventManager.isEventRunning()) {
                    try {
                        EventType eventType = EventType.valueOf(a[1].toUpperCase());

                        // now check if there are any event levels for that type
                        switch (eventType) {
                            case PVP:
                                if (Parkour.getLevelManager().getPvPEventLevels().isEmpty()) {
                                    player.sendMessage(Utils.translate("&cThere are no levels for &4PvP Event &cavailable"));
                                    return true;
                                }
                                break;
                            case HALF_HEART:
                                if (Parkour.getLevelManager().getHalfHeartEventLevels().isEmpty()) {
                                    player.sendMessage(Utils.translate("&cThere are no levels for &4Half Heart Event &cavailable"));
                                    return true;
                                }
                                break;
                            case RISING_WATER:
                                if (Parkour.getLevelManager().getRisingWaterEventLevels().isEmpty()) {
                                    player.sendMessage(Utils.translate("&cThere are no levels for &4Rising Water Event &cavailable"));
                                    return true;
                                }
                                break;
                        }

                        eventManager.startEvent(eventType);
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
                    eventManager.endEvent(null,true, false);
                else
                    player.sendMessage(Utils.translate("&cYou cannot end an event that is not running!"));

            // send help
            } else if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("help"))) {
                sendHelp(sender);
            }
        /*
            Player Section
         */
        // join event
        } else if (a.length == 1 && a[0].equalsIgnoreCase("join")) {

            // run through all the conditions that would not allow them to join
            if (eventManager.isEventRunning()) {
                if (eventManager.get(player.getUniqueId().toString()) == null) {
                    if (playerStats.getPracticeLocation() == null) {
                        if (playerStats.getPlayerToSpectate() == null) {
                            if (!playerStats.inRace()) {
                                if (!PlayerHider.containsPlayer(player)) {
                                    if (!eventManager.isEliminated(player)) {
                                        eventManager.addParticipant(player);
                                    } else {
                                        player.sendMessage(Utils.translate("&cYou cannot join this event when you were eliminated!"));
                                    }
                                } else {
                                    player.sendMessage(Utils.translate("&cYou cannot do this while you are hiding players"));
                                }
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
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

                if (eventManager.get(player.getUniqueId().toString()) != null)
                    eventManager.removeParticipant(player, false);
                else
                    player.sendMessage(Utils.translate("&cYou are not in this event! &7Type &c/event join &7to join!"));

            } else {
                player.sendMessage(Utils.translate("&cThere is no event running!"));
            }
        // send help
        } else if (a.length == 0 || (a.length == 1 && a[0].equalsIgnoreCase("help"))) {
            sendHelp(sender);
        }
        return false;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("join")); // console friendly
        sender.sendMessage(getHelp("leave"));

        // send admin commands if they have permission
        if (sender.hasPermission("pc-parkour.admin")) {

            sender.sendMessage(getHelp("start"));
            sender.sendMessage(getHelp("stop"));
        }
        sender.sendMessage(getHelp("help"));
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "join":
                return Utils.translate("&b/event join  &7Join an active event");
            case "leave":
                return Utils.translate("&b/event leave  &7Leave an active event");
            case "start":
                return Utils.translate("&b/event start <type> &7Starts an event based on type");
            case "stop":
                return Utils.translate("&b/event stop  &7Stops the running event");
            case "help":
                return Utils.translate("&b/event help  &7Sends this display");
        }
        return "";
    }
}
