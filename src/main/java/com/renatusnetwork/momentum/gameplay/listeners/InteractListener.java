package com.renatusnetwork.momentum.gameplay.listeners;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.cmdsigns.CmdSignLocation;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSign;
import com.renatusnetwork.momentum.data.cmdsigns.CommandSignManager;
import com.renatusnetwork.momentum.data.levels.Level;
import com.renatusnetwork.momentum.data.races.gamemode.RaceEndReason;
import com.renatusnetwork.momentum.data.stats.PlayerStats;
import com.renatusnetwork.momentum.data.stats.StatsManager;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Openable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;

public class InteractListener implements Listener {

    private HashMap<String, BukkitTask> resetConfirmMap = new HashMap<>();
    private HashMap<String, BukkitTask> spawnConfirmMap = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.HAND &&
                (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            // this is in case they try to click a trapdoor, door or something openable if they're in spectator
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    event.getClickedBlock() != null &&
                    event.getClickedBlock().getState().getData() instanceof Openable &&
                    Momentum.getStatsManager().get(player).isSpectating()) {
                event.setCancelled(true);
                return;
            }

            ItemStack item = event.getItem();
            StatsManager statsManager = Momentum.getStatsManager();

            if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null)
                return;

            if (item.getItemMeta().getDisplayName().startsWith(Utils.translate("&7Players »"))) {
                event.setCancelled(true);

                if (statsManager.containsHiddenPlayer(player))
                    statsManager.togglePlayerHiderOff(player, player.getInventory().getHeldItemSlot(), true);
                else
                    statsManager.togglePlayerHiderOn(player, player.getInventory().getHeldItemSlot(), true);
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&eLast Checkpoint"))) {
                event.setCancelled(true);
                PlayerStats playerStats = Momentum.getStatsManager().get(player);
                Momentum.getCheckpointManager().teleportToCheckpoint(playerStats);
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&aYour Profile"))) {
                event.setCancelled(true);
                PlayerStats playerStats = Momentum.getStatsManager().get(player);

                if (playerStats != null && playerStats.isLoaded())
                    Momentum.getMenuManager().openInventory(playerStats, "profile", true);
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate("&cReset"))) {
                event.setCancelled(true);
                PlayerStats playerStats = Momentum.getStatsManager().get(player);
                Level level = playerStats.getLevel();

                if (!playerStats.isInTutorial()) {
                    if (!playerStats.inRace()) {
                        if (!playerStats.isEventParticipant()) {
                            if (!playerStats.isSpectating()) {
                                if (!playerStats.isPreviewingLevel()) {
                                    if (level != null) {
                                        if (level.getStartLocation() != null) {
                                            // gets if they have right clicked it already, if so, cancel the task and reset them
                                            if (!resetConfirmMap.containsKey(player.getName())) {
                                                // otherwise, put them in and ask them to confirm within 5 seconds
                                                player.sendMessage(Utils.translate("&cAre you sure you want to reset? Right click again to confirm"));

                                                resetConfirmMap.put(player.getName(), new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        if (resetConfirmMap.containsKey(player.getName())) {
                                                            resetConfirmMap.remove(player.getName());
                                                            player.sendMessage(Utils.translate("&cYou did not confirm in time"));
                                                        }
                                                    }
                                                }.runTaskLater(Momentum.getPlugin(), 20 * 5));
                                            } else {
                                                resetConfirmMap.get(player.getName()).cancel();

                                                Momentum.getCheckpointManager().deleteCheckpoint(playerStats, level);

                                                statsManager.resetPracticeDataOnly(playerStats);
                                                playerStats.disableLevelStartTime();

                                                if (level.hasPotionEffects()) {

                                                    playerStats.clearPotionEffects();

                                                    // if has nv status, add nv
                                                    if (playerStats.hasNightVision())
                                                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));

                                                    for (PotionEffect potionEffect : level.getPotionEffects())
                                                        if (playerStats.hasNightVision() || potionEffect.getType() != PotionEffectType.NIGHT_VISION)
                                                            player.addPotionEffect(potionEffect);
                                                }
                                                resetConfirmMap.remove(player.getName());

                                                playerStats.teleport(level.getStartLocation(), true);
                                                playerStats.resetFails();
                                                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 0.5f, 1f);
                                            }
                                        } else {
                                            player.sendMessage(Utils.translate("&cYou cannot reset a level with no start location"));
                                        }
                                    } else {
                                        player.sendMessage(Utils.translate("&cYou are not in a level"));
                                    }
                                } else
                                    player.sendMessage(Utils.translate("&cYou cannot do this while previewing a level"));
                            } else {
                                player.sendMessage(Utils.translate("&cYou cannot do this while spectating"));
                            }
                        } else {
                            player.sendMessage(Utils.translate("&cYou cannot do this while in an event"));
                        }
                    } else {
                        player.sendMessage(Utils.translate("&cYou cannot do this while in a race"));
                    }
                } else {
                    player.sendMessage(Utils.translate("&cYou cannot do this while in the tutorial"));
                }
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(Momentum.getSettingsManager().prac_title))) {
                event.setCancelled(true);
                Momentum.getCheckpointManager().teleportToPracticeCheckpoint(Momentum.getStatsManager().get(player));
            } else if (event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(Utils.translate(Momentum.getSettingsManager().leave_title))) {
                if (Momentum.getStatsManager().get(player).isLoaded()) {
                    event.setCancelled(true);

                    if (!spawnConfirmMap.containsKey(player.getName())) {
                        // otherwise, put them in and ask them to confirm within 5 seconds
                        player.sendMessage(Utils.translate("&cAre you sure you want to leave? Right click again to confirm"));
                        spawnConfirmMap.put(player.getName(), new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (spawnConfirmMap.containsKey(player.getName())) {
                                    spawnConfirmMap.remove(player.getName());
                                    player.sendMessage(Utils.translate("&cYou did not confirm in time"));
                                }
                            }
                        }.runTaskLater(Momentum.getPlugin(), 20 * 5));
                    } else {
                        spawnConfirmMap.get(player.getName()).cancel();
                        spawnConfirmMap.remove(player.getName());

                        PlayerStats playerStats = Momentum.getStatsManager().get(player);

                        if (playerStats.inRace()) {
                            String forfeitMessage = "&cYou forfeit the race, giving a loss, taking elo";

                            if (playerStats.getRace().hasBet())
                                forfeitMessage += " and not returning the &6" + Utils.formatNumber(playerStats.getRace().getBet()) + " &eCoins &cbet";

                            playerStats.sendMessage(Utils.translate(forfeitMessage));
                            playerStats.endRace(playerStats.getRace().getOpponent(), RaceEndReason.FORFEIT);
                        } else
                            Momentum.getLocationManager().teleportToSpawn(playerStats, player);
                    }
                } else
                    player.sendMessage(Utils.translate("&cYou cannot do this while loading your stats"));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignClick(PlayerInteractEvent event) {
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                && event.getClickedBlock().getType().equals(Material.WALL_SIGN)
                && !event.getClickedBlock().getWorld().getName().equalsIgnoreCase(Momentum.getSettingsManager().player_submitted_world)) {
            // return if gamemode 0, opped and left clicked
            Player player = event.getPlayer();
            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE && event.getAction().equals(Action.LEFT_CLICK_BLOCK))
                return;

            CommandSignManager csignManager = Momentum.getCommandSignManager();
            PlayerStats playerStats = Momentum.getStatsManager().get(player);
            CommandSign csign = csignManager.getCommandSign(new CmdSignLocation(event.getClickedBlock().getLocation()));
            if (csign != null) {
                if (!csign.hasUsed(playerStats.getUUID())) {
                    csignManager.obtainCommandSign(playerStats.getUUID(), csign.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), csign.getCommand().replaceAll("%player%", playerStats.getName()));
                } else
                    player.sendMessage("&cYou have already used this sign!");
            }
        }
    }
}
