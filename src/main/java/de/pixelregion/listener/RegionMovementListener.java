package de.pixelregion.listener;

import de.pixelregion.region.Region;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public final class RegionMovementListener implements Listener {

    private final RegionManager manager;

    public RegionMovementListener(final RegionManager manager) {
        this.manager = manager;
    }

    // Enforces ENTRY and EXIT policies whenever a player actually crosses polygon/height boundaries.
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (sameLocation(event.getFrom(), event.getTo())) {
            return;
        }

        final Optional<Region> from = manager.find(event.getFrom());
        final Optional<Region> to = manager.find(event.getTo());

        final String fromId = from.map(region -> region.id().toString()).orElse("");
        final String toId = to.map(region -> region.id().toString()).orElse("");

        if (fromId.equals(toId)) {
            return;
        }

        if (to.isPresent() && !manager.allows(event.getTo(), RegionFlag.ENTRY, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (from.isPresent() && !manager.allows(event.getFrom(), RegionFlag.EXIT, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.setTo(event.getFrom());
        }
    }

    private boolean sameLocation(final org.bukkit.Location a, final org.bukkit.Location b) {
        return a.getWorld() == b.getWorld()
                && Double.compare(a.getX(), b.getX()) == 0
                && Double.compare(a.getY(), b.getY()) == 0
                && Double.compare(a.getZ(), b.getZ()) == 0;
    }
}
