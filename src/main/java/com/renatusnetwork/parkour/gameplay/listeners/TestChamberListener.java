package com.renatusnetwork.parkour.gameplay.listeners;


import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class TestChamberListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        PlayerStats playerStats = Parkour.getStatsManager().get(player);

        if (playerStats != null && playerStats.inLevel() && playerStats.getLevel().isTCLevel())
        {
            Location location = player.getLocation();

            if (location.getBlock().getRelative(BlockFace.WEST).getType() == Material.BRICK ||
                    location.getBlock().getRelative(BlockFace.EAST).getType() == Material.BRICK ||
                    location.getBlock().getRelative(BlockFace.NORTH).getType() == Material.BRICK ||
                    location.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.BRICK)
            {
                player.setVelocity(location.getDirection().multiply(0.7D));
                player.playSound(location, Sound.BLOCK_WOOD_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.WEST).getType() == Material.DIAMOND_ORE)
            {
                player.setVelocity(location.getDirection().setX(0.9D).setY(0.7D).setZ(0));
                player.playSound(location, Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.EAST).getType() == Material.DIAMOND_ORE)
            {
                player.setVelocity(location.getDirection().setX(-0.9D).setY(0.7D).setZ(0));
                player.playSound(location, Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.NORTH).getType() == Material.DIAMOND_ORE)
            {
                player.setVelocity(location.getDirection().setX(0).setY(0.7D).setZ(0.9D));
                player.playSound(location, Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.DIAMOND_ORE)
            {
                player.setVelocity(location.getDirection().setX(0).setY(0.7D).setZ(-0.9D));
                player.playSound(location, Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.WEST).getType() == Material.EMERALD_ORE ||
                    location.getBlock().getRelative(BlockFace.EAST).getType() == Material.EMERALD_ORE ||
                    location.getBlock().getRelative(BlockFace.NORTH).getType() == Material.EMERALD_ORE ||
                    location.getBlock().getRelative(BlockFace.SOUTH).getType() == Material.EMERALD_ORE)
            {
                player.setVelocity(location.getDirection().setY(-1).multiply(-0.7D));
                player.playSound(location, Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.SPONGE)
            {
                player.setVelocity(new Vector(player.getVelocity().getX(), 1.5D, 0.0D));
                player.playSound(location, Sound.ENTITY_FIREWORK_LARGE_BLAST, 10.0F, 20.0F);
            }
            else if (location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)
            {
                player.setVelocity(location.getDirection().setY(0.2D).multiply(6));
                player.playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 10.0F, 20.0F);
            }
        }
    }
}