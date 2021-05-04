package com.parkourcraft.parkour.commands;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.checkpoints.CheckpointDB;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.plots.PlotsDB;
import com.parkourcraft.parkour.data.stats.PlayerStats;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class PlotCMD implements CommandExecutor {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // send help
        if (a.length == 0) {
            sendHelp(sender);
        // do create algorithm after
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("auto") || a[0].equalsIgnoreCase("create"))) {
            Parkour.getPlotsManager().createPlot(player);
        // teleport to plot
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("home") ||
                                     a[0].equalsIgnoreCase("teleport") ||
                                     a[0].equalsIgnoreCase("h"))) {

            Plot plot = Parkour.getPlotsManager().get(player.getName());

            // make sure they have a plot
            if (plot != null) {
                plot.teleportOwner();

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                resetPlayerLevelData(playerStats);
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot to teleport to"));
            }
        // visit someone else
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("visit") ||
                                     a[0].equalsIgnoreCase("v"))) {

            String playerName = a[1];
            if (PlotsDB.playerNameHasPlot(playerName)) {
                String targetLoc = PlotsDB.getPlotCenterFromName(playerName);
                String[] split = targetLoc.split(":");

                // get loc from result
                Location loc = new Location(Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world),
                                            Double.parseDouble(split[0]),
                                            Parkour.getSettingsManager().player_submitted_plot_default_y,
                                            Double.parseDouble(split[1]),
                                            player.getLocation().getYaw(), player.getLocation().getPitch());

                player.teleport(loc.clone().add(0.5, 0, 0.5));
                player.sendMessage(Utils.translate("&7Teleporting you to &a" + playerName + "&7's Plot"));

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                resetPlayerLevelData(playerStats);
            } else {
                player.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a plot"));
            }
        // clear stuff on plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("clear")) {
            Plot plot = Parkour.getPlotsManager().get(player.getName());

            if (plot != null) {
                Parkour.getPlotsManager().clearPlot(plot);
                player.sendMessage(Utils.translate("&cYou have cleared your plot"));

                // reset bedrock and teleport 1 second later
                new BukkitRunnable() {
                    public void run() {
                        plot.getSpawnLoc().clone().subtract(0, 1, 0).getBlock().setType(Material.BEDROCK);
                    }
                }.runTaskLater(Parkour.getPlugin(), 20 * 1);
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot!"));
            }
        // trust on plot
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("trust") ||
                                     a[0].equalsIgnoreCase("add"))) {

            Plot plot = Parkour.getPlotsManager().get(player.getName());
            Player target = Bukkit.getPlayer(a[1]);

            if (target == null) {
                player.sendMessage(Utils.translate("&4" + target.getName() + " &cis not online!"));
                return true;
            }

            // if they have plot
            if (plot != null) {
                // if they are not a trusted player
                if (!plot.getTrustedPlayers().contains(target.getName())) {
                    PlotsDB.addTrustedPlayer(player, target);
                    plot.addTrustedPlayer(target);

                    player.sendMessage(Utils.translate("&7You trusted &3" + target.getName() + " &7to your plot!"));
                } else {
                    player.sendMessage(Utils.translate("&4" + player.getName() + " &cis already trusted in your plot"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot"));
            }
        // untrust from plot
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("untrust") ||
                                     a[0].equalsIgnoreCase("remove"))) {

            Plot plot = Parkour.getPlotsManager().get(player.getName());
            Player target = Bukkit.getPlayer(a[1]);

            if (target == null) {
                player.sendMessage(Utils.translate("&4" + target.getName() + " &cis not online!"));
                return true;
            }

            // if they have plot
            if (plot != null) {
                // if they are not a trusted player
                if (plot.getTrustedPlayers().contains(target.getName())) {
                    PlotsDB.removeTrustedPlayer(player, target);
                    plot.removeTrustedPlayer(target);

                    player.sendMessage(Utils.translate("&7You removed &3" + target.getName() + " &7from your plot!"));
                } else {
                    player.sendMessage(Utils.translate("&4" + player.getName() + " &cis not trusted in your plot"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou do not have a plot"));
            }
        // clear and delete their plot data
        } else if (a.length == 1 && a[0].equalsIgnoreCase("delete")) {
            // if they are confirming, delete it
            if (confirmMap.containsKey(player.getName())) {
                confirmMap.get(player.getName()).cancel();
                confirmMap.remove(player.getName());
                Parkour.getPlotsManager().deletePlot(player);
            // otherwise ask them to confirm it
            } else {
                confirmPlayer(player);
                player.sendMessage(Utils.translate("&cAre you sure? &7Type &c/plot delete &7again within 30 seconds to confirm"));
            }
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
        } else {
            sendHelp(sender);
        }
        return false;
    }

    private void resetPlayerLevelData(PlayerStats playerStats) {
        if (playerStats.getLevel() != null) {
            // save checkpoint if had one
            if (playerStats.getCheckpoint() != null) {
                CheckpointDB.savePlayerAsync(playerStats.getPlayer());
                playerStats.resetCheckpoint();
            }
            playerStats.resetLevel();
            playerStats.resetPracticeMode();
        }
    }

    private void confirmPlayer(Player player) {
        confirmMap.put(player.getName(), new BukkitRunnable() {
            public void run() {
                // make sure they are still in it
                if (confirmMap.containsKey(player.getName())) {
                    confirmMap.remove(player.getName());
                    player.sendMessage(Utils.translate("&cYou ran out of time to confirm"));
                }
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * 30));
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("create")); // console friendly
        sender.sendMessage(getHelp("delete"));
        sender.sendMessage(getHelp("clear"));
        sender.sendMessage(getHelp("home"));
        sender.sendMessage(getHelp("visit"));
        sender.sendMessage(getHelp("trust"));
        sender.sendMessage(getHelp("untrust"));
        sender.sendMessage(getHelp("help"));
    }

    private static String getHelp(String cmd) {
        switch (cmd.toLowerCase()) {
            case "create":
                return Utils.translate("&a/plot create  &7Automatically create a plot");
            case "delete":
                return Utils.translate("&a/plot delete  &7Deletes your plot (confirm needed)");
            case "clear":
                return Utils.translate("&a/plot clear  &7Clears your plot but does not delete it");
            case "home":
                return Utils.translate("&a/plot home  &7Teleports you to your plot");
            case "visit":
                return Utils.translate("&a/plot visit <player>  &7Visit another player's plot");
            case "trust":
                return Utils.translate("&a/plot trust <player>  &7Trust a player to your plot");
            case "untrust":
                return Utils.translate("&a/plot untrust <player>  &7Untrust a player from your plot");
            case "help":
                return Utils.translate("&a/plot help  &7Sends this display");
        }
        return "";
    }
}