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
    private final RegionRepository repository;
    private final ConcurrentMap<UUID, Region> regions = new ConcurrentHashMap<>();
    private final ConcurrentMap<ChunkKey, List<UUID>> index = new ConcurrentHashMap<>();
    private final EnumMap<RegionFlag, FlagState> defaults = new EnumMap<>(RegionFlag.class);

    public RegionManager(RegionRepository repository, Map<RegionFlag, FlagState> defaults) {
        this.repository = repository;
        if (defaults != null) this.defaults.putAll(defaults);
    }

    public void load() {
        regions.clear(); index.clear();
        for (Region region : repository.load()) {
            try { RegionValidator.validate(region); register(region); }
            catch (RuntimeException exception) { repository.logInvalid(region, exception); }
        }
    }

    public synchronized void save() { repository.save(new ArrayList<>(regions.values())); }
    public int size() { return regions.size(); }
    public List<Region> all() { return regions.values().stream().sorted(order()).toList(); }

    public Optional<Region> byName(String name) {
        if (name == null) return Optional.empty();
        return regions.values().stream().filter(region -> region.name().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<Region> byId(UUID id) { return Optional.ofNullable(regions.get(id)); }

    public void add(Region region) {
        RegionValidator.validate(region);
        if (byName(region.name()).isPresent()) throw new IllegalArgumentException("A region with that name already exists.");
        if (regions.putIfAbsent(region.id(), region) != null) throw new IllegalArgumentException("A region with that id already exists.");
        addToIndex(region);
    }

    public boolean remove(String name) {
        Optional<Region> region = byName(name);
        if (region.isEmpty()) return false;
        Region removed = regions.remove(region.get().id());
        if (removed == null) return false;
        removeFromIndex(removed); return true;
    }

    public void replace(Region region) {
        RegionValidator.validate(region);
        Region old = regions.put(region.id(), region);
        if (old != null) removeFromIndex(old);
        addToIndex(region);
    }

    public List<Region> applicableRegions(Location location) {
        if (location == null || location.getWorld() == null) return List.of();
        final ChunkKey key = new ChunkKey(location.getWorld().getUID(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        final List<UUID> candidates = index.getOrDefault(key, List.of());
        if (candidates.isEmpty()) return List.of();

        final List<Region> matches = new ArrayList<>();
        for (UUID id : candidates) {
            Region region = regions.get(id);
            if (region != null && region.contains(location)) matches.add(region);
        }
        matches.sort(order());
        return List.copyOf(matches);
    }

    public List<Region> findAll(Location location) { return applicableRegions(location); }
    public Optional<Region> find(Location location) { return applicableRegions(location).stream().findFirst(); }
    public FlagState defaultState(RegionFlag flag) { return defaults.getOrDefault(flag, FlagState.ALLOW); }

    public boolean allows(Location location, RegionFlag flag, UUID playerId) {
        return new RegionPolicyService(this).allows(location, flag, playerId);
    }

    public boolean allows(Location location, RegionFlag flag, UUID playerId, boolean bypass) {
        return new RegionPolicyService(this).allows(location, flag, playerId, bypass);
    }

    private void register(Region region) { regions.put(region.id(), region); addToIndex(region); }

    private void addToIndex(Region region) {
        RegionBounds bounds = region.bounds();
        int minX = floorChunk(bounds.minX()), maxX = floorChunk(bounds.maxX());
        int minZ = floorChunk(bounds.minZ()), maxZ = floorChunk(bounds.maxZ());
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            ChunkKey key = new ChunkKey(region.worldId(), x, z);
            index.compute(key, (ignored, current) -> {
                List<UUID> updated = current == null ? new ArrayList<>() : new ArrayList<>(current);
                if (!updated.contains(region.id())) updated.add(region.id());
                return List.copyOf(updated);
            });
        }
    }

    private void removeFromIndex(Region region) {
        RegionBounds bounds = region.bounds();
        int minX = floorChunk(bounds.minX()), maxX = floorChunk(bounds.maxX());
        int minZ = floorChunk(bounds.minZ()), maxZ = floorChunk(bounds.maxZ());
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            ChunkKey key = new ChunkKey(region.worldId(), x, z);
            index.computeIfPresent(key, (ignored, current) -> {
                List<UUID> updated = current.stream().filter(id -> !id.equals(region.id())).toList();
                return updated.isEmpty() ? null : updated;
            });
        }
    }

    private static Comparator<Region> order() {
        return Comparator.comparingInt(Region::priority).reversed().thenComparing(Region::name, String.CASE_INSENSITIVE_ORDER);
    }

    private static int floorChunk(double coordinate) { return Math.floorDiv((int) Math.floor(coordinate), 16); }
    private record ChunkKey(UUID worldId, int x, int z) {}
}
