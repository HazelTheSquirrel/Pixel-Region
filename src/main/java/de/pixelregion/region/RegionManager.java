package de.pixelregion.region;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RegionManager {

    private final RegionRepository repository;
    private final ConcurrentMap<UUID, Region> regions = new ConcurrentHashMap<>();

    public RegionManager(final RegionRepository repository) {
        this.repository = repository;
    }

    public void load() {
        regions.clear();
        for (final Region region : repository.load()) {
            try {
                new RegionGeometry(region.points());
                regions.put(region.id(), region);
            } catch (RuntimeException exception) {
                repositoryPluginLog("Skipping invalid region " + region.name() + ": " + exception.getMessage());
            }
        }
    }

    public void save() {
        repository.save(new ArrayList<>(regions.values()));
    }

    public int size() {
        return regions.size();
    }

    public List<Region> all() {
        return regions.values().stream()
                .sorted(Comparator.comparingInt(Region::priority).reversed().thenComparing(Region::name))
                .toList();
    }

    public Optional<Region> byName(final String name) {
        return regions.values().stream()
                .filter(region -> region.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Region> byId(final UUID id) {
        return Optional.ofNullable(regions.get(id));
    }

    public void add(final Region region) {
        if (regions.values().stream().anyMatch(existing -> existing.name().equalsIgnoreCase(region.name()))) {
            throw new IllegalArgumentException("A region with that name already exists.");
        }
        regions.put(region.id(), region);
    }

    public boolean remove(final String name) {
        final Optional<Region> region = byName(name);
        region.ifPresent(value -> regions.remove(value.id()));
        return region.isPresent();
    }

    public List<Region> findAll(final Location location) {
        if (location == null || location.getWorld() == null) {
            return List.of();
        }

        final double x = location.getX();
        final double z = location.getZ();

        return regions.values().stream()
                .filter(region -> region.worldId().equals(location.getWorld().getUID()))
                .filter(region -> location.getY() >= region.minY() && location.getY() < region.maxY())
                .filter(region -> region.bounds().contains(x, z))
                .filter(region -> region.contains(location))
                .sorted(Comparator.comparingInt(Region::priority).reversed().thenComparing(Region::name))
                .toList();
    }

    public Optional<Region> find(final Location location) {
        return findAll(location).stream().findFirst();
    }

    public FlagState effectiveFlag(final Location location, final RegionFlag flag) {
        for (final Region region : findAll(location)) {
            final FlagState state = region.flag(flag);
            if (state != null) {
                return state;
            }
        }
        return FlagState.ALLOW;
    }

    public boolean allows(final Location location, final RegionFlag flag, final UUID playerId) {
        for (final Region region : findAll(location)) {
            if (playerId != null && region.hasAccess(playerId)) {
                return true;
            }

            final FlagState state = region.flag(flag);
            if (state != null) {
                return state == FlagState.ALLOW;
            }
        }
        return true;
    }

    private void repositoryPluginLog(final String message) {
        System.err.println("[Pixel-Region] " + message);
    }
}
