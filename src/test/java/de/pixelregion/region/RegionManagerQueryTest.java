package de.pixelregion.region;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegionManagerQueryTest {
    private static final UUID WORLD = UUID.randomUUID();

    @Test
    void queryUsesWorldYBoundsPolygonAndChunkIndex() {
        final Region region = region("crossing", 0, List.of(
                new RegionPoint(15, 15),
                new RegionPoint(33, 15),
                new RegionPoint(33, 33),
                new RegionPoint(15, 33)
        ));
        final RegionManager manager = manager(region);

        assertEquals(List.of(region), manager.applicableRegions(new RegionQuery(WORLD, 16, 5, 16)));
        assertEquals(List.of(), manager.applicableRegions(new RegionQuery(WORLD, 34, 5, 16)));
        assertEquals(List.of(), manager.applicableRegions(new RegionQuery(UUID.randomUUID(), 16, 5, 16)));
        assertEquals(List.of(), manager.applicableRegions(new RegionQuery(WORLD, 16, 50, 16)));
    }


    @Test
    void manyRegionsRemainQueryableThroughSpatialIndex() {
        final RegionManager manager = manager();
        for (int i = 0; i < 1000; i++) {
            final double offset = i * 32.0;
            manager.add(region("r" + i, 0, square(offset)));
        }

        for (int i = 0; i < 1000; i++) {
            final double offset = i * 32.0;
            assertEquals("r" + i, manager.applicableRegions(
                    new RegionQuery(WORLD, offset + 5, 5, offset + 5)
            ).getFirst().name());
        }
    }

    @Test
    void caseInsensitiveNamesAndReplacementStayUnique() {
        final Region first = region("Alpha", 0, square(0));
        final Region second = region("Beta", 0, square(40));
        final RegionManager manager = manager(first, second);

        assertThrows(IllegalArgumentException.class, () -> manager.add(region("alpha", 0, square(80))));
        assertThrows(IllegalArgumentException.class, () -> manager.replace(new Region(
                second.id(), "alpha", WORLD, 0, 100, second.points(), 0,
                second.owner(), Set.of(), Map.of()
        )));
    }

    private static Region region(String name, int priority, List<RegionPoint> points) {
        return new Region(
                UUID.randomUUID(), name, WORLD, 0, 40, points,
                priority, UUID.randomUUID(), Set.of(), Map.of()
        );
    }

    private static List<RegionPoint> square(double offset) {
        return List.of(
                new RegionPoint(offset, offset),
                new RegionPoint(offset + 10, offset),
                new RegionPoint(offset + 10, offset + 10),
                new RegionPoint(offset, offset + 10)
        );
    }

    private static RegionManager manager(Region... regions) {
        final RegionManager manager = new RegionManager(new MemoryStorage(), Map.of());
        for (Region region : regions) {
            manager.add(region);
        }
        return manager;
    }

    private static final class MemoryStorage implements RegionStorage {
        @Override public List<Region> load() { return List.of(); }
        @Override public void save(List<Region> regions) {}
        @Override public void logInvalid(Region region, RuntimeException exception) {}
    }
}
