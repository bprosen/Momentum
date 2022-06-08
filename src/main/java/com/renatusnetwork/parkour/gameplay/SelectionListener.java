package com.renatusnetwork.parkour.gameplay;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.plots.Plot;
import com.renatusnetwork.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class SelectionListener implements Listener {

    /*
        yeah... i know... it's pretty jank, but the listeners below stopped working (due to something with FAWE) :(
     */
    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage();

        // only continue if not opped and not bypassing
        if (player != null && !(player.isOp() && Parkour.getStatsManager().get(player).isBypassingPlots())) {

            // if in plot world and it is a worldedit command, continue
            if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world) &&
               (cmd.startsWith("//") || cmd.startsWith("/replacenear") || cmd.startsWith("/undo") || cmd.startsWith("/redo"))) {

                try {
                    // next get the region from player
                    LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
                    Region region = session.getSelection(session.getSelectionWorld());

                    // check if region is allowed, if not, cancel and notify
                    if (checkSelection(region.getMinimumPoint(), region.getMaximumPoint(), player)) {
                        event.setCancelled(true);

                        // send bypass info if opped
                        String messageToSend = "&cYou cannot do WorldEdit commands here!";
                        if (player.isOp())
                            messageToSend += " &7You can bypass this with &c/plot bypass";

                        player.sendMessage(Utils.translate(messageToSend));
                    }
                } catch (IncompleteRegionException e) {
                    // dont print stack track so we dont spam console with simple error
                }
            }
        }
    }

    /*
    // listen very early to be ahead of normal fawe
    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onSelection(EditSessionEvent event) {

        Actor actor = event.getActor();

        /
           there are 3 stages, so only listen to before anything happens (avoid 3 event fires),
           make sure the player is the person executing and the world is the plot world
         /
        if (event.getStage().toString().equalsIgnoreCase("BEFORE_CHANGE") && actor != null && actor.isPlayer() &&
            event.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            // get bukkit player from name
            Player player = Bukkit.getPlayer(actor.getName());

            // only continue if not opped
            if (player != null) {
                // if opped and bypassing plots, return
                if (player.isOp() && Parkour.getStatsManager().get(player).isBypassingPlots())
                    return;

                // try catch for incomplete regions
                try {
                    // get their session, the region from that selection, and the min/max points
                    LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
                    Region region = session.getSelection(event.getWorld());

                    if (checkSelection(region.getMinimumPoint(), region.getMaximumPoint(), player)) {
                        // use FAWE api to cancel and send message
                        event.setCancelled(true);

                        // send bypass info if opped
                        String messageToSend = "&cThis WorldEdit selection is out of where you can build";
                        if (player.isOp())
                            messageToSend += " &7You can bypass this with &c/plot bypass";

                        player.sendMessage(Utils.translate(messageToSend));
                    }
                } catch (IncompleteRegionException e) {
                    // dont print stack track so we dont spam console with simple error
                }
            }
        }
    }

    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onPaste(PasteEvent event) {

        com.sk89q.worldedit.entity.Player wePlayer = event.getPlayer();
        Player player = Bukkit.getPlayer(wePlayer.getName());

        if (player != null &&
            player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            // if opped and bypassing plots, return
            if (player.isOp() && Parkour.getStatsManager().get(player).isBypassingPlots())
                return;

            try {
                // get session and clipboard holder of session
                LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
                ClipboardHolder holder = session.getClipboard();

                /
                    this is a complicated part because the location of
                    the min/max of the new paste is not stored, so we have to do hacky math

                    how it is done
                    1) first we get the offset of the clipboard by subtracting the minimum point of the current clipboard
                       by the origin
                    2) we then apply that offset to the holder's transform (rotation, position etc) and add that
                       to the position (gives us the new max)
                    3) finally, we apply the max clipboard point subtracted by the min and then apply that to the transform,
                       and add that to the max point, giving us the min
                 /
                Vector clipboardOffset = event.getClipboard().getMinimumPoint().subtract(event.getClipboard().getOrigin());
                Vector max = event.getPosition().add(holder.getTransform().apply(clipboardOffset));
                Vector min = max.add(holder.getTransform()
                                    .apply(event.getClipboard().getMaximumPoint()
                                    .subtract(event.getClipboard().getMinimumPoint())));

                if (checkSelection(min, max, player)) {
                    event.setCancelled(true);

                    // send bypass info if opped
                    String messageToSend = "&cYour paste selection is out of where you can build";
                    if (player.isOp())
                        messageToSend += " &7You can bypass this with &c/plot bypass";

                    player.sendMessage(Utils.translate(messageToSend));
                }
            } catch (EmptyClipboardException e) {
                // dont print stack track so we dont spam console with simple error
            }
        }
    }*/


    // method to check if a selection will pass if they are in their plot or a trusted plot
    private boolean checkSelection(Vector min, Vector max, Player player) {
        boolean selectionPasses = false;

        // get world
        World plotWorld = Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world);

        // get plots in min and max locations
        Plot minPlot = Parkour.getPlotsManager().getPlotInLocation(new Location(plotWorld, min.getX(), min.getY(), min.getZ()));
        Plot maxPlot = Parkour.getPlotsManager().getPlotInLocation(new Location(plotWorld, max.getX(), max.getY(), max.getZ()));

        /*
            this logic is a bit complicated in a couple ways:

            1) we have to make sure they did not set a pos in a buffer width between plots (would return null)
            2) if it is not a road, then it is fine to use the plot to see if they are trusted or own it,
               if either are false, then it would cancel
            3) finally, we have to see if they are not the same plots as the player could be trusted in the plot
               of one of their pos and another pos in their own plot, therefore the selection goes over the road
               and across multiple plots
         */
        boolean cantInMin = minPlot == null || !minPlot.canBuild(player.getName());
        boolean cantInMax = maxPlot == null || !maxPlot.canBuild(player.getName());
        boolean notSameOwner = ((minPlot != null && maxPlot != null) &&
                !minPlot.getOwnerName().equalsIgnoreCase(maxPlot.getOwnerName()));

        if (cantInMin || cantInMax || notSameOwner)
            selectionPasses = true;

        return selectionPasses;
    }
}
