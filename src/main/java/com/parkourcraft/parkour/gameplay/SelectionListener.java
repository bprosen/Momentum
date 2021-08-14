package com.parkourcraft.parkour.gameplay;

import com.boydti.fawe.object.FawePlayer;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.plots.PlotsManager;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.event.extent.PasteEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SelectionListener {

    // listen very early to be ahead of normal fawe
    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onSelection(EditSessionEvent event) {

        Actor actor = event.getActor();

        /*
           there are 3 stages, so only listen to before anything happens (avoid 3 event fires),
           make sure the player is the person executing and the world is the plot world
         */
        if (event.getStage().toString().equalsIgnoreCase("BEFORE_CHANGE") && actor != null && actor.isPlayer() &&
            event.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            // get bukkit player from name
            Player player = Bukkit.getPlayer(actor.getName());

            // only continue if not opped
            if (player != null && !player.isOp()) {
                // try catch for incomplete regions
                try {
                    // get their session, the region from that selection, and the min/max points
                    LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
                    Region region = session.getSelection(event.getWorld());
                    Vector maxPoint = region.getMaximumPoint();
                    Vector minPoint = region.getMinimumPoint();

                    PlotsManager plotsManager = Parkour.getPlotsManager();
                    World world = Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world);

                    // get plot from min and max locations
                    Plot plotInMin = plotsManager.getPlotInLocation(new Location(world, minPoint.getX(), minPoint.getY(), minPoint.getZ()));
                    Plot plotInMax = plotsManager.getPlotInLocation(new Location(world, maxPoint.getX(), maxPoint.getY(), maxPoint.getZ()));

                /*
                    this logic is a bit complicated in a couple ways:

                    1) we have to make sure they did not set a pos in a buffer width between plots (would return null)
                    2) if it is not a road, then it is fine to use the plot to see if they are trusted or own it,
                       if either are false, then it would cancel
                    3) finally, we have to see if they are not the same plots as the player could be trusted in the plot
                       of one of their pos and another pos in their own plot, therefore the selection goes over the road
                       and across multiple plots
                 */
                    boolean cantInMin = plotInMin == null || !plotInMin.canBuild(player.getName());
                    boolean cantInMax = plotInMax == null || !plotInMax.canBuild(player.getName());

                    if (cantInMin || cantInMax || ((plotInMin != null && plotInMax != null) && !plotInMin.getOwnerName().equalsIgnoreCase(plotInMax.getOwnerName()))) {
                        // use FAWE api to cancel and send message
                        event.setCancelled(true);
                        player.sendMessage(Utils.translate("&cThis WorldEdit selection is out of where you can build"));
                    }
                } catch (IncompleteRegionException e) {
                    // dont print stack track so we dont spam console with simple error
                }
            }
        }
    }

    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onPaste(PasteEvent event) throws EmptyClipboardException {

        com.sk89q.worldedit.entity.Player wePlayer = event.getPlayer();
        Player player = Bukkit.getPlayer(wePlayer.getName());

        if (player != null && !player.isOp() &&
            player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
            ClipboardHolder holder = session.getClipboard();

            /*AffineTransform transform = new AffineTransform();
            transform = transform.rotateY(-(yRotate != null ? yRotate : 0));
            transform = transform.rotateX(-(xRotate != null ? xRotate : 0));
            transform = transform.rotateZ(-(zRotate != null ? zRotate : 0));

            holder.setTransform(holder.getTransform().combine(transform));
            Vector min = event.getClipboard().getMinimumPoint();
            Vector diff = min.subtract(event.getClipboard().getOrigin());
            Vector pastedMin = event.getPosition().add(diff);
            Vector pastedMax = pastedMin.add(event.getClipboard().getMaximumPoint().subtract(event.getClipboard().getMinimumPoint()));*/

            // TODO: actually make this adjust for rotations, so far ive tried practically everything
            // https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/main/worldedit-core/src/main/java/com/sk89q/worldedit/command/ClipboardCommands.java#L529-L539
            // code from here ^

            Vector clipboardOffset = event.getClipboard().getRegion().getMinimumPoint().subtract(event.getClipboard().getOrigin());
            Vector realTo = event.getPosition().add(holder.getTransform().apply(clipboardOffset));
            Vector max = realTo.add(holder
                    .getTransform()
                    .apply(event.getClipboard().getRegion().getMaximumPoint().subtract(event.getClipboard().getMinimumPoint())));
            RegionSelector selector = new CuboidRegionSelector(wePlayer.getWorld(), event.getPosition().toBlockPoint(), max.toBlockPoint());
            session.setRegionSelector(wePlayer.getWorld(), selector);
            selector.learnChanges();
            selector.explainRegionAdjust(wePlayer, session);

            //Bukkit.broadcastMessage(session.getClipboard().getClipboard().getMinimumPoint() + " -> " + session.getClipboard().getClipboard().getMaximumPoint());
        }
    }
}
