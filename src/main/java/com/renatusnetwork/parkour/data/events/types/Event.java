package com.renatusnetwork.parkour.data.events.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.events.EventManager;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.data.levels.LevelsYAML;
import com.renatusnetwork.parkour.utils.Utils;
import com.renatusnetwork.parkour.utils.dependencies.WorldGuard;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Event {

    private Level level;
    private ProtectedRegion region;
    private String formattedName;

    public Event(Level level, String formattedName)
    {
        this.formattedName = formattedName;
        this.level = level;
        this.region = WorldGuard.getRegion(level.getStartLocation());
    }

    public abstract void end();

    public String getFormattedName()
    {
        return formattedName;
    }
    public Level getLevel() {
        return level;
    }

    public ProtectedRegion getRegion() { return region; }
}

