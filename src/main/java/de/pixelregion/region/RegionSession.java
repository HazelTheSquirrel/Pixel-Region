package de.pixelregion.region;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RegionSession {

    private final UUID playerId;
    private final String name;
    private final UUID worldId;
    private final int minY;
    private final int maxY;
    private final List<RegionPoint> points = new ArrayList<>();

    public RegionSession(
            final UUID playerId,
            final String name,
            final UUID worldId,
            final int minY,
            final int maxY
    ) {
        this.playerId = playerId;
        this.name = name;
        this.worldId = worldId;
        this.minY = minY;
        this.maxY = maxY;
    }

    public UUID playerId() { return playerId; }
    public String name() { return name; }
    public UUID worldId() { return worldId; }
    public int minY() { return minY; }
    public int maxY() { return maxY; }
    public List<RegionPoint> points() { return List.copyOf(points); }

    public void addPoint(final RegionPoint point) {
        points.add(point);
    }

    public void clearPoints() {
        points.clear();
    }
}
