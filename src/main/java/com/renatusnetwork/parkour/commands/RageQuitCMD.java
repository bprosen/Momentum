package com.renatusnetwork.parkour.commands;

import com.renatusnetwork.parkour.Parkour;
import com.renatusnetwork.parkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RageQuitCMD implements CommandExecutor {

    private HashMap<String, BukkitTask> confirmMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (a.length == 0) {
            // run rage quit here
            if (player.hasPermission("rn-parkour.ragequit")) {

                // find random message
                Random random = new Random();
                List<String> randomMessages = Parkour.getSettingsManager().rage_quit_messages;
                String randomMessage = Utils.translate(
                        randomMessages.get(random.nextInt(randomMessages.size()))
                        .replace("%player%", player.getDisplayName()));

                // build firework
                Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();

                meta.clearEffects();

                // build the firework and then set the new one
                FireworkEffect effect = FireworkEffect.builder()
                        .flicker(true)
                        .trail(true)
                        .with(FireworkEffect.Type.BURST)
                        .withColor(Color.WHITE)
                        .withFade(Color.RED)
                        .build();

                meta.addEffect(effect);
                firework.setFireworkMeta(meta);

                // detonate 0.5 later
                new BukkitRunnable() {
                    public void run() {
                        firework.detonate();
                    }
                }.runTaskLater(Parkour.getPlugin(), 10);

                // broadcast and kick
                Bukkit.broadcastMessage(randomMessage);
                player.kickPlayer("Rage Quit :(");

            // run confirm and buy
            } else {

                double price = Parkour.getSettingsManager().rage_quit_price;
                double balance = Parkour.getEconomy().getBalance(player);

                // can buy it
                if (balance >= price) {
                    // if they are in map, confirm it
                    if (confirmMap.containsKey(player.getName())) {
                        confirmMap.get(player.getName()).cancel();
                        confirmMap.remove(player.getName());

                        // remove price and give perm
                        Parkour.getEconomy().withdrawPlayer(player, price);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set rn-parkour.ragequit");
                        player.sendMessage(Utils.translate("&7You purchased &c/ragequit&7! Type it again to use it"));
                    } else {
                        player.sendMessage(Utils.translate("&7Are you sure you want to buy the &c/ragequit &7command?" +
                                " Type &c/ragequit &7again to buy it for &6$" + Utils.formatNumber(price)));

                        confirmMap.put(player.getName(), new BukkitRunnable() {
                            public void run() {
                                // ran out of time
                                if (confirmMap.containsKey(player.getName())) {

                                    player.sendMessage(Utils.translate("&7You ran out of time to buy the &c/ragequit &7command"));
                                    confirmMap.remove(player.getName());
                                }
                            }
                            // 30 seconds to buy it
                        }.runTaskLater(Parkour.getPlugin(), 20 * 30));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou do not have enough coins buy the &c/ragequit &7command" +
                            " &6($" + Utils.formatNumber(price - balance) + " more)"));
                }
            }
        }
        return false;
    }
}