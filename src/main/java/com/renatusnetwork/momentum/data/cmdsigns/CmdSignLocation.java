package com.renatusnetwork.momentum.data.cmdsigns;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class CmdSignLocation {

    private final World world;
    private final int x;
    private final int y;
    private final int z;

    public CmdSignLocation(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CmdSignLocation(Location location) {
        this.world = location.getWorld();
        this.x = (int) location.getX();
        this.y = (int) location.getY();
        this.z = (int) location.getZ();
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CmdSignLocation)) {
            return false;
        }

        CmdSignLocation l = (CmdSignLocation) other;
        return l.getWorld().getName().equals(this.world.getName()) && l.getX() == this.x && l.getY() == this.y && l.getZ() == this.z;
    }

    @Override
    public String toString() {
        return String.format("%s(%d,%d,%d)", world.getName(), x, y, z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world.getName(), x, y, z);
    }
}