package de.pixelregion.listener;

import de.pixelregion.region.RegionTransitionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class RegionMovementListener implements Listener {
    private final RegionTransitionService transitions;
    public RegionMovementListener(RegionTransitionService transitions) { this.transitions = transitions; }

    // Enforces region entry/exit policies for complete overlapping region sets.
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null || !event.hasExplicitlyChangedPosition()) return;
        if (!transitions.handleTransition(event.getPlayer(), event.getFrom(), event.getTo())) event.setCancelled(true);
    }
}
