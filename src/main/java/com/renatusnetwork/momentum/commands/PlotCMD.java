package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.plots.Plot;
import com.renatusnetwork.momentum.data.plots.PlotsDB;
import com.renatusnetwork.momentum.data.ranks.Rank;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;

public class PlotCMD implements CommandExecutor {

    private HashMap<String, BukkitTask> deletePlotConfirm = new HashMap<>();
    private HashMap<String, BukkitTask> acceptPlotConfirm = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        /*
          admin commands section
         */
        if (player.hasPermission("momentum.admin")) {
            // send list of plots in gui
            if (a.length == 2 && a[0].equalsIgnoreCase("submit") && a[1].equalsIgnoreCase("list")) {

                // open submitted plots list
                Momentum.getMenuManager().openSubmittedPlotsGUI(Momentum.getStatsManager().get(player));
            } else if (a.length == 1 && a[0].equalsIgnoreCase("bypass")) {

                PlayerStats playerStats = Momentum.getStatsManager().get(player);
                boolean bypassingPlots = true;

                if (playerStats.isBypassingPlots())
                    bypassingPlots = false;

                playerStats.setPlotBypassing(bypassingPlots);
                player.sendMessage(Utils.translate("&7You have toggled &cPlot Bypassing &7to &c" + bypassingPlots));

            } else if (a.length == 3 && a[0].equalsIgnoreCase("submit") && a[1].equalsIgnoreCase("accept")) {

                String plotOwner = a[2];
                Plot targetPlot = Momentum.getPlotsManager().get(plotOwner);

                if (targetPlot != null) {
                    if (targetPlot.isSubmitted()) {
                        // make sure they confirm it
                        if (acceptPlotConfirm.containsKey(player.getName())) {
                            acceptPlotConfirm.get(player.getName()).cancel();
                            acceptPlotConfirm.remove(player.getName());

                            targetPlot.desubmit();
                            PlotsDB.toggleSubmittedFromName(plotOwner);
                            player.sendMessage(Utils.translate("&7You accepted &4" + plotOwner + "&7's Plot"));

                            Bukkit.dispatchCommand(player, "mail send " + plotOwner + " Your plot has been accepted, congratulations!");
                            // otherwise, add timer and add to confirm map
                        } else {
                            player.sendMessage(Utils.translate("&7Are you someone with backend that will add this map?" +
                                    " If so, type &c/plot submit accept " + plotOwner + " &7again"));

                            acceptPlotConfirm.put(player.getName(), new BukkitRunnable() {
                                public void run() {
                                    // make sure they are still in it
                                    if (acceptPlotConfirm.containsKey(player.getName())) {
                                        acceptPlotConfirm.remove(player.getName());
                                        player.sendMessage(Utils.translate("&cYou ran out of time to confirm"));
                                    }
                                }
                            }.runTaskLater(Momentum.getPlugin(), 20 * 30));
                        }
                    }
                } else {
                    player.sendMessage(Utils.translate("&4" + plotOwner + " &cdoes not have a Plot"));
                }

            } else if (a.length > 3 && a[0].equalsIgnoreCase("submit") && a[1].equalsIgnoreCase("deny")) {

                String plotOwner = a[2];

                String[] split = Arrays.copyOfRange(a, 3, a.length);
                // make sure it is not too long of a reason
                if (split.length > 10) {
                    player.sendMessage(Utils.translate("&7Too long of a reason! Make it &c10 words &7or under"));
                    return true;
                }

                String reason = String.join(" ", split);

                Plot targetPlot = Momentum.getPlotsManager().get(plotOwner);
                if (targetPlot != null) {
                    if (targetPlot.isSubmitted()) {

                        targetPlot.desubmit();
                        PlotsDB.toggleSubmittedFromName(plotOwner);
                        player.sendMessage(Utils.translate("&cYou denied &4" + plotOwner + "&c's Plot"));

                        Bukkit.dispatchCommand(player, "mail send " + plotOwner + " Your plot has been denied for: " + reason);
                    } else {
                        player.sendMessage(Utils.translate("&4" + plotOwner + "&c's Plot is not submitted!"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&4" + plotOwner + " &cdoes not have a Plot"));
                }
            } else if (a.length == 1 && (a[0].equalsIgnoreCase("auto") || a[0].equalsIgnoreCase("create"))) {
                createPlot(player);
                // teleport to plot
            } else if (a.length == 1 && (a[0].equalsIgnoreCase("home") ||
                                         a[0].equalsIgnoreCase("teleport") ||
                                         a[0].equalsIgnoreCase("h"))) {

                plotHome(player);
                // visit someone else
            } else if (a.length == 2 && (a[0].equalsIgnoreCase("visit") ||
                                         a[0].equalsIgnoreCase("v"))) {

                visitPlot(player, a);
                // clear stuff on plot
            } else if (a.length >= 1 && a[0].equalsIgnoreCase("clear")) {

                // if 1, then its themself
                if (a.length == 1) {
                    Plot plot = Momentum.getPlotsManager().get(player.getName());
                    clearPlot(plot, false, player);
                // if 2 then its a target
                } else if (a.length == 2) {
                    Plot targetPlot = Momentum.getPlotsManager().get(a[1]);
                    clearPlot(targetPlot, true, player);
                }

                // trust on plot
            } else if (a.length == 2 && (a[0].equalsIgnoreCase("trust") ||
                                         a[0].equalsIgnoreCase("add"))) {

                trustPlayer(player, a);
            // untrust from plot
            } else if (a.length == 2 && (a[0].equalsIgnoreCase("untrust") ||
                                         a[0].equalsIgnoreCase("remove"))) {

                untrustPlayer(player, a);
            // clear and delete their plot data
            } else if (a.length >= 1 && a[0].equalsIgnoreCase("delete")) {

                // if 1, then its themself
                if (a.length == 1) {
                    Plot plot = Momentum.getPlotsManager().get(player.getName());
                    deletePlot(plot, false, player);
                    // if 2 then its a target
                } else if (a.length == 2) {
                    Plot targetPlot = Momentum.getPlotsManager().getIgnoreCase(a[1]);
                    deletePlot(targetPlot, true, player);
                }

            // submit your plot
            } else if (a.length == 1 && a[0].equalsIgnoreCase("submit")) {
                submitPlot(Momentum.getStatsManager().get(player));
            // get info of current loc
            } else if (a.length == 1 && a[0].equalsIgnoreCase("info")) {
                plotInfo(player);
            } else if ((a.length == 1 && a[0].equalsIgnoreCase("help")) || a.length == 0) {
                sendHelp(sender);
            } else {
                sendHelp(sender);
            }
        } else

        /*
          player commands section
         */

        // send help
        if (a.length == 0) {
            sendHelp(sender);
        // do create algorithm after
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("auto") || a[0].equalsIgnoreCase("create"))) {
            createPlot(player);
        // teleport to plot
        } else if (a.length == 1 && (a[0].equalsIgnoreCase("home") ||
                                     a[0].equalsIgnoreCase("teleport") ||
                                     a[0].equalsIgnoreCase("h"))) {

            plotHome(player);
        // visit someone else
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("visit") ||
                                     a[0].equalsIgnoreCase("v"))) {

            visitPlot(player, a);
        // clear stuff on plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("clear")) {

            Plot plot = Momentum.getPlotsManager().get(player.getName());
            clearPlot(plot, false, player);
        // trust on plot
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("trust") ||
                                     a[0].equalsIgnoreCase("add"))) {

            trustPlayer(player, a);
        // untrust from plot
        } else if (a.length == 2 && (a[0].equalsIgnoreCase("untrust") ||
                                     a[0].equalsIgnoreCase("remove"))) {

            untrustPlayer(player, a);
        // clear and delete their plot data
        } else if (a.length == 1 && a[0].equalsIgnoreCase("delete")) {
            Plot plot = Momentum.getPlotsManager().get(player.getName());
            deletePlot(plot, false, player);
        // submit your plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("submit")) {
            submitPlot(Momentum.getStatsManager().get(player));
        // get info of current loc
        } else if (a.length == 1 && a[0].equalsIgnoreCase("info")) {
            plotInfo(player);
        } else if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
        } else {
            sendHelp(sender);
        }
        return false;
    }

    private void createPlot(Player player) {

        PlayerStats playerStats = Momentum.getStatsManager().get(player.getUniqueId().toString());

        if (checkConditions(playerStats))
        {
            if (!Momentum.getPlotsManager().exists(player.getName()))
            {
                Rank minimumRank = Momentum.getRanksManager().get(Momentum.getSettingsManager().minimum_rank_for_plot_creation);

                if (Momentum.getRanksManager().isPastOrAtRank(playerStats, minimumRank))
                    Momentum.getPlotsManager().createPlot(playerStats);
                else
                    player.sendMessage(Utils.translate("&7You must be at least &c" + minimumRank.getTitle() + " &7to create a &aPlot"));
            }
            else
                player.sendMessage(Utils.translate("&cYou already have a plot"));
        }
    }

    private void clearPlot(Plot targetPlot, boolean forceCleared, Player player) {

        if (targetPlot != null) {
            Momentum.getPlotsManager().clearPlot(targetPlot, false);
            if (!forceCleared)
                player.sendMessage(Utils.translate("&aYou cleared your plot! You may need to relog to remove any glitched client-side blocks you see"));
            else
                player.sendMessage(Utils.translate("&aYou cleared &2" + targetPlot.getOwnerName() + "&a's Plot"));
        } else if (!forceCleared)
            player.sendMessage(Utils.translate("&cYou do not have a Plot"));
        else
            player.sendMessage(Utils.translate("&aThey do not have a Plot"));
    }

    private void deletePlot(Plot targetPlot, boolean forceCleared, Player player) {

        if (targetPlot != null) {
            // if they are confirming, delete it
            if (deletePlotConfirm.containsKey(player.getName())) {
                deletePlotConfirm.get(player.getName()).cancel();
                deletePlotConfirm.remove(player.getName());

                Momentum.getPlotsManager().deletePlot(targetPlot);
                if (!forceCleared)
                    player.sendMessage(Utils.translate("&aYou deleted your plot!"));
                else
                    player.sendMessage(Utils.translate("&aYou deleted &2" + targetPlot.getOwnerName() + "&a's Plot"));

                // otherwise ask them to confirm it
            } else {
                confirmPlayer(player);
                player.sendMessage(Utils.translate("&cAre you sure? &7Type &c/plot delete &7again within 30 seconds to confirm"));
            }
        } else if (!forceCleared)
            player.sendMessage(Utils.translate("&cYou do not have a Plot"));
        else
            player.sendMessage(Utils.translate("&aThey do not have a Plot"));

    }

    private void untrustPlayer(Player player, String[] a)
    {
        Plot plot = Momentum.getPlotsManager().get(player.getName());

        // if you have plot
        if (plot == null) {
            player.sendMessage(Utils.translate("&cYou do not have a plot"));
            return;
        }

        String name = a[1];
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStats playerStats = Momentum.getStatsManager().getByName(name);
                String target = playerStats == null ? StatsDB.getUUIDByName(name) : playerStats.getUUID();
                if (target == null) {
                    player.sendMessage(Utils.translate("&4" + name + " &chas never played before"));
                    return;
                }

                if (!plot.isTrusted(target)) {
                    player.sendMessage(Utils.translate("&4" + name + " &cis not trusted in your plot"));
                    return;
                }

                Momentum.getPlotsManager().removeTrusted(plot, target);
                player.sendMessage(Utils.translate("&7You untrusted &3" + name + " &7from your plot"));
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }

    private void trustPlayer(Player player, String[] a)
    {
        Plot plot = Momentum.getPlotsManager().get(player.getName());

        // if you have plot
        if (plot == null) {
            player.sendMessage(Utils.translate("&cYou do not have a plot"));
            return;
        }

        String name = a[1];
        new BukkitRunnable() {
            @Override
            public void run() {
                PlayerStats playerStats = Momentum.getStatsManager().getByName(name);
                String target = playerStats == null ? StatsDB.getUUIDByName(name) : playerStats.getUUID();
                if (target == null) {
                    player.sendMessage(Utils.translate("&4" + name + " &chas never played before"));
                    return;
                }

                if (plot.isTrusted(target)) {
                    player.sendMessage(Utils.translate("&4" + name + " &cis already trusted in your plot"));
                    return;
                }

                Momentum.getPlotsManager().addTrusted(plot, target);
                player.sendMessage(Utils.translate("&7You trusted &3" + name + " &7to your plot"));
            }
        }.runTaskAsynchronously(Momentum.getPlugin());
    }

    private void visitPlot(Player player, String[] a) {
        PlayerStats playerStats = Momentum.getStatsManager().get(player);

        if (checkConditions(playerStats)) {

            String playerName = a[1];
            Plot targetPlot = Momentum.getPlotsManager().getIgnoreCase(playerName);
            if (targetPlot != null) {
                targetPlot.teleportPlayerToEdge(player);
                player.sendMessage(Utils.translate("&7Teleporting you to &a" + playerName + "&7's Plot"));

                resetPlayerLevelData(playerStats);
            } else {
                player.sendMessage(Utils.translate("&4" + playerName + " &cdoes not have a plot"));
            }
        }
    }

    private void plotHome(Player player) {
        Plot plot = Momentum.getPlotsManager().get(player.getName());

        // make sure they have a plot
        if (plot != null) {
            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            if (checkConditions(playerStats)) {
                plot.teleportPlayerToEdge(player);
                resetPlayerLevelData(playerStats);
            }
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot to teleport to"));
        }
    }

    private void submitPlot(PlayerStats playerStats)
    {
        Player player = playerStats.getPlayer();
        Plot plot = Momentum.getPlotsManager().get(player.getName());

        // they have a plot
        if (plot != null)
        {
            // already submitted
            if (!plot.isSubmitted())
                // submit map!
                Momentum.getMenuManager().openInventory(playerStats, "submit-plot", true);
            else
                player.sendMessage(Utils.translate("&cYou have already submitted your plot!"));
        }
        else
            player.sendMessage(Utils.translate("&cYou do not have a plot to submit!"));
    }

    private void plotInfo(Player player) {
        Plot plot = Momentum.getPlotsManager().getPlotInLocation(player.getLocation());

        if (plot != null)
            player.sendMessage(Utils.translate("&a" + plot.getOwnerName() + " &7owns this plot"));
        else
            player.sendMessage(Utils.translate("&cYou are not in any plot currently"));
    }

    private void resetPlayerLevelData(PlayerStats playerStats)
    {
        if (playerStats.inLevel())
            Momentum.getStatsManager().leaveLevelAndReset(playerStats, true);
    }

    private void confirmPlayer(Player player) {
        deletePlotConfirm.put(player.getName(), new BukkitRunnable() {
            public void run() {
                // make sure they are still in it
                if (deletePlotConfirm.containsKey(player.getName())) {
                    deletePlotConfirm.remove(player.getName());
                    player.sendMessage(Utils.translate("&cYou ran out of time to confirm"));
                }
            }
        }.runTaskLater(Momentum.getPlugin(), 20 * 30));
    }

    private boolean checkConditions(PlayerStats playerStats) {
        Player player = playerStats.getPlayer();

        if (playerStats.inLevel() && playerStats.hasAutoSave() && !playerStats.getPlayer().isOnGround())
        {
            player.sendMessage(Utils.translate("&cYou cannot leave the level while in midair with auto-save enabled"));
            return false;
        }

        if (playerStats.inRace())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
            return false;
        }

        if (playerStats.isInInfinite())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in infinite"));
            return false;
        }

        if (playerStats.isSpectating())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
            return false;
        }

        if (playerStats.isEventParticipant())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
            return false;
        }

        if (playerStats.isInBlackMarket())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
            return false;
        }

        if (playerStats.inPracticeMode())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
            return false;
        }

        if (playerStats.isInTutorial())
        {
            player.sendMessage(Utils.translate("&cYou cannot do this while in tutorial"));
            return false;
        }

        return true;
    }

    private static void sendHelp(CommandSender sender)
    {
        sender.sendMessage(Utils.translate("&2&lPlots Help"));
        sender.sendMessage(Utils.translate("&a/plot create  &7Automatically create a plot"));
        sender.sendMessage(Utils.translate("&a/plot delete  &7Deletes your plot (confirm needed)"));
        sender.sendMessage(Utils.translate("&a/plot clear  &7Clears your plot but does not delete it"));
        sender.sendMessage(Utils.translate("&a/plot home  &7Teleports you to your plot"));
        sender.sendMessage(Utils.translate("&a/plot visit <player>  &7Visit another player's plot"));
        sender.sendMessage(Utils.translate("&a/plot trust <player>  &7Trust a player to your plot"));
        sender.sendMessage(Utils.translate("&a/plot untrust <player>  &7Untrust a player from your plot"));
        sender.sendMessage(Utils.translate("&a/plot submit  &7Submit your plot"));

        // send admin commands if they have permission
        if (sender.hasPermission("momentum.admin"))
        {
            sender.sendMessage(Utils.translate("&a/plot submit accept <player>  &7Accepts a submitted plot"));
            sender.sendMessage(Utils.translate("&a/plot submit deny <player> (Reason...)  &7Deny a player with reason (10 word max)"));
            sender.sendMessage(Utils.translate("&a/plot submit list  &7Open the submitted parkours GUI"));
            sender.sendMessage(Utils.translate("&a/plot bypass  &7Toggles Plot Bypassing"));
        }
        sender.sendMessage(Utils.translate("&a/plot help  &7Sends this display"));
    }
}
