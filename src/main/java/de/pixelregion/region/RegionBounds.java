package de.pixelregion.region;

public record RegionBounds(double minX, double maxX, double minZ, double maxZ) {

    public boolean contains(double x, double z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
