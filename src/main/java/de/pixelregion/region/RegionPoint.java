package de.pixelregion.region;

public record RegionPoint(double x, double z) {

    public RegionPoint {
        if (!Double.isFinite(x) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Region coordinates must be finite.");
        }
    }
}
