package de.pixelregion.listener;

import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionPolicyService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class RegionCombatListener implements Listener {
    private final RegionPolicyService policy;

    public RegionCombatListener(RegionPolicyService policy) {
        this.policy = policy;
    }

    // Applies PVP, mob-damage, and generic entity-damage through the central policy service.
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        final Entity victim = event.getEntity();
        final Entity source = event.getDamageSource().getCausingEntity();

        if (victim instanceof Player playerVictim && source instanceof Player playerSource
                && !policy.allows(playerVictim.getLocation(), RegionFlag.PVP, playerSource.getUniqueId(),
                playerSource.hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            return;
        }

        if (victim instanceof Player playerVictim && source != null && !(source instanceof Player)
                && !policy.allows(playerVictim.getLocation(), RegionFlag.MOB_DAMAGE, null,
                playerVictim.hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
            return;
        }

        if (!(victim instanceof Player) && source instanceof Player playerSource
                && !policy.allows(victim.getLocation(), RegionFlag.ENTITY_DAMAGE, playerSource.getUniqueId(),
                playerSource.hasPermission("pixelregion.bypass"))) {
            event.setCancelled(true);
        }
    }
}
