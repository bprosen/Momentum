package com.parkourcraft.parkour.gameplay;


import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class TestChamberHandler implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();

        if (p.getLocation().getBlock().getRelative(BlockFace.WEST).getType() == Material.BRICK ||
            p.getLocation().getBlock().getRelative(BlockFace.EAST).getType() == Material.BRICK ||
            p.getLocation().getBlock().getRelative(BlockFace.NORTH).getType() == Material.BRICK ||
            p.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType() == Material.BRICK) {

            p.setVelocity(p.getLocation().getDirection().multiply(0.7D));
            p.playSound(p.getLocation(), Sound.BLOCK_WOOD_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.WEST).getType() == Material.DIAMOND_ORE) {
            Vector vector = p.getLocation().getDirection().setX(0.9D).setY(0.7D).setZ(0);
            p.setVelocity(vector);
            p.playSound(p.getLocation(), Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.EAST).getType() == Material.DIAMOND_ORE) {
            Vector vector = p.getLocation().getDirection().setX(-0.9D).setY(0.7D).setZ(0);
            p.setVelocity(vector);
            p.playSound(p.getLocation(), Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.NORTH).getType() == Material.DIAMOND_ORE) {
            Vector vector = p.getLocation().getDirection().setX(0).setY(0.7D).setZ(0.9D);
            p.setVelocity(vector);
            p.playSound(p.getLocation(), Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType() == Material.DIAMOND_ORE) {
            Vector vector = p.getLocation().getDirection().setX(0).setY(0.7D).setZ(-0.9D);
            p.setVelocity(vector);
            p.playSound(p.getLocation(), Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.WEST).getType() == Material.EMERALD_ORE ||
                   p.getLocation().getBlock().getRelative(BlockFace.EAST).getType() == Material.EMERALD_ORE ||
                   p.getLocation().getBlock().getRelative(BlockFace.NORTH).getType() == Material.EMERALD_ORE ||
                   p.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType() == Material.EMERALD_ORE) {

            p.setVelocity(p.getLocation().getDirection().setY(-1).multiply(-0.7D));
            p.playSound(p.getLocation(), Sound.BLOCK_SLIME_STEP, 1.0F, 1.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SPONGE) {
            p.setVelocity(new Vector(p.getVelocity().getX(), 1.5D, 0.0D));
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_LARGE_BLAST, 10.0F, 20.0F);

        } else if (p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK) {
            p.setVelocity(p.getLocation().getDirection().setY(0.2D).multiply(6));
            p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 10.0F, 20.0F);
        }
    }
}