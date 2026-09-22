package de.pixelregion.listener;

import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class RegionBlockListener implements Listener {

    private final RegionManager manager;

    public RegionBlockListener(final RegionManager manager) {
        this.manager = manager;
    }

    // Prevents block breaking when the effective BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!manager.allows(event.getBlock().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendRichMessage("<red>You cannot break blocks in this region.</red>");
        }
    }

    // Prevents block placement when the effective BUILD policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (!manager.allows(event.getBlock().getLocation(), RegionFlag.BUILD, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendRichMessage("<red>You cannot place blocks in this region.</red>");
        }
    }

    // Prevents physical and right-click interaction when the effective USE policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!manager.allows(event.getClickedBlock().getLocation(), RegionFlag.USE, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendRichMessage("<red>You cannot use that here.</red>");
        }
    }
}
