package de.pixelregion.listener;

import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionPolicyService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

public final class RegionEnvironmentListener implements Listener {
    private final RegionPolicyService policy;

    public RegionEnvironmentListener(RegionPolicyService policy) {
        this.policy = policy;
    }

    // Filters block-explosion damage against the central EXPLOSION policy.
    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> !policy.allows(block.getLocation(), RegionFlag.EXPLOSION, null));
    }

    // Filters entity-explosion damage against the central EXPLOSION policy.
    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> !policy.allows(block.getLocation(), RegionFlag.EXPLOSION, null));
    }

    // Prevents fluid transfer when the destination denies FLUID_FLOW.
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!policy.allows(event.getToBlock().getLocation(), RegionFlag.FLUID_FLOW, null)) {
            event.setCancelled(true);
        }
    }

    // Prevents player and environmental ignition when FIRE_SPREAD is denied.
    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        if (!policy.allows(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD, null)) {
            event.setCancelled(true);
        }
    }

    // Prevents natural fire block spread when FIRE_SPREAD is denied.
    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getNewState().getType() == Material.FIRE
                && !policy.allows(event.getBlock().getLocation(), RegionFlag.FIRE_SPREAD, null)) {
            event.setCancelled(true);
        }
    }

    // Prevents creature spawning when MOB_SPAWN is denied.
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!policy.allows(event.getLocation(), RegionFlag.MOB_SPAWN, null)) {
            event.setCancelled(true);
        }
    }
}
