package de.pixelregion.region;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionGeometryTest {
    @Test
    void triangleInsideOutsideAndBoundary() {
        final RegionGeometry geometry = new RegionGeometry(List.of(
                new RegionPoint(0, 0),
                new RegionPoint(10, 0),
                new RegionPoint(0, 10)
        ));

        assertTrue(geometry.contains(2, 2));
        assertFalse(geometry.contains(8, 8));
        assertTrue(geometry.contains(0, 5));
        assertTrue(geometry.contains(0, 0));
        assertTrue(geometry.contains(5, 0));
    }

    @Test
    void rectangleAndChunkBoundaryCoordinatesRemainInside() {
        final RegionGeometry geometry = new RegionGeometry(List.of(
                new RegionPoint(-16, -16),
                new RegionPoint(16, -16),
                new RegionPoint(16, 16),
                new RegionPoint(-16, 16)
        ));

        assertTrue(geometry.contains(-16, -16));
        assertTrue(geometry.contains(16, 16));
        assertTrue(geometry.contains(0, 0));
        assertFalse(geometry.contains(16.0001, 0));
    }

    @Test
    void concavePolygonUsesActualPolygonNotOnlyBounds() {
        final RegionGeometry geometry = new RegionGeometry(List.of(
                new RegionPoint(0, 0),
                new RegionPoint(10, 0),
                new RegionPoint(10, 10),
                new RegionPoint(6, 10),
                new RegionPoint(6, 4),
                new RegionPoint(0, 4)
        ));

        assertTrue(geometry.contains(2, 2));
        assertTrue(geometry.contains(8, 8));
        assertFalse(geometry.contains(2, 8));
    }

    @Test
    void rejectsDuplicateAreaAndSelfIntersection() {
        assertThrows(IllegalArgumentException.class, () -> new RegionGeometry(List.of(
                new RegionPoint(0, 0),
                new RegionPoint(10, 0),
                new RegionPoint(10, 0)
        )));

        assertThrows(IllegalArgumentException.class, () -> new RegionGeometry(List.of(
                new RegionPoint(0, 0),
                new RegionPoint(1, 0),
                new RegionPoint(2, 0)
        )));

        assertThrows(IllegalArgumentException.class, () -> new RegionGeometry(List.of(
                new RegionPoint(0, 0),
                new RegionPoint(10, 10),
                new RegionPoint(0, 10),
                new RegionPoint(10, 0)
        )));
    }
}
