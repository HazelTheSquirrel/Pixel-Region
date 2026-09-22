package de.pixelregion.listener;

import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public final class RegionEnvironmentListener implements Listener {

    private final RegionManager manager;

    public RegionEnvironmentListener(final RegionManager manager) {
        this.manager = manager;
    }

    // Prevents block explosions from damaging blocks inside regions that deny explosions.
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent event) {
        event.blockList().removeIf(block ->
                !manager.allows(block.getLocation(), RegionFlag.EXPLOSION, null));
    }

    // Prevents entity explosions from damaging blocks inside regions that deny explosions.
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        event.blockList().removeIf(block ->
                !manager.allows(block.getLocation(), RegionFlag.EXPLOSION, null));
    }

    // Prevents liquid transfer into a location where the effective FLUID_FLOW policy denies it.
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (!manager.allows(event.getToBlock().getLocation(), RegionFlag.FLUID_FLOW, null)) {
            event.setCancelled(true);
        }
    }

    // Prevents fire ignition inside regions that deny fire spread.
    @EventHandler(ignoreCancelled = true)
    public void onIgnite(final BlockIgniteEvent event) {
        if (!manager.allows(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD, null)) {
            event.setCancelled(true);
        }
    }
}
