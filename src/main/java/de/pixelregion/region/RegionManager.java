package de.pixelregion.region;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RegionManager {
    private final RegionStorage storage;
    private final ConcurrentMap<UUID, Region> regions = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkKey, List<UUID>> index = new ConcurrentHashMap<>();
    private final EnumMap<RegionFlag, FlagState> defaults = new EnumMap<>(RegionFlag.class);
    private volatile boolean dirty;

    public RegionManager(RegionStorage storage, Map<RegionFlag, FlagState> defaults) {
        this.storage = storage;
        if (defaults != null) {
            this.defaults.putAll(defaults);
        }
    }

    public synchronized void load() {
        regions.clear();
        index.clear();
        for (Region region : storage.load()) {
            try {
                RegionValidator.validate(region);
                register(region);
            } catch (RuntimeException exception) {
                storage.logInvalid(region, exception);
            }
        }
        dirty = false;
    }

    public synchronized void save() {
        if (!dirty) {
            return;
        }
        storage.save(new ArrayList<>(regions.values()));
        dirty = false;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int size() {
        return regions.size();
    }

    public List<Region> all() {
        return regions.values().stream().sorted(order()).toList();
    }

    public Optional<Region> byName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return regions.values().stream()
                .filter(region -> region.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Region> byId(UUID id) {
        return Optional.ofNullable(regions.get(id));
    }

    public void add(Region region) {
        RegionValidator.validate(region);
        if (byName(region.name()).isPresent()) {
            throw new IllegalArgumentException("A region with that name already exists.");
        }
        if (regions.putIfAbsent(region.id(), region) != null) {
            throw new IllegalArgumentException("A region with that id already exists.");
        }
        addToIndex(region);
        dirty = true;
    }

    public boolean remove(String name) {
        final Optional<Region> region = byName(name);
        if (region.isEmpty()) {
            return false;
        }
        final Region removed = regions.remove(region.get().id());
        if (removed == null) {
            return false;
        }
        removeFromIndex(removed);
        dirty = true;
        return true;
    }

    public void replace(Region region) {
        RegionValidator.validate(region);
        final Region old = regions.get(region.id());
        final Region duplicate = byName(region.name()).orElse(null);
        if (duplicate != null && !duplicate.id().equals(region.id())) {
            throw new IllegalArgumentException("A region with that name already exists.");
        }
        regions.put(region.id(), region);
        if (old != null) {
            removeFromIndex(old);
        }
        addToIndex(region);
        dirty = true;
    }

    public void markDirty() {
        dirty = true;
    }

    public List<Region> applicableRegions(Location location) {
        if (location == null || location.getWorld() == null) {
            return List.of();
        }
        return applicableRegions(new RegionQuery(
                location.getWorld().getUID(),
                location.getX(),
                location.getY(),
                location.getZ()
        ));
    }

    public List<Region> applicableRegions(RegionQuery query) {
        final int chunkX = Math.floorDiv((int) Math.floor(query.x()), 16);
        final int chunkZ = Math.floorDiv((int) Math.floor(query.z()), 16);
        final ChunkKey key = new ChunkKey(query.worldId(), chunkX, chunkZ);
        final List<UUID> candidates = index.getOrDefault(key, List.of());
        if (candidates.isEmpty()) {
            return List.of();
        }

        final List<Region> matches = new ArrayList<>();
        for (UUID id : candidates) {
            final Region region = regions.get(id);
            if (region != null && region.contains(query.worldId(), query.x(), query.y(), query.z())) {
                matches.add(region);
            }
        }
        matches.sort(order());
        return List.copyOf(matches);
    }

    public List<Region> findAll(Location location) {
        return applicableRegions(location);
    }

    public Optional<Region> find(Location location) {
        return applicableRegions(location).stream().findFirst();
    }

    public FlagState defaultState(RegionFlag flag) {
        return defaults.getOrDefault(flag, FlagState.ALLOW);
    }

    private void register(Region region) {
        final Region duplicate = byName(region.name()).orElse(null);
        if (duplicate != null && !duplicate.id().equals(region.id())) {
            throw new IllegalArgumentException("Duplicate region name: " + region.name());
        }
        if (regions.putIfAbsent(region.id(), region) != null) {
            throw new IllegalArgumentException("Duplicate region id: " + region.id());
        }
        addToIndex(region);
    }

    private void addToIndex(Region region) {
        final RegionBounds bounds = region.bounds();
        final int minX = floorChunk(bounds.minX());
        final int maxX = floorChunk(bounds.maxX());
        final int minZ = floorChunk(bounds.minZ());
        final int maxZ = floorChunk(bounds.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final ChunkKey key = new ChunkKey(region.worldId(), x, z);
                index.compute(key, (ignored, current) -> {
                    final List<UUID> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
                    if (!updated.contains(region.id())) {
                        updated.add(region.id());
                    }
                    return List.copyOf(updated);
                });
            }
        }
    }

    private void removeFromIndex(Region region) {
        final RegionBounds bounds = region.bounds();
        final int minX = floorChunk(bounds.minX());
        final int maxX = floorChunk(bounds.maxX());
        final int minZ = floorChunk(bounds.minZ());
        final int maxZ = floorChunk(bounds.maxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final ChunkKey key = new ChunkKey(region.worldId(), x, z);
                index.computeIfPresent(key, (ignored, current) -> {
                    final List<UUID> updated = current.stream().filter(id -> !id.equals(region.id())).toList();
                    return updated.isEmpty() ? null : updated;
                });
            }
        }
    }

    private static Comparator<Region> order() {
        return Comparator.comparingInt(Region::priority)
                .reversed()
                .thenComparing(Region::name, String.CASE_INSENSITIVE_ORDER);
    }

    private static int floorChunk(double coordinate) {
        return Math.floorDiv((int) Math.floor(coordinate), 16);
    }

    private record ChunkKey(UUID worldId, int x, int z) {
    }
}
