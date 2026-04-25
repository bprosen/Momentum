<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/RespawnListener.java
package com.renatusnetwork.momentum.gameplay.listeners;
========
package com.renatusnetwork.momentum.gameplay;
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/RespawnListener.java

import com.renatusnetwork.momentum.Momentum;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {

<<<<<<<< HEAD:src/main/java/com/renatusnetwork/momentum/gameplay/listeners/RespawnListener.java
        Location spawn = Momentum.getLocationManager().getSpawnLocation();
========
        Location spawn = Momentum.getLocationManager().getLobbyLocation();
>>>>>>>> master:src/main/java/com/renatusnetwork/momentum/gameplay/RespawnListener.java

        if (spawn != null) {
            event.setRespawnLocation(spawn);
        }
    }
}
