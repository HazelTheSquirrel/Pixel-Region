package de.pixelregion.region;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRegionStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void saveLoadAndBackupAreDurable() {
        final Path file = tempDir.resolve("regions.json");
        final JsonRegionStorage storage = new JsonRegionStorage(file, ignored -> {}, ignored -> {});
        final Region region = region("storage");

        storage.save(List.of(region));
        assertTrue(Files.exists(file));

        final List<Region> loaded = storage.load();
        assertEquals(1, loaded.size());
        assertEquals(region.id(), loaded.getFirst().id());

        storage.save(List.of());
        assertTrue(Files.exists(tempDir.resolve("regions.json.bak")));
    }

    @Test
    void legacyArrayFormatIsReadable() throws Exception {
        final Path file = tempDir.resolve("regions.json");
        final JsonRegionStorage storage = new JsonRegionStorage(file, ignored -> {}, ignored -> {});
        final Region region = region("legacy");
        Files.writeString(file, new com.google.gson.Gson().toJson(List.of(region)));

        assertEquals(region.id(), storage.load().getFirst().id());
    }

    @Test
    void unsupportedSchemaDoesNotSilentlyOverwriteData() throws Exception {
        final Path file = tempDir.resolve("regions.json");
        Files.writeString(file, "{\"schemaVersion\":999,\"regions\":[]}");
        final JsonRegionStorage storage = new JsonRegionStorage(file, ignored -> {}, ignored -> {});

        assertThrows(IllegalStateException.class, storage::load);
        assertTrue(Files.exists(file));
    }

    private static Region region(String name) {
        return new Region(
                UUID.randomUUID(), name, UUID.randomUUID(), -10, 10,
                List.of(new RegionPoint(0, 0), new RegionPoint(10, 0), new RegionPoint(10, 10), new RegionPoint(0, 10)),
                0, UUID.randomUUID(), Set.of(), Map.of()
        );
    }
}
