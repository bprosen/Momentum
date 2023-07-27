package com.renatusnetwork.parkour.data.bank.types;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.levels.Level;
import com.renatusnetwork.parkour.utils.Time;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;

public class Jackpot
{
    private Level level;
    private int bonus;
    private HashSet<String> completed;
    private long endMillis;

    private BukkitTask reminderTask;

    public Jackpot(Level level, int bonus)
    {
        this.level = level;
        this.bonus = bonus;
        this.endMillis = System.currentTimeMillis() + 1800000; // 30 mins

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
        Bukkit.broadcastMessage(Utils.translate(" &6&lJACKPOT &e&lENDED"));

        // grammar craziness
        String playersString = "player";
        int playerCount = completed.size();

        if (playerCount > 1)
            playersString += "s";

        Bukkit.broadcastMessage(Utils.translate(" &a" + Utils.formatNumber(playerCount) + " &7" + playersString + " completed the " + level.getFormattedTitle() + " &7level"));
        Bukkit.broadcastMessage(Utils.translate(" &6" + Utils.formatNumber(completed.size() * (level.getReward() + bonus)) + " &eCoins &7were rewarded"));
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
                Bukkit.broadcastMessage(Utils.translate(" &e&lBANK &6&lJACKPOT &e&lALERT"));
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(Utils.translate(" &7Complete &2" + level.getFormattedTitle() + " &7for &6" + Utils.formatNumber(bonus) + " &d&lBONUS &e&lCOINS"));
                broadcastJoinComponent();
                Bukkit.broadcastMessage(Utils.translate(" &7There are &a" + (Math.round(millisLeft() / 1000f / 60f) + " minutes &7left to get the reward")));
                Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
            }
        }.runTaskTimer(Parkour.getPlugin(), 1, 20 * 60 * 3); // every 3 minutes, one tick offset to cancel in time
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

    public void broadcastCompletion(Player player)
    {
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
        Bukkit.broadcastMessage(Utils.translate(" &6&l" + player.getDisplayName() + " &e&lCOMPLETED THE &6&lJACKPOT"));
        broadcastJoinComponent();
        Bukkit.broadcastMessage(Utils.translate("&2&m----------------------------------------"));
    }

    private void broadcastJoinComponent()
    {
        Utils.broadcastClickableHoverableCMD(" &7Type &a/jackpot play&7 or &aclick here&7 to join in", "&aClick me to play the &6&lJACKPOT", "/jackpot play");
    }

    private long millisLeft()
    {
        return endMillis - System.currentTimeMillis();
    }
}
