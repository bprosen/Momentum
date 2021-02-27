package com.parkourcraft.parkour.gameplay;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PracticeHandler {

    public static void setSpectatorMode(Player spectator, Player player) {
        spectator.setAllowFlight(true);
        spectator.setFlying(true);
        spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));

    }
}
