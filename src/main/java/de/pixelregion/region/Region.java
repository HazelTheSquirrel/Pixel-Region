package de.pixelregion.region;

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

    public Region(
            final UUID id,
            final String name,
            final UUID worldId,
            final int minY,
            final int maxY,
            final List<RegionPoint> points,
            final int priority,
            final UUID owner,
            final Set<UUID> members,
            final Map<RegionFlag, FlagState> flags
    ) {
        if (id == null || worldId == null) {
            throw new IllegalArgumentException("Region id and world id are required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Region name is required.");
        }
        if (minY >= maxY) {
            throw new IllegalArgumentException("minY must be smaller than maxY.");
        }

        this.id = id;
        this.name = name;
        this.worldId = worldId;
        this.minY = minY;
        this.maxY = maxY;
        this.points = List.copyOf(points);
        this.priority = priority;
        this.owner = owner;
        this.members = new HashSet<>(members == null ? Set.of() : members);
        this.flags = new EnumMap<>(RegionFlag.class);
        if (flags != null) {
            this.flags.putAll(flags);
        }

        new RegionGeometry(this.points);
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

    public boolean contains(final org.bukkit.Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!worldId.equals(location.getWorld().getUID())) {
            return false;
        }
        if (location.getY() < minY || location.getY() >= maxY) {
            return false;
        }
        final RegionGeometry geometry = new RegionGeometry(points);
        return geometry.contains(location.getX(), location.getZ());
    }

    public RegionBounds bounds() {
        return new RegionGeometry(points).bounds();
    }

    public FlagState flag(final RegionFlag flag) {
        return flags.get(flag);
    }

    public void setFlag(final RegionFlag flag, final FlagState state) {
        if (state == null) {
            flags.remove(flag);
        } else {
            flags.put(flag, state);
        }
    }

    public boolean hasAccess(final UUID playerId) {
        return playerId != null && (playerId.equals(owner) || members.contains(playerId));
    }

    public void addMember(final UUID playerId) {
        members.add(playerId);
    }

    public void removeMember(final UUID playerId) {
        members.remove(playerId);
    }
}
