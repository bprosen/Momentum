package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;

public class Jackpot
{
    private Level level;
    private int bonus;
    private HashSet<String> completed;

    private BukkitTask reminderTask;

    public Jackpot(Level level, int bonus)
    {
        this.level = level;
        this.bonus = bonus;

        completed = new HashSet<>();
    }

    public void start()
    {
        runSchedulers();
    }

    public void end()
    {
        // cancel reminder
        if (reminderTask != null)
            reminderTask.cancel();

        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &a&lJACKPOT ENDED "));
        Bukkit.broadcastMessage(Utils.translate(" &a" + Utils.formatNumber(completed.size()) + " &7completed the " + level.getFormattedTitle() + " level!"));
        Bukkit.broadcastMessage(Utils.translate(" &6" + Utils.formatNumber(completed.size() * (level.getReward() + bonus)) + " &eCoins &7were rewarded!"));
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
    }

    private void runSchedulers()
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                end();
            }
        }.runTaskLater(Parkour.getPlugin(), 20 * Parkour.getSettingsManager().jackpotLength);

        reminderTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
                Bukkit.broadcastMessage(Utils.translate(" &a&lBANK JACKPOT ALERT "));
                Bukkit.broadcastMessage(Utils.translate(" &aComplete &2" + level.getFormattedTitle() + " &afor &6" + Utils.formatNumber(bonus) + " &2&lBONUS &eCOINS"));
                Utils.broadcastClickableHoverableCMD(" &7Type &2'/jackpot play' or click here to join in!", "&aClick me to play!", "/jackpot play");
                Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
            }
        }.runTaskTimer(Parkour.getPlugin(), 0, 20 * 60 * 3); // every 3 minutes
    }
    public void addCompleted(String playerName)
    {
        completed.add(playerName);
    }

    public boolean hasCompleted(String playerName)
    {
        return completed.contains(playerName);
    }

    public String getLevelName()
    {
        return level.getName();
    }

    public Level getLevel() { return level; }

    public int getBonus()
    {
        return bonus;
    }

    public void broadcastCompletion(String playerName)
    {
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &a&l" + playerName + " COMPLETED THE JACKPOT"));
        Utils.broadcastClickableHoverableCMD(" &7Type &2'/jackpot play' or click here &7to join in!", "&aClick me to play!", "/jackpot play");
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
    }
}
