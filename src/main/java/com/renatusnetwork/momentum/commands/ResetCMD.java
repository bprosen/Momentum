package com.renatusnetwork.momentum.commands;

import com.renatusnetwork.momentum.Momentum;
import com.renatusnetwork.momentum.data.checkpoints.CheckpointDB;
import com.renatusnetwork.momentum.data.cmdsigns.CmdSignsDB;
import com.renatusnetwork.momentum.data.infinite.gamemode.InfiniteType;
import com.renatusnetwork.momentum.data.levels.CompletionsDB;
import com.renatusnetwork.momentum.data.levels.LevelsDB;
import com.renatusnetwork.momentum.data.ranks.RanksDB;
import com.renatusnetwork.momentum.data.saves.SavesDB;
import com.renatusnetwork.momentum.data.stats.StatsDB;
import com.renatusnetwork.momentum.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResetCMD implements CommandExecutor {

    private static final int CONFIRM_TIMEOUT_SECONDS = 3;

    private final Map<ResetInfo, BukkitTask> resetConfirmMap = new HashMap<>();

    private static final Map<ResettableStat, Resetter> STAT_RESETTERS = new HashMap<>();

    static {
        STAT_RESETTERS.put(ResettableStat.ALL, playerUUID -> {
            resetCoins(playerUUID);
            resetAllCompletions(playerUUID);
            resetSavesAndCheckpoints(playerUUID);
            resetParkourRank(playerUUID);
            resetClanMembership(playerUUID);
            resetPurchasedLevels(playerUUID);
            resetInGameStats(playerUUID);
            resetModifiers(playerUUID);
            resetELO(playerUUID);
            resetUsedCommandSigns(playerUUID);
        });

        STAT_RESETTERS.put(ResettableStat.COINS,               ResetCMD::resetCoins);
        STAT_RESETTERS.put(ResettableStat.TIMED_COMPLETIONS,   ResetCMD::resetTimedCompletions);
        STAT_RESETTERS.put(ResettableStat.ALL_COMPLETIONS,     ResetCMD::resetAllCompletions);
        STAT_RESETTERS.put(ResettableStat.SAVES_AND_CHECKPOINTS, ResetCMD::resetSavesAndCheckpoints);
        STAT_RESETTERS.put(ResettableStat.PARKOUR_RANK,        ResetCMD::resetParkourRank);
        STAT_RESETTERS.put(ResettableStat.CLAN_MEMBERSHIP,     ResetCMD::resetClanMembership);
        STAT_RESETTERS.put(ResettableStat.PURCHASED_LEVELS,    ResetCMD::resetPurchasedLevels);
        STAT_RESETTERS.put(ResettableStat.INGAME_STATS,        ResetCMD::resetInGameStats);
        STAT_RESETTERS.put(ResettableStat.MODIFIERS,           ResetCMD::resetModifiers);
        STAT_RESETTERS.put(ResettableStat.ELO,                 ResetCMD::resetELO);
        STAT_RESETTERS.put(ResettableStat.USED_COMMAND_SIGNS,  ResetCMD::resetUsedCommandSigns);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Utils.translate("&cInsufficient permissions"));
            return true;
        }

        if (args.length != 2) {
            sendHelp(sender);
            return true;
        }

        String playerName = args[0].toLowerCase();
        String playerUUID = StatsDB.getUUIDByName(playerName);

        if (playerUUID == null) {
            sender.sendMessage(Utils.translate(String.format("&cPlayer &4%s &cwas not found", playerName)));
            return true;
        }

        if (Momentum.getStatsManager().get(playerUUID) != null) {
            sender.sendMessage(Utils.translate("&cYou can only reset the stats of an offline player"));
            return true;
        }

        if (Momentum.getStatsManager().getOffline(playerUUID) != null) {
            Momentum.getStatsManager().removeOffline(playerUUID);
        }

        ResettableStat statToReset = parseStatSafely(sender, args[1]);
        if (statToReset == null) {
            return true;
        }

        if (!confirm(sender, playerUUID, playerName, statToReset)) {
            sender.sendMessage(Utils.translate(String.format(
                    "&6Are you sure you want to reset stat &4%s &6for player &4%s&6? Type the command again to confirm.",
                    statToReset.name(), playerName)));
            return true;
        }

        STAT_RESETTERS.get(statToReset).execute(playerUUID);
        sender.sendMessage(Utils.translate(String.format(
                "&aSuccessfully reset stat &2%s &afor player &2%s",
                statToReset.name(), playerName)));

        return true;
    }

    private ResettableStat parseStatSafely(CommandSender sender, String stat) {
        try {
            return ResettableStat.valueOf(stat.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Utils.translate(String.format("&cPlayer stat &4%s &cis not a valid stat", stat)));
            return null;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Utils.translate("&4&lReset Help"));
        for (ResettableStat stat : ResettableStat.values()) {
            sender.sendMessage(Utils.translate("&4/reset <player> " + stat.name() + "  &c" + stat.getDescription()));
        }
    }

    private boolean confirm(CommandSender sender, String playerUUID, String playerName, ResettableStat stat) {
        ResetInfo resetInfo = new ResetInfo(sender.getName(), playerUUID, stat);

        BukkitTask existing = resetConfirmMap.remove(resetInfo);
        if (existing != null) {
            existing.cancel();
            return true;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                resetConfirmMap.remove(resetInfo);
                sender.sendMessage(Utils.translate(String.format(
                        "&cYou did not confirm in time to reset stat &4%s &cfor player &4%s",
                        stat.name(), playerName)));
            }
        }.runTaskLater(Momentum.getPlugin(), 20L * CONFIRM_TIMEOUT_SECONDS);

        resetConfirmMap.put(resetInfo, task);
        return false;
    }

    // ---------------------------------------------------------------------------
    // Reset implementations
    // ---------------------------------------------------------------------------

    private static void resetCoins(String playerUUID) {
        StatsDB.updateCoins(playerUUID, 0, true);
    }

    private static void resetTimedCompletions(String playerUUID) {
        CompletionsDB.removeAllCompletionsFromPlayer(playerUUID, true, true);
    }

    private static void resetAllCompletions(String playerUUID) {
        CompletionsDB.removeAllCompletionsFromPlayer(playerUUID, false, true);
    }

    private static void resetSavesAndCheckpoints(String playerUUID) {
        SavesDB.removeAllSavesFromPlayer(playerUUID);
        CheckpointDB.deleteAllCheckpointsFromPlayer(playerUUID);
    }

    private static void resetParkourRank(String playerUUID) {
        StatsDB.updateRank(playerUUID, Momentum.getSettingsManager().default_rank);
    }

    private static void resetClanMembership(String playerUUID) {
        StatsDB.resetPlayerClanByUUID(playerUUID);
    }

    private static void resetPurchasedLevels(String playerUUID) {
        StatsDB.removeAllBoughtLevels(playerUUID);
    }

    private static void resetInGameStats(String playerUUID) {
        for (InfiniteType type : InfiniteType.values()) {
            StatsDB.updateInfiniteScore(playerUUID, type, 0);
        }

        StatsDB.updateRaceLosses(playerUUID, 0);
        StatsDB.updateRaceWins(playerUUID, 0);
        StatsDB.updateEventWins(playerUUID, 0);
        RanksDB.updatePrestiges(playerUUID, 0);
        StatsDB.resetInfiniteBlock(playerUUID);
        LevelsDB.removeAllLevelRatingsFromPlayer(playerUUID);
        StatsDB.removeAllFavoriteLevels(playerUUID);
    }

    private static void resetModifiers(String playerUUID) {
        StatsDB.removeAllModifiersFromPlayer(playerUUID);
    }

    private static void resetELO(String playerUUID) {
        StatsDB.updateELO(playerUUID, Momentum.getSettingsManager().default_elo);
        StatsDB.updateELOTier(playerUUID, Momentum.getSettingsManager().default_elo_tier);
    }

    private static void resetUsedCommandSigns(String playerUUID) {
        CmdSignsDB.unuseAllCommandSigns(playerUUID);
    }

    // ---------------------------------------------------------------------------
    // Inner types
    // ---------------------------------------------------------------------------

    public enum ResettableStat {
        ALL("Resets ALL stats for a player"),
        COINS("Sets a player's coins to 0"),
        TIMED_COMPLETIONS("Resets all timed level completions for a player"),
        ALL_COMPLETIONS("Resets all level completions for a player"),
        SAVES_AND_CHECKPOINTS("Resets all saves and checkpoints within all levels for a player"),
        PARKOUR_RANK {
            @Override
            public String getDescription() {
                return "Sets the rank of a player to " +
                        Momentum.getRanksManager().get(Momentum.getSettingsManager().default_rank).getTitle();
            }
        },
        CLAN_MEMBERSHIP("Forcefully kicks a player from their clan"),
        PURCHASED_LEVELS("Resets all purchased levels for a player"),
        INGAME_STATS("Resets all in-game stats for a player"),
        MODIFIERS("Resets all modifiers for a player"),
        ELO {
            @Override
            public String getDescription() {
                return "Sets the ELO of a player to " + Momentum.getSettingsManager().default_elo +
                        ", and their ELO tier to " +
                        Momentum.getELOTiersManager().get(Momentum.getSettingsManager().default_elo_tier).getTitle();
            }
        },
        USED_COMMAND_SIGNS("Resets all used command signs for a player");

        private final String description;

        ResettableStat(String description) {
            this.description = description;
        }

        ResettableStat() {
            this.description = null;
        }

        public String getDescription() {
            return description;
        }
    }

    private static class ResetInfo {
        private final String commandSender;
        private final String playerUUID;
        private final ResettableStat stat;

        ResetInfo(String commandSender, String playerUUID, ResettableStat stat) {
            this.commandSender = commandSender;
            this.playerUUID = playerUUID;
            this.stat = stat;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ResetInfo)) return false;
            ResetInfo o = (ResetInfo) other;
            return commandSender.equals(o.commandSender) &&
                    playerUUID.equals(o.playerUUID) &&
                    stat == o.stat;
        }

        @Override
        public int hashCode() {
            return Objects.hash(commandSender, playerUUID, stat);
        }
    }

    @FunctionalInterface
    private interface Resetter {
        void execute(String playerUUID);
    }
}