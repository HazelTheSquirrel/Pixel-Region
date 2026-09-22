package de.pixelregion.region;

import de.pixelregion.RegionMessages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RegionTransitionService {
    private final RegionManager regions;
    private final RegionPolicyService policy;
    private final Map<UUID, Set<UUID>> current = new HashMap<>();

    public RegionTransitionService(RegionManager regions, RegionPolicyService policy) {
        this.regions = regions;
        this.policy = policy;
    }

    public boolean handleTransition(Player player, Location from, Location to) {
        final List<Region> beforeRegions = regions.applicableRegions(from);
        final List<Region> afterRegions = regions.applicableRegions(to);
        final Set<UUID> before = ids(beforeRegions);
        final Set<UUID> after = ids(afterRegions);

        if (before.equals(after)) {
            current.put(player.getUniqueId(), after);
            return true;
        }

        final boolean bypass = player.hasPermission("pixelregion.bypass");
        for (Region region : afterRegions) {
            if (!before.contains(region.id())
                    && !policy.allows(to, RegionFlag.ENTRY, player.getUniqueId(), bypass)) {
                return false;
            }
        }
        for (Region region : beforeRegions) {
            if (!after.contains(region.id())
                    && !policy.allows(from, RegionFlag.EXIT, player.getUniqueId(), bypass)) {
                return false;
            }
        }

        for (Region region : beforeRegions) {
            if (!after.contains(region.id())) {
                RegionMessages.send(player, "You left region " + region.name() + ".");
            }
        }
        for (Region region : afterRegions) {
            if (!before.contains(region.id())) {
                RegionMessages.send(player, "You entered region " + region.name() + ".");
            }
        }

        current.put(player.getUniqueId(), after);
        return true;
    }

    public void clear(UUID playerId) {
        if (playerId != null) current.remove(playerId);
    }

    public void clearAll() {
        current.clear();
    }

    private static Set<UUID> ids(List<Region> regions) {
        return regions.stream().map(Region::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
