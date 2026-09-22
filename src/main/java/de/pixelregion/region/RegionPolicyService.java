package de.pixelregion.region;

import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public final class RegionPolicyService {
    private final RegionManager regions;

    public RegionPolicyService(RegionManager regions) {
        this.regions = regions;
    }

    public boolean allows(Location location, RegionFlag flag, UUID playerId) {
        return allows(location, flag, playerId, false);
    }

    public boolean allows(Location location, RegionFlag flag, UUID playerId, boolean bypass) {
        if (bypass || location == null || flag == null) {
            return true;
        }
        if (location.getWorld() == null) {
            return true;
        }
        return allows(new RegionQuery(
                location.getWorld().getUID(),
                location.getX(),
                location.getY(),
                location.getZ()
        ), flag, playerId, false);
    }

    public boolean allows(RegionQuery query, RegionFlag flag, UUID playerId) {
        return allows(query, flag, playerId, false);
    }

    public boolean allows(RegionQuery query, RegionFlag flag, UUID playerId, boolean bypass) {
        if (bypass || query == null || flag == null) {
            return true;
        }

        final List<Region> applicable = regions.applicableRegions(query);
        for (Region region : applicable) {
            if (playerId != null && region.hasAccess(playerId)) {
                return true;
            }
            final FlagState state = region.flag(flag);
            if (state != null) {
                return state == FlagState.ALLOW;
            }
        }
        return regions.defaultState(flag) == FlagState.ALLOW;
    }
}
