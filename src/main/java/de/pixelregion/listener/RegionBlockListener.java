package de.pixelregion.listener;

import de.pixelregion.RegionMessages;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionPolicyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class RegionBlockListener implements Listener {
    private final RegionPolicyService policy;
    private final RegionMessages messages;

    public RegionBlockListener(RegionPolicyService policy, RegionMessages messages) {
        this.policy = policy;
        this.messages = messages;
    }

    // Prevents block breaking when the central BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!policy.allows(event.getBlock().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            messages.denied(event.getPlayer());
        }
    }

    // Prevents block placement when the central BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!policy.allows(event.getBlockPlaced().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            messages.denied(event.getPlayer());
        }
    }

    // Prevents block and item interaction when the central USE policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!policy.allows(event.getClickedBlock().getLocation(), RegionFlag.USE, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            messages.denied(event.getPlayer());
        }
    }
}
