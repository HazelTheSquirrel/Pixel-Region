package de.pixelregion.listener;

import de.pixelregion.RegionMessages;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionPolicyService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public final class RegionContainerListener implements Listener {
    private final RegionPolicyService policy;
    private final RegionMessages messages;

    public RegionContainerListener(RegionPolicyService policy, RegionMessages messages) {
        this.policy = policy;
        this.messages = messages;
    }

    // Prevents opening a physical or entity-backed container when CONTAINER_USE denies access.
    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        final var location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }
        if (!policy.allows(location, RegionFlag.CONTAINER_USE, player.getUniqueId(),
                player.hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            messages.denied(player);
        }
    }
}
