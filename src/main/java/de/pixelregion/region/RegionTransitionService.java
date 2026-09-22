package de.pixelregion.region;

import de.pixelregion.RegionMessages;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RegionTransitionService {
    private final RegionManager regions;
    private final RegionPolicyService policy;
    private final RegionMessages messages;
    private final Map<UUID, Set<UUID>> current = new HashMap<>();

    public RegionTransitionService(
            RegionManager regions,
            RegionPolicyService policy,
            RegionMessages messages
    ) {
        this.regions = regions;
        this.policy = policy;
        this.messages = messages;
    }

    public boolean handleTransition(Player player, Location from, Location to) {
        final List<Region> beforeRegions = regions.applicableRegions(from);
        final List<Region> afterRegions = regions.applicableRegions(to);
        final Set<UUID> before = ids(beforeRegions);
        final Set<UUID> after = ids(afterRegions);
        final RegionTransition transition = RegionTransition.calculate(before, after);

        if (transition.entered().isEmpty() && transition.exited().isEmpty()) {
            current.put(player.getUniqueId(), after);
            return true;
        }

        final boolean bypass = player.hasPermission("pixelregion.bypass");
        if (!bypass) {
            for (Region region : afterRegions) {
                if (transition.entered().contains(region.id())
                        && !allowsRegionTransition(to, RegionFlag.ENTRY, region, player.getUniqueId())) {
                    return false;
                }
            }
            for (Region region : beforeRegions) {
                if (transition.exited().contains(region.id())
                        && !allowsRegionTransition(from, RegionFlag.EXIT, region, player.getUniqueId())) {
                    return false;
                }
            }
        }

        for (Region region : beforeRegions) {
            if (transition.exited().contains(region.id())) {
                messages.exited(player, region.name());
            }
        }
        for (Region region : afterRegions) {
            if (transition.entered().contains(region.id())) {
                messages.entered(player, region.name());
            }
        }

        current.put(player.getUniqueId(), after);
        return true;
    }

    public void clear(UUID playerId) {
        if (playerId != null) {
            current.remove(playerId);
        }
    }

    public void clearAll() {
        current.clear();
    }

    private boolean allowsRegionTransition(Location location, RegionFlag flag, Region target, UUID playerId) {
        if (target.hasAccess(playerId)) {
            return true;
        }
        final List<Region> applicable = regions.applicableRegions(location);
        for (Region region : applicable) {
            if (region.id().equals(target.id())) {
                final FlagState state = region.flag(flag);
                if (state != null) {
                    return state == FlagState.ALLOW;
                }
                continue;
            }
            final FlagState state = region.flag(flag);
            if (state != null) {
                return state == FlagState.ALLOW;
            }
        }
        return regions.defaultState(flag) == FlagState.ALLOW;
    }

    private static Set<UUID> ids(List<Region> regions) {
        return regions.stream().map(Region::id).collect(Collectors.toUnmodifiableSet());
    }
}
