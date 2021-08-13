package com.parkourcraft.parkour.gameplay;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.config.BBC;
import com.boydti.fawe.object.extent.NullExtent;
import com.boydti.fawe.wrappers.PlayerWrapper;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.data.plots.PlotsManager;
import com.parkourcraft.parkour.utils.Utils;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class SelectionListener {

    @Subscribe (priority = EventHandler.Priority.VERY_EARLY)
    public void onSelection(EditSessionEvent event) throws Exception {

        Actor actor = event.getActor();

        if (actor != null && actor.isPlayer() &&
            event.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

            Player player = Bukkit.getPlayer(actor.getName());

            if (player != null && !player.isOp()) {

                LocalSession session = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
                Region region = session.getSelection(event.getWorld());
                Vector maxPoint = region.getMaximumPoint();
                Vector minPoint = region.getMinimumPoint();

                PlotsManager plotsManager = Parkour.getPlotsManager();
                World world = Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world);

                Plot plotInMin = plotsManager.getPlotInLocation(new Location(world, minPoint.getX(), minPoint.getY(), minPoint.getZ()));
                Plot plotInMax = plotsManager.getPlotInLocation(new Location(world, maxPoint.getX(), maxPoint.getY(), maxPoint.getZ()));

                boolean cantInMin = plotInMin == null || !plotInMin.canBuild(player.getName());
                boolean cantInMax = plotInMax == null || !plotInMax.canBuild(player.getName());

                if (cantInMin || cantInMax || ((plotInMin != null && plotInMax != null) && !plotInMin.getOwnerName().equalsIgnoreCase(plotInMax.getOwnerName()))) {
                    try {
                        Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                        field.setAccessible(true);
                        Object currentExtent = field.get(event.getExtent());

                        if (!(currentExtent instanceof NullExtent)) {
                            field.set(event.getExtent(), new NullExtent());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    throw new CommandException("Outside Plot Region");
                }
            }
        }
    }
}
