package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.menus.MenuManager;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.data.plots.PlotsDB;
import com.renatusnetwork.parkour.data.ranks.Rank;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.gameplay.handlers.PracticeHandler;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.mozilla.javascript.ObjArray;

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
        if (player.hasPermission("rn-parkour.admin")) {
            // send list of plots in gui
            if (a.length == 2 && a[0].equalsIgnoreCase("submit") && a[1].equalsIgnoreCase("list")) {

                // open submitted plots list
                Parkour.getMenuManager().openSubmittedPlotsGUI(Parkour.getStatsManager().get(player));
            } else if (a.length == 1 && a[0].equalsIgnoreCase("bypass")) {

                PlayerStats playerStats = Parkour.getStatsManager().get(player);
                boolean bypassingPlots = true;

                if (playerStats.isBypassingPlots())
                    bypassingPlots = false;

                playerStats.setPlotBypassing(bypassingPlots);
                player.sendMessage(Utils.translate("&7You have toggled &cPlot Bypassing &7to &c" + bypassingPlots));

            } else if (a.length == 3 && a[0].equalsIgnoreCase("submit") && a[1].equalsIgnoreCase("accept")) {

                String plotOwner = a[2];
                Plot targetPlot = Parkour.getPlotsManager().get(plotOwner);

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
                            }.runTaskLater(Parkour.getPlugin(), 20 * 30));
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

                Plot targetPlot = Parkour.getPlotsManager().get(plotOwner);
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
                    Plot plot = Parkour.getPlotsManager().get(player.getName());
                    clearPlot(plot, false, player);
                // if 2 then its a target
                } else if (a.length == 2) {
                    Plot targetPlot = Parkour.getPlotsManager().get(a[1]);
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
                    Plot plot = Parkour.getPlotsManager().get(player.getName());
                    deletePlot(plot, false, player);
                    // if 2 then its a target
                } else if (a.length == 2) {
                    Plot targetPlot = Parkour.getPlotsManager().getIgnoreCase(a[1]);
                    deletePlot(targetPlot, true, player);
                }

            // submit your plot
            } else if (a.length == 1 && a[0].equalsIgnoreCase("submit")) {
                submitPlot(Parkour.getStatsManager().get(player));
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

            Plot plot = Parkour.getPlotsManager().get(player.getName());
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
            Plot plot = Parkour.getPlotsManager().get(player.getName());
            deletePlot(plot, false, player);
        // submit your plot
        } else if (a.length == 1 && a[0].equalsIgnoreCase("submit")) {
            submitPlot(Parkour.getStatsManager().get(player));
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

        PlayerStats playerStats = Parkour.getStatsManager().get(player.getUniqueId().toString());

        if (checkConditions(playerStats))
        {
            if (!Parkour.getPlotsManager().exists(player.getName()))
            {
                Rank minimumRank = Parkour.getRanksManager().get(Parkour.getSettingsManager().minimum_rank_for_plot_creation);

                if (Parkour.getRanksManager().isPastOrAtRank(playerStats, minimumRank))
                    Parkour.getPlotsManager().createPlot(player);
                else
                    player.sendMessage(Utils.translate("&7You must be at least &c" + minimumRank.getTitle() + " &7to create a &aPlot"));
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou already have a plot"));
            }
        }
    }

    private void clearPlot(Plot targetPlot, boolean forceCleared, Player player) {

        if (targetPlot != null) {
            Parkour.getPlotsManager().clearPlot(targetPlot, false);
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

                Parkour.getPlotsManager().deletePlot(targetPlot);
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
        Player target = Bukkit.getPlayer(a[1]);

        if (target == null)
        {
            Plot plot = Parkour.getPlotsManager().get(player.getName());

            // if you have plot
            if (plot != null)
            {
                // if they are not a trusted player
                if (plot.isTrusted(target.getUniqueId().toString()))
                {
                    PlotsDB.removeTrustedPlayer(plot.getPlotID(), target);
                    plot.removeTrusted(target);

                    player.sendMessage(Utils.translate("&7You removed &3" + target.getName() + " &7from your plot!"));
                }
                else
                {
                    player.sendMessage(Utils.translate("&4" + player.getName() + " &cis not trusted in your plot"));
                }
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou do not have a plot"));
            }
        }
        else
        {
            player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online!"));
        }

    }

    private void trustPlayer(Player player, String[] a)
    {
        Player target = Bukkit.getPlayer(a[1]);

        if (target != null)
        {
            Plot plot = Parkour.getPlotsManager().get(player.getName());

            // if you have plot
            if (plot != null)
            {
                // if they are not a trusted player
                if (!plot.isTrusted(target.getUniqueId().toString()))
                {
                    PlotsDB.addTrustedPlayer(plot.getPlotID(), target);
                    plot.addTrusted(target);

                    player.sendMessage(Utils.translate("&7You trusted &3" + target.getName() + " &7to your plot!"));
                }
                else
                    player.sendMessage(Utils.translate("&4" + player.getName() + " &cis already trusted in your plot"));
            }
            else
            {
                player.sendMessage(Utils.translate("&cYou do not have a plot"));
            }
        }
        else
        {
            player.sendMessage(Utils.translate("&4" + a[1] + " &cis not online!"));
        }
    }

    private void visitPlot(Player player, String[] a) {
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (checkConditions(playerStats)) {

            String playerName = a[1];
            Plot targetPlot = Parkour.getPlotsManager().getIgnoreCase(playerName);
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
        Plot plot = Parkour.getPlotsManager().get(player.getName());

        // make sure they have a plot
        if (plot != null) {
            PlayerStats playerStats = Parkour.getStatsManager().get(player);
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
        Plot plot = Parkour.getPlotsManager().get(player.getName());
        // they have a plot
        if (plot != null) {
            // already submitted
            if (!plot.isSubmitted()) {

                // submit map!
                openMenu(playerStats, "submit-plot");
            } else {
                player.sendMessage(Utils.translate("&cYou have already submitted your plot!"));
            }
        } else {
            player.sendMessage(Utils.translate("&cYou do not have a plot to submit!"));
        }
    }

    private void plotInfo(Player player) {
        Plot plot = Parkour.getPlotsManager().getPlotInLocation(player.getLocation());

        if (plot != null)
            player.sendMessage(Utils.translate("&a" + plot.getOwnerName() + " &7owns this plot"));
        else
            player.sendMessage(Utils.translate("&cYou are not in any plot currently"));
    }

    private void resetPlayerLevelData(PlayerStats playerStats) {
        if (playerStats.getLevel() != null) {

            // save checkpoint if had one
            playerStats.resetCurrentCheckpoint();
            playerStats.resetLevel();
            PracticeHandler.resetDataOnly(playerStats);

            if (playerStats.isAttemptingRankup())
                Parkour.getRanksManager().leftRankup(playerStats);
        }
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
        }.runTaskLater(Parkour.getPlugin(), 20 * 30));
    }

    private void openMenu(PlayerStats playerStats, String menuName)
    {
        MenuManager menuManager = Parkour.getMenuManager();
        Player player = playerStats.getPlayer();

        if (menuManager.exists(menuName)) {

            Inventory inventory = menuManager.getInventory(playerStats, menuName, 1);

            if (inventory != null) {
                player.openInventory(inventory);
                menuManager.updateInventory(playerStats, player.getOpenInventory(), menuName, 1);
            } else {
                player.sendMessage(Utils.translate("&cError loading the inventory"));
            }
        } else {
            player.sendMessage(Utils.translate("&7'&c" + menuName + "&7' is not an existing menu"));
        }
    }

    private boolean checkConditions(PlayerStats playerStats) {
        Player player = playerStats.getPlayer();

        boolean passes = false;

        if (!playerStats.inRace()) {
            if (!playerStats.isInInfinite()) {
                if (!playerStats.isSpectating()) {
                    if (!playerStats.isEventParticipant()) {
                        if (!playerStats.isInBlackMarket()) {
                            if (!playerStats.inPracticeMode()) {
                                passes = true;
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while in practice mode"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while in the Black Market"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                }
            } else {
                player.sendMessage(Utils.translate("&cYou cannot do this while in Infinite Parkour"));
            }
        } else {
            player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
        }

        return passes;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(getHelp("create")); // console friendly
        sender.sendMessage(getHelp("delete"));
        sender.sendMessage(getHelp("clear"));
        sender.sendMessage(getHelp("home"));
        sender.sendMessage(getHelp("visit"));
        sender.sendMessage(getHelp("trust"));
        sender.sendMessage(getHelp("untrust"));
        sender.sendMessage(getHelp("submit"));

        // send admin commands if they have permission
        if (sender.hasPermission("rn-parkour.admin")) {

            sender.sendMessage(getHelp("accept"));
            sender.sendMessage(getHelp("deny"));
            sender.sendMessage(getHelp("list"));
            sender.sendMessage(getHelp("bypass"));
        }
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
            case "submit":
                return Utils.translate("&a/plot submit  &7Submit your plot");
            case "accept":
                return Utils.translate("&a/plot submit accept <player>  &7Accepts a subbmited plot");
            case "deny":
                return Utils.translate("&a/plot submit deny <player> (Reason...)  &7Deny a player with reason (10 word max)");
            case "list":
                return Utils.translate("&a/plot submit list  &7Open the submitted parkours GUI");
            case "help":
                return Utils.translate("&a/plot help  &7Sends this display");
            case "bypass":
                return Utils.translate("&a/plot bypass  &7Toggles Plot Bypassing");
        }
        return "";
    }
}
