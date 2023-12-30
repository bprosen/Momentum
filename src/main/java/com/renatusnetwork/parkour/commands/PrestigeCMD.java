package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.data.ranks.RanksManager;
import com.renatusnetwork.parkour.data.stats.PlayerStats;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class PrestigeCMD implements CommandExecutor {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        PlayerStats playerStats = Parkour.getStatsManager().get(player);
        RanksManager rankManager = Parkour.getRanksManager();

        if (a.length == 0) {
            // this means they are max rank
            if (playerStats.getRank().isMaxRank())
            {
                int prestiges = playerStats.getPrestiges();
                double cost = Parkour.getSettingsManager().base_prestige_cost + (prestiges * Parkour.getSettingsManager().additional_cost_per_prestige);

                if (!confirmMap.containsKey(player.getName()))
                {
                    // otherwise, put them in and ask them to confirm within 5 seconds
                    player.sendMessage("");
                    player.sendMessage(Utils.translate("&cAre you sure you want to prestige? This will reset you to rank &f" + Parkour.getSettingsManager().default_rank));
                    player.sendMessage(Utils.translate("&cIt will cost you &6" + Utils.formatNumber(cost) + " &eCoins"));
                    player.sendMessage(Utils.translate("&cType &4/prestige &cagain to confirm"));
                    player.sendMessage("");

                    confirmMap.put(player.getName(), new BukkitRunnable() {
                        public void run() {
                            if (confirmMap.containsKey(player.getName())) {
                                confirmMap.remove(player.getName());
                                player.sendMessage(Utils.translate("&cYou did not confirm in time"));
                            }
                        }
                    }.runTaskLater(Parkour.getPlugin(), 20 * 10));
                }
                else if (playerStats.getCoins() >= cost)
                {
                    rankManager.doPrestige(playerStats, cost);
                    cancelTask(player);
                }
                else
                {
                    player.sendMessage(Utils.translate("&cYou do not have &6" + Utils.formatNumber(cost) + " &eCoins&c!" +
                            " You need &6" + Utils.formatNumber(cost - playerStats.getCoins()) + " &cmore &eCoins"));
                    cancelTask(player);
                }
            } else
                player.sendMessage(Utils.translate("&cYou cannot do this yet!" +
                                                        " You need to be Rank &4" + rankManager.getMaxRank().getTitle()));
        }
        return false;
    }

    private void cancelTask(Player player)
    {
        confirmMap.get(player.getName()).cancel();
        confirmMap.remove(player.getName());
    }
}
