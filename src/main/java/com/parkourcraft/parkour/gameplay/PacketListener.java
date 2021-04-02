package com.parkourcraft.parkour.gameplay;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.parkourcraft.parkour.Parkour;
import com.parkourcraft.parkour.data.plots.Plot;
import com.parkourcraft.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PacketListener implements Listener {

    public static void loadListeners(Plugin plugin) {
        registerBlockListener(plugin);
    }

    private static void registerBlockListener(Plugin plugin) {

        // block interact out of block event
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.BLOCK_DIG, PacketType.Play.Client.USE_ITEM) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                // use block change for right click, left click, etc

                if (packet.getType() == PacketType.Play.Client.BLOCK_DIG ||
                    packet.getType() == PacketType.Play.Client.USE_ITEM) {

                    Player player = event.getPlayer();
                    Plot plot = Parkour.getPlotsManager().get(player.getName());

                    // make sure they are in the right world
                    if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)) {

                        BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
                        Location loc = blockPosition.toVector().toLocation(
                                Bukkit.getWorld(Parkour.getSettingsManager().player_submitted_world));

                        // if it is a block place, adjust location to the block being placed, not what it is placed on
                        if (packet.getType() == PacketType.Play.Client.USE_ITEM) {
                            String direction = packet.getDirections().read(0).toString();

                            // adjust location
                            switch (direction) {
                                case "UP":
                                    loc.add(0, 1, 0);
                                    break;
                                case "DOWN":
                                    loc.subtract(0, 1, 0);
                                    break;
                                case "NORTH":
                                    loc.subtract(0, 0, 1);
                                    break;
                                case "EAST":
                                    loc.add(1, 0, 0);
                                    break;
                                case "SOUTH":
                                    loc.add(0, 0, 1);
                                    break;
                                case "WEST":
                                    loc.subtract(1, 0, 0);
                                    break;
                            }
                        }

                        boolean doCancel = false;
                        // check if they have a plot
                        if (plot != null) {
                            // check if the block is not in the plot
                            if (!blockInPlot(loc, plot))
                                doCancel = true;
                        } else
                            doCancel = true;

                        if (doCancel) {
                            player.sendMessage(Utils.translate("&cYou cannot do this here"));
                            event.setCancelled(true);
                            player.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
                        }
                    }
                }
            }
        });
    }

    private static boolean blockInPlot(Location loc, Plot plot) {

        int maxX = plot.getSpawnLoc().getBlockX() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int maxZ = plot.getSpawnLoc().getBlockZ() + (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minX = plot.getSpawnLoc().getBlockX() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);
        int minZ = plot.getSpawnLoc().getBlockZ() - (Parkour.getSettingsManager().player_submitted_plot_width / 2);

        if (loc.getBlockX() <= maxX && loc.getBlockX() >= minX && loc.getBlockZ() <= maxZ && loc.getBlockZ() >= minZ)
            return true;
        return false;
    }
}