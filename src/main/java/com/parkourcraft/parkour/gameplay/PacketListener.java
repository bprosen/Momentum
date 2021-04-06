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

                    // make sure they are in the right world and not opped
                    if (player.getWorld().getName().equalsIgnoreCase(Parkour.getSettingsManager().player_submitted_world)
                        && !player.isOp()) {

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

                        // get nearest plot from location
                        Plot plot = Parkour.getPlotsManager().getNearestPlot(loc);

                        boolean doCancel = false;
                        String reason = "";

                        // check if they have a plot,
                        // only way this does not get cancelled is if they are trusted or own it
                        if (plot != null) {
                            // check if their plot is submitted and they own the plot
                            if (plot.getOwnerName().equalsIgnoreCase(player.getName())) {
                                // if they have submitted it, cancel it
                                if (plot.isSubmitted()) {
                                    doCancel = true;
                                    reason = "&cYou cannot edit your plot when it has been submitted";
                                }
                            // check if they are not trusted, then cancel
                            } else if (!plot.getTrustedPlayers().contains(player.getName())) {
                                doCancel = true;
                                reason = "&cYou cannot do this here";
                            }
                        // no nearest plot
                        } else {
                            doCancel = true;
                            reason = "&cYou cannot do this here";
                        }

                        if (doCancel) {
                            player.sendMessage(Utils.translate(reason));
                            event.setCancelled(true);
                            // send block update back to player from server that intercepted the packet
                            player.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
                        }
                    }
                }
            }
        });
    }
}