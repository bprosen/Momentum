package com.renatusnetwork.momentum.data.jackpot;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class JackpotManager {
    private Jackpot currentJackpot;

    public JackpotManager() {
        runScheduler();
    }

    private void runScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ThreadLocalRandom.current().nextInt(0, 10) == 0) { // 10% chance every 6 hours
                    startJackpot();
                }
            }
        }.runTaskTimerAsynchronously(Momentum.getPlugin(), 20 * 21600, 20 * 21600);
    }

    public void startJackpot() {
        if (currentJackpot == null) {
            ArrayList<Level> tempList = new ArrayList<>();

            for (Level level : Momentum.getLevelManager().getLevelsInAllMenus())
            // only allow levels with reward > 0 and <= 5000
            {
                if (level.getRequiredLevels().isEmpty() && !level.isFeaturedLevel() && !level.hasPermissionNode() && !level.isRankUpLevel() && !level.isAscendance() && level.hasReward() && level.getReward() <= 5000) {
                    tempList.add(level);
                }
            }

            Level level = tempList.get(new Random().nextInt(tempList.size()));

            int low = Momentum.getSettingsManager().jackpot_bonus_low_random_bound;
            int high = Momentum.getSettingsManager().jackpot_bonus_high_random_bound;
            int bonus = level.getReward() * ThreadLocalRandom.current().nextInt(low, high + 1);

            currentJackpot = new Jackpot(level, bonus);
            currentJackpot.start(); // begin jackpot
            Utils.playSound(Sound.BLOCK_NOTE_PLING);
        } else {
            Momentum.getPluginLogger().info("Tried to start Jackpot with one already running");
        }
    }

    public boolean isJackpotRunning() {
        return currentJackpot != null;
    }

    public Jackpot getJackpot() {
        return currentJackpot;
    }

    public void chooseJackpot(Level level, int bonus) {
        if (currentJackpot == null) {
            currentJackpot = new Jackpot(level, bonus);
            currentJackpot.start();
        } else {
            Momentum.getPluginLogger().info("Tried to choose Jackpot with one already running");
        }
    }

    public void endJackpot() {
        if (currentJackpot != null) {
            currentJackpot.end();
            currentJackpot = null;
        } else {
            Momentum.getPluginLogger().info("Tried to end Jackpot with none currently running");
        }
    }
}
