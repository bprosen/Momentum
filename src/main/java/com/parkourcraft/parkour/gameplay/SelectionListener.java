package com.parkourcraft.parkour.gameplay;

import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.plots.PlotsManager;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SelectionListener {

    // listen very early to be ahead of normal fawe
    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onSelection(EditSessionEvent event) throws Exception {

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
            }
        }
    }
}
