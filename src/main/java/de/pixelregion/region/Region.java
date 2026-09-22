package de.pixelregion.region;

import org.bukkit.Location;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Region {
    private final UUID id;
    private final String name;
    private final UUID worldId;
    private final int minY;
    private final int maxY;
    private final List<RegionPoint> points;
    private final int priority;
    private UUID owner;
    private final Set<UUID> members;
    private final EnumMap<RegionFlag, FlagState> flags;
    private transient RegionGeometry geometry;

    public Region(UUID id, String name, UUID worldId, int minY, int maxY, List<RegionPoint> points, int priority,
                  UUID owner, Set<UUID> members, Map<RegionFlag, FlagState> flags) {
        if (id == null || worldId == null) throw new IllegalArgumentException("Region id and world id are required.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Region name is required.");
        if (minY >= maxY) throw new IllegalArgumentException("minY must be smaller than maxY.");
        this.id = id; this.name = name; this.worldId = worldId; this.minY = minY; this.maxY = maxY;
        this.points = List.copyOf(points); this.priority = priority; this.owner = owner;
        this.members = new HashSet<>(members == null ? Set.of() : members); this.members.remove(this.owner);
        this.flags = new EnumMap<>(RegionFlag.class); if (flags != null) this.flags.putAll(flags);
        rebuildGeometry();
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public UUID worldId() { return worldId; }
    public int minY() { return minY; }
    public int maxY() { return maxY; }
    public List<RegionPoint> points() { return points; }
    public int priority() { return priority; }
    public UUID owner() { return owner; }
    public Set<UUID> members() { return Set.copyOf(members); }
    public Map<RegionFlag, FlagState> flags() { return Map.copyOf(flags); }

    public RegionGeometry geometry() {
        if (geometry == null) rebuildGeometry();
        return geometry;
    }

    public void rebuildGeometry() { geometry = new RegionGeometry(points); }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null || !worldId.equals(location.getWorld().getUID())) return false;
        if (location.getY() < minY || location.getY() >= maxY) return false;
        return geometry().contains(location.getX(), location.getZ());
    }

    public RegionBounds bounds() { return geometry().bounds(); }
    public FlagState flag(RegionFlag flag) { return flags.get(flag); }
    public boolean hasExplicitFlag(RegionFlag flag) { return flags.containsKey(flag); }

    public void setFlag(RegionFlag flag, FlagState state) {
        if (state == null) flags.remove(flag); else flags.put(flag, state);
    }

    public boolean hasAccess(UUID playerId) {
        return playerId != null && (playerId.equals(owner) || members.contains(playerId));
    }

    public void addMember(UUID playerId) {
        if (playerId != null && !playerId.equals(owner)) members.add(playerId);
    }

    public void removeMember(UUID playerId) {
        if (playerId != null) members.remove(playerId);
    }
}
