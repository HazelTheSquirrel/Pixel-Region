package de.pixelregion.region;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionTransitionTest {
    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    @Test
    void outsideToInside() {
        final RegionTransition transition = RegionTransition.calculate(Set.of(), Set.of(a));
        assertEquals(Set.of(a), transition.entered());
        assertEquals(Set.of(), transition.exited());
        assertEquals(Set.of(), transition.retained());
    }

    @Test
    void insideToOutside() {
        final RegionTransition transition = RegionTransition.calculate(Set.of(a), Set.of());
        assertEquals(Set.of(), transition.entered());
        assertEquals(Set.of(a), transition.exited());
        assertEquals(Set.of(), transition.retained());
    }

    @Test
    void regionAToRegionBAndOverlap() {
        final RegionTransition change = RegionTransition.calculate(Set.of(a), Set.of(b));
        assertEquals(Set.of(b), change.entered());
        assertEquals(Set.of(a), change.exited());

        final RegionTransition overlap = RegionTransition.calculate(Set.of(a, b), Set.of(b, c));
        assertEquals(Set.of(c), overlap.entered());
        assertEquals(Set.of(a), overlap.exited());
        assertEquals(Set.of(b), overlap.retained());
    }
}
