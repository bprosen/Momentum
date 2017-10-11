package com.parkourcraft.Parkour.commands;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.data.stats.Stats_DB;
import com.parkourcraft.Parkour.gameplay.SpectatorHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spectate_CMD implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {
            if (sender.hasPermission("parkourcraft.localcommands.donator")) {

                PlayerStats spectatorStats = Parkour.stats.get(((Player) sender).getPlayer());

                if (spectatorStats != null) {
                    if (a.length > 0) {
                        if (a[0].equalsIgnoreCase("public")) {
                            if (spectatorStats.isSpectatable())
                                sender.sendMessage(ChatColor.GRAY + "You can no longer be spectated");
                            else
                                sender.sendMessage(ChatColor.GRAY + "You can now be spectated");

                            spectatorStats.isSpectatable(!spectatorStats.isSpectatable());
                            Stats_DB.updatePlayerSpectatable(spectatorStats);
                        } else {
                            PlayerStats playerStats = Parkour.stats.getByNameIgnoreCase(a[0]);

                            if (playerStats != null
                                    && playerStats.getPlayer().isOnline()) {
                                if (playerStats.isSpectatable()) {
                                    spectatorStats.setPlayerToSpectate(playerStats);
                                    SpectatorHandler.setSpectatorMode(spectatorStats.getPlayer());
                                    SpectatorHandler.spectateToPlayer(
                                            spectatorStats.getPlayer(),
                                            playerStats.getPlayer()
                                    );

                                    playerStats.getPlayer().sendMessage(
                                            ChatColor.GREEN + spectatorStats.getPlayerName()
                                                    + ChatColor.GRAY + " began to spectate you"
                                    );
                                } else
                                    sender.sendMessage(ChatColor.RED + "That player cannot be spectated");
                            } else
                                sender.sendMessage(
                                        ChatColor.RED + "There is no player online named "
                                                + ChatColor.DARK_RED + a[0]
                                );
                        }
                    } else if (spectatorStats.getPlayerToSpectate() != null)
                        SpectatorHandler.removeSpectatorMode(spectatorStats);
                    else {
                        if (spectatorStats.isSpectatable())
                            sender.sendMessage(ChatColor.GRAY + "You can be spectated");
                        else
                            sender.sendMessage(ChatColor.GRAY + "You can not be spectated");
                        sendHelp(sender);
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "Error loading your data");
            } else
                sender.sendMessage(ChatColor.RED + "Only Donators can use spectate mode");
        } else
            sender.sendMessage(ChatColor.RED + "Only players can run this command");

        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("player"));
        sender.sendMessage(getHelp("public"));
    }

    private static String getHelp(String cmd) {
        if (cmd.equalsIgnoreCase("player"))
            return ChatColor.GREEN + "/spectate <player>" +
                    ChatColor.GRAY + " Spectates a player";
        else if (cmd.equalsIgnoreCase("public"))
            return ChatColor.GREEN + "/spectate public" +
                    ChatColor.GRAY + " Toggles if you can be spectated";
        return "";
    }

}
