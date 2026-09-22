package de.pixelregion.region;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class RegionRepository {
    private static final int CURRENT_SCHEMA_VERSION = 2;
    private static final Type LIST_TYPE = new TypeToken<List<Region>>() {}.getType();
    private static final Type DOCUMENT_TYPE = new TypeToken<RegionDocument>() {}.getType();
    private final JavaPlugin plugin;
    private final Gson gson;
    private final Path file;

    public RegionRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.file = plugin.getDataFolder().toPath().resolve(plugin.getConfig().getString("storage.file", "regions.json"));
    }

    public List<Region> load() {
        if (!Files.exists(file)) return List.of();
        try {
            final String json;
            try (Reader reader = Files.newBufferedReader(file)) {
                json = reader.transferTo(new java.io.StringWriter()).toString();
            }
            if (json.isBlank()) return List.of();
            if (json.trim().startsWith("[")) {
                List<Region> legacy = gson.fromJson(json, LIST_TYPE);
                return legacy == null ? List.of() : new ArrayList<>(legacy);
            }
            RegionDocument document = gson.fromJson(json, DOCUMENT_TYPE);
            if (document == null) return List.of();
            if (document.schemaVersion() > CURRENT_SCHEMA_VERSION) {
                plugin.getLogger().severe("Unsupported region schema version: " + document.schemaVersion());
                return List.of();
            }
            return document.regions() == null ? List.of() : new ArrayList<>(document.regions());
        } catch (Exception exception) {
            plugin.getLogger().severe("Could not load regions: " + exception.getMessage());
            return List.of();
        }
    }

    public synchronized void save(List<Region> regions) {
        final Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        final Path backup = file.resolveSibling(file.getFileName() + ".bak");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temp)) {
                gson.toJson(new RegionDocument(CURRENT_SCHEMA_VERSION, List.copyOf(regions)), DOCUMENT_TYPE, writer);
            }
            if (Files.exists(file)) {
                try { Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING); }
                catch (IOException exception) { plugin.getLogger().warning("Could not update region backup: " + exception.getMessage()); }
            }
            try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException atomicFailure) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save regions: " + exception.getMessage());
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
        }
    }

    public void logInvalid(Region region, RuntimeException exception) {
        plugin.getLogger().warning("Skipping invalid region " + (region == null ? "<unknown>" : region.name()) + ": " + exception.getMessage());
    }

    private record RegionDocument(int schemaVersion, List<Region> regions) {}
}
