package de.pixelregion.region;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record RegionTransition(Set<UUID> entered, Set<UUID> exited, Set<UUID> retained) {
    public RegionTransition {
        entered = Set.copyOf(entered);
        exited = Set.copyOf(exited);
        retained = Set.copyOf(retained);
    }

    public static RegionTransition calculate(Set<UUID> before, Set<UUID> after) {
        final Set<UUID> entered = new HashSet<>(after);
        entered.removeAll(before);
        final Set<UUID> exited = new HashSet<>(before);
        exited.removeAll(after);
        final Set<UUID> retained = new HashSet<>(before);
        retained.retainAll(after);
        return new RegionTransition(entered, exited, retained);
    }
}
