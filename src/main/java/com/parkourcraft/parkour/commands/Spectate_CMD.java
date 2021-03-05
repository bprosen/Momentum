package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.data.stats.Stats_DB;
import com.parkourcraft.parkour.gameplay.SpectatorHandler;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (sender.hasPermission("pc-parkour.donator")) {

                PlayerStats spectatorStats = Parkour.getStatsManager().get(player);

                if (spectatorStats != null) {
                    if (a.length > 0) {
                        if (a[0].equalsIgnoreCase("toggle")) {
                            if (spectatorStats.isSpectatable())
                                sender.sendMessage(Utils.translate("&7You can no longer be spectated"));
                            else
                                sender.sendMessage(Utils.translate("&7You can now be spectated"));

                            spectatorStats.setSpectatable(!spectatorStats.isSpectatable());
                            Stats_DB.updatePlayerSpectatable(spectatorStats);
                        } else {
                            PlayerStats playerStats = Parkour.getStatsManager().getByNameIgnoreCase(a[0]);

                            if (playerStats != null && playerStats.getPlayer().isOnline()) {
                                if (playerStats.isSpectatable()) {
                                    if (playerStats.getPlayer().getWorld().equals(spectatorStats.getPlayer().getWorld())) {

                                        // enable spectator mode
                                        SpectatorHandler.setSpectatorMode(spectatorStats, playerStats);

                                        playerStats.getPlayer().sendMessage(Utils.translate("&2" +
                                                spectatorStats.getPlayerName() + " &7began to spectate you"));
                                    } else {
                                        sender.sendMessage(Utils.translate("&cYou are not in the same world as &4" + a[0]));
                                    }
                                } else {
                                    sender.sendMessage(Utils.translate("&cThat player cannot be spectated"));
                                }
                            } else {
                                sender.sendMessage(Utils.translate("&cThere is no player online named &4" + a[0]));
                            }
                        }
                    } else if (spectatorStats.getPlayerToSpectate() != null) {
                        SpectatorHandler.removeSpectatorMode(spectatorStats);
                    } else if (spectatorStats.isSpectatable())
                            sender.sendMessage(Utils.translate("&7You can be spectated"));
                        else
                            sender.sendMessage(Utils.translate("&7You can not be spectated"));
                        sendHelp(sender);
                } else {
                    sender.sendMessage(Utils.translate("&cError loading your data"));
                }
            } else {
                sender.sendMessage(Utils.translate("&cOnly Donators can use spectate mode"));
            }
        } else {
            sender.sendMessage(Utils.translate("&cOnly players can run this command"));
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("player"));
        sender.sendMessage(getHelp("toggle"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("player"))
            return Utils.translate("&2/spectate <player>  &7Spectates a player");
        else if (cmd.equalsIgnoreCase("toggle"))
            return Utils.translate("&2/spectate toggle  &7Toggles if you can be spectated");
        return "";
    }
}
