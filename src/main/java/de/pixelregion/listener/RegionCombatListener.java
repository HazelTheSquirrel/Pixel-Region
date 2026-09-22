package de.pixelregion.listener;

import de.pixelregion.RegionMessages;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class RegionCombatListener implements Listener {

    private final RegionManager manager;

    public RegionCombatListener(final RegionManager manager) {
        this.manager = manager;
    }

    // Applies PVP, mob-damage, and generic entity-damage policies using Paper's causing entity attribution.
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity victim = event.getEntity();
        final Entity source = event.getDamageSource().getCausingEntity();

        if (victim instanceof Player playerVictim && source instanceof Player playerSource) {
            if (!manager.allows(playerVictim.getLocation(), RegionFlag.PVP, playerSource.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }

        if (victim instanceof Player playerVictim && source != null && !(source instanceof Player)) {
            if (!manager.allows(playerVictim.getLocation(), RegionFlag.MOB_DAMAGE, null)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(victim instanceof Player) && source != null && source instanceof Player playerSource) {
            if (!manager.allows(victim.getLocation(), RegionFlag.ENTITY_DAMAGE, playerSource.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
