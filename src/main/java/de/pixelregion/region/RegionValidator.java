package de.pixelregion.region;

public final class RegionValidator {
    private RegionValidator() {}

    public static void validate(Region region) {
        if (region == null) throw new IllegalArgumentException("Region is required.");
        validateName(region.name());
        if (region.id() == null || region.worldId() == null) throw new IllegalArgumentException("Region id and world are required.");
        if (region.minY() >= region.maxY()) throw new IllegalArgumentException("minY must be smaller than maxY.");
        if (region.points().size() < 3) throw new IllegalArgumentException("A region polygon requires at least three points.");
        region.geometry();
    }

    public static void validateName(String name) {
        if (name == null || name.length() < 2 || name.length() > 32 || !name.matches("[A-Za-z0-9][A-Za-z0-9_-]*")) {
            throw new IllegalArgumentException("Region name must contain 2-32 letters, numbers, '_' or '-'.");
        }
    }
}
