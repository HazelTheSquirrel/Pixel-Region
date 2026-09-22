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
    public RegionBlockListener(RegionPolicyService policy) { this.policy = policy; }

    // Prevents block breaking when the central BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!policy.allows(event.getBlock().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            RegionMessages.send(event.getPlayer(), "You cannot break blocks in this region.");
        }
    }

    // Prevents block placement when the central BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!policy.allows(event.getBlock().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            RegionMessages.send(event.getPlayer(), "You cannot place blocks in this region.");
        }
    }

    // Prevents block and item interaction when the central USE policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!policy.allows(event.getClickedBlock().getLocation(), RegionFlag.USE, event.getPlayer().getUniqueId(),
                event.getPlayer().hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            RegionMessages.send(event.getPlayer(), "You cannot use that here.");
        }
    }
}
