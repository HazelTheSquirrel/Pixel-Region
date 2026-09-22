package de.pixelregion.region;

import java.util.UUID;

public record RegionQuery(UUID worldId, double x, double y, double z) {
    public RegionQuery {
        if (worldId == null) {
            throw new IllegalArgumentException("World id is required.");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Query coordinates must be finite.");
        }
    }
}
