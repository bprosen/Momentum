package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (sender.hasPermission("momentum.donator")) {
                StatsManager statsManager = Momentum.getStatsManager();
                PlayerStats spectatorStats = statsManager.get(player);

                if (spectatorStats != null) {
                    if (a.length > 0) {
                        if (a.length == 1 && a[0].equalsIgnoreCase("toggle")) {
                            if (spectatorStats.isSpectatable()) {
                                statsManager.updateSpectatable(spectatorStats, false);
                                sender.sendMessage(Utils.translate("&7You can no longer be spectated"));
                            } else {
                                statsManager.updateSpectatable(spectatorStats, true);
                                sender.sendMessage(Utils.translate("&7You can now be spectated"));
                            }
                        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
                            sendHelp(sender);
                            // spectate to player
                        } else if (a.length == 1) {
                            PlayerStats playerStats = Momentum.getStatsManager().getByName(a[0]);

                            if (playerStats == null || !playerStats.isLoaded()) {
                                player.sendMessage(Utils.translate("&4" + a[0] + "&c is not online or stats are not loaded yet"));
                                return false;
                            }

                            if (player.getName().equalsIgnoreCase(playerStats.getName())) {
                                player.sendMessage(Utils.translate("&cYou cannot spectate yourself"));
                                return false;
                            }

                            if (!playerStats.isSpectatable()) {
                                player.sendMessage(Utils.translate("&cThat player cannot be spectated"));
                                return false;
                            }

                            if (playerStats.getPlayer().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
                                player.sendMessage(Utils.translate("&cYou cannot spectate players that are in the plot world"));
                                return false;
                            }

                            if (spectatorStats.inPracticeMode()) {
                                player.sendMessage(Utils.translate("&cYou cannot enter spectator mode while in /prac"));
                                return false;
                            }

                            if (!spectatorStats.isLoaded()) {
                                player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
                                return false;
                            }

                            if (spectatorStats.inRace()) {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                                return false;
                            }

                            if (spectatorStats.isEventParticipant()) {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                                return false;
                            }

                            if (player.getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
                                player.sendMessage(Utils.translate("&cYou cannot spectate in the plot world"));
                                return false;
                            }

                            if (spectatorStats.isInInfinite()) {
                                player.sendMessage(Utils.translate("&cYou cannot spectate while in infinite parkour"));
                                return false;
                            }

                            if (playerStats.isInBlackMarket()) {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
                                return false;
                            }

                            if (!player.isOnGround()) {
                                player.sendMessage(Utils.translate("&cYou cannot use spectate while in the air"));
                                return false;
                            }

                            Location location = player.getLocation().clone();
                            if (location.add(0, 1, 0).getBlock().getType() == Material.AIR) {
                                boolean initialSpectate = true;
                                if (spectatorStats.isSpectating()) {
                                    initialSpectate = false;

                                    /*
                                     if they are already spectating and the person they
                                     are spectating are who they are trying to spectate again, cancel
                                     */
                                    if (spectatorStats.getPlayerToSpectate().equals(playerStats)) {
                                        player.sendMessage(Utils.translate("&cYou cannot spectate the same person you are spectating"));
                                        return true;
                                    }
                                }

                                // enable spectator mode
                                statsManager.setSpectatorMode(spectatorStats, playerStats, initialSpectate);
                                playerStats.sendMessage(Utils.translate("&2" + spectatorStats.getDisplayName() + " &7began to spectate you"));
                            }
                        }
                        // if they just run /spectate
                    } else if (spectatorStats.isSpectating()) {
                        statsManager.resetSpectatorMode(spectatorStats);
                    } else {
                        sendHelp(sender);
                    }
                } else {
                    sender.sendMessage(Utils.translate("&cError loading your data"));
                }
            } else {
                sender.sendMessage(Utils.translate("&cOnly players with &aMilites &cor higher can use spectate mode"));
            }
        } else {
            sender.sendMessage(Utils.translate("&cOnly players can run this command"));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&a&lSpectator Help"));
        sender.sendMessage(Utils.translate("&2/spectate (player)  &7Spectates a player"));
        sender.sendMessage(Utils.translate("&2/spectate toggle  &7Toggles if you can be spectated"));
        sender.sendMessage(Utils.translate("&2/spectate help  &7Display this page"));
    }
}
