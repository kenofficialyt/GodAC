package ac.god.godac.checks.movement;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GodMovementListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (GodMovementManager.getInstance() != null) {
            GodMovementManager.getInstance().onPlayerMove(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (GodMovementManager.getInstance() != null) {
            GodMovementManager.getInstance().onPlayerQuit(event.getPlayer());
        }
        if (GodVelocityManager.getInstance() != null) {
            GodVelocityManager.getInstance();
        }
    }
}
