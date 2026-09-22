package de.pixelregion.region;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionPolicyServiceTest {
    private static final UUID WORLD = UUID.randomUUID();
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID MEMBER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();

    @Test
    void worldAndYBoundsArePartOfQuery() {
        final Region region = region("world", WORLD, -10, 10, 0, Map.of(RegionFlag.BUILD, FlagState.DENY));
        final RegionManager manager = manager(region);
        final RegionPolicyService policy = new RegionPolicyService(manager);

        assertFalse(policy.allows(new RegionQuery(WORLD, 5, 0, 5), RegionFlag.BUILD, OTHER));
        assertTrue(policy.allows(new RegionQuery(UUID.randomUUID(), 5, 0, 5), RegionFlag.BUILD, OTHER));
        assertTrue(policy.allows(new RegionQuery(WORLD, 5, 10, 5), RegionFlag.BUILD, OTHER));
    }

    @Test
    void priorityAndUnsetFallthroughAreDeterministic() {
        final Region low = region("low", WORLD, -10, 20, 0, Map.of(RegionFlag.BUILD, FlagState.ALLOW));
        final Region highUnset = region("high", WORLD, -10, 20, 10, Map.of());
        final Region highDeny = region("deny", WORLD, -10, 20, 20, Map.of(RegionFlag.BUILD, FlagState.DENY));

        final RegionManager manager = manager(low, highUnset, highDeny);
        final RegionPolicyService policy = new RegionPolicyService(manager);

        assertFalse(policy.allows(new RegionQuery(WORLD, 1, 0, 1), RegionFlag.BUILD, OTHER));
        manager.remove("deny");
        assertTrue(policy.allows(new RegionQuery(WORLD, 1, 0, 1), RegionFlag.BUILD, OTHER));
    }

    @Test
    void ownerAndMemberReceiveExplicitRegionAccess() {
        final Region region = region("owned", WORLD, -10, 20, 0, Map.of(RegionFlag.BUILD, FlagState.DENY));
        final RegionManager manager = manager(region);
        final RegionPolicyService policy = new RegionPolicyService(manager);
        final RegionQuery query = new RegionQuery(WORLD, 1, 0, 1);

        assertTrue(policy.allows(query, RegionFlag.BUILD, OWNER));
        assertFalse(policy.allows(query, RegionFlag.BUILD, MEMBER));
        assertFalse(policy.allows(query, RegionFlag.BUILD, OTHER));

        final Region withMember = region("member", WORLD, -10, 20, 0, Map.of(RegionFlag.BUILD, FlagState.DENY),
                OWNER, Set.of(MEMBER));
        final RegionManager memberManager = manager(withMember);
        assertTrue(new RegionPolicyService(memberManager).allows(query, RegionFlag.BUILD, MEMBER));
    }

    @Test
    void explicitFlagFallsBackToConfiguredDefault() {
        final RegionManager manager = managerWithDefaults(Map.of(RegionFlag.PVP, FlagState.DENY));
        final RegionPolicyService policy = new RegionPolicyService(manager);

        assertFalse(policy.allows(new RegionQuery(WORLD, 1, 0, 1), RegionFlag.PVP, OTHER));
        assertTrue(policy.allows(new RegionQuery(WORLD, 1, 0, 1), RegionFlag.BUILD, OTHER));
    }

    @Test
    void bypassIsSeparateFromRegionPolicy() {
        final Region region = region("protected", WORLD, -10, 20, 0, Map.of(RegionFlag.BUILD, FlagState.DENY));
        final RegionPolicyService policy = new RegionPolicyService(manager(region));
        assertTrue(policy.allows(new RegionQuery(WORLD, 1, 0, 1), RegionFlag.BUILD, OTHER, true));
    }

    private static Region region(String name, UUID world, int minY, int maxY, int priority,
                                 Map<RegionFlag, FlagState> flags) {
        return region(name, world, minY, maxY, priority, flags, OWNER, Set.of());
    }

    private static Region region(String name, UUID world, int minY, int maxY, int priority,
                                 Map<RegionFlag, FlagState> flags, UUID owner, Set<UUID> members) {
        return new Region(
                UUID.randomUUID(), name, world, minY, maxY,
                List.of(new RegionPoint(0, 0), new RegionPoint(32, 0), new RegionPoint(32, 32), new RegionPoint(0, 32)),
                priority, owner, members, flags
        );
    }

    private static RegionManager manager(Region... regions) {
        return managerWithDefaults(Map.of(), regions);
    }

    private static RegionManager managerWithDefaults(Map<RegionFlag, FlagState> defaults, Region... regions) {
        final EnumMap<RegionFlag, FlagState> configured = new EnumMap<>(RegionFlag.class);
        configured.putAll(defaults);
        final RegionManager manager = new RegionManager(new MemoryStorage(), configured);
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
