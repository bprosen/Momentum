package com.parkourcraft.Parkour.data;

import com.parkourcraft.Parkour.Parkour;
import com.parkourcraft.Parkour.data.perks.Perk;
import com.parkourcraft.Parkour.data.perks.PerkData;
import com.parkourcraft.Parkour.data.perks.Perks_YAML;
import com.parkourcraft.Parkour.data.stats.PlayerStats;
import com.parkourcraft.Parkour.storage.mysql.DataQueries;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PerkManager {

    private List<Perk> perks = new ArrayList<>();
    private Map<String, Integer> perkIDCache = new HashMap<>();

    public PerkManager() {
        load();
    }

    public void load() {
        for (String perkName : Perks_YAML.getNames())
            load(perkName);

        syncPermissions();
    }

    public Map<String, Integer> getPerkIDCache() {
        return perkIDCache;
    }

    public Perk get(String perkName) {
        for (Perk perk : perks)
            if (perk.getName().equals(perkName))
                return perk;

        return null;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public boolean exists(String perkName) {
        return get(perkName) != null;
    }

    public void remove(String perkName) {
        for (Iterator<Perk> iterator = perks.iterator(); iterator.hasNext();)
            if (iterator.next().getName().equals(perkName))
                iterator.remove();
    }

    public void create(String perkName) {
        if (!exists(perkName))
            Perks_YAML.create(perkName);
    }

    public void load(String perkName) {
        boolean exists = exists(perkName);

        if (!Perks_YAML.exists(perkName)
                && exists)
            remove(perkName);
        else {
            Perk perk = new Perk(perkName);
            PerkData.syncPerkID(perk);

            if (exists)
                remove(perkName);

            perks.add(perk);
        }
    }

    public void syncPermissions(Player player) {
        PlayerStats playerStats = StatsManager.get(player);

        for (Perk perk : perks) {
            boolean hasRequirements = perk.hasRequirements(playerStats, player);

            for (String permission : perk.getPermissions())
                player.addAttachment(Parkour.getPlugin(), permission, hasRequirements);
        }
    }

    private void syncPermissions() {
        for (Player player : Bukkit.getOnlinePlayers())
            syncPermissions(player);
    }

    public void bought(PlayerStats playerStats, Perk perk) {
        if (perk.getID() > 0) {
            Long date = System.currentTimeMillis();

            playerStats.addPerk(perk.getName(), date);
            PerkData.insertPerk(playerStats, perk, date);
        }
    }

}
