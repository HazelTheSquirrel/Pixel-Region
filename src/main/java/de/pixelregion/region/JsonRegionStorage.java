package de.pixelregion.region;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class JsonRegionStorage implements RegionStorage {
    private static final int CURRENT_SCHEMA_VERSION = 3;
    private static final Type LIST_TYPE = new TypeToken<List<Region>>() {}.getType();
    private static final Type DOCUMENT_TYPE = new TypeToken<RegionDocument>() {}.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final Consumer<String> warningLogger;
    private final Consumer<String> errorLogger;

    public JsonRegionStorage(Path file, Consumer<String> warningLogger, Consumer<String> errorLogger) {
        this.file = file;
        this.warningLogger = warningLogger;
        this.errorLogger = errorLogger;
    }

    @Override
    public List<Region> load() {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return parse(file);
        } catch (RuntimeException primaryFailure) {
            final Path backup = backupPath();
            if (!Files.exists(backup)) {
                throw storageFailure("Could not load region storage.", primaryFailure);
            }
            try {
                final List<Region> recovered = parse(backup);
                warningLogger.accept("Primary region storage was invalid; recovered from " + backup.getFileName() + ".");
                return recovered;
            } catch (RuntimeException backupFailure) {
                throw storageFailure("Region storage and backup are both invalid.", backupFailure);
            }
        }
    }

    @Override
    public synchronized void save(List<Region> regions) {
        final Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            final Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(temp)) {
                gson.toJson(new RegionDocument(CURRENT_SCHEMA_VERSION, List.copyOf(regions)), DOCUMENT_TYPE, writer);
            }
            if (Files.exists(file)) {
                Files.copy(file, backupPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailure) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
            throw storageFailure("Could not save region storage.", exception);
        }
    }

    @Override
    public void logInvalid(Region region, RuntimeException exception) {
        warningLogger.accept("Skipping invalid region "
                + (region == null ? "<unknown>" : region.name())
                + ": " + exception.getMessage());
    }

    private List<Region> parse(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            final StringWriter buffer = new StringWriter();
            reader.transferTo(buffer);
            final String json = buffer.toString();
            if (json.isBlank()) {
                return List.of();
            }
            if (json.trim().startsWith("[")) {
                final List<Region> legacy = gson.fromJson(json, LIST_TYPE);
                return legacy == null ? List.of() : new ArrayList<>(legacy);
            }
            final RegionDocument document = gson.fromJson(json, DOCUMENT_TYPE);
            if (document == null) {
                return List.of();
            }
            if (document.schemaVersion() > CURRENT_SCHEMA_VERSION) {
                throw new IllegalStateException("Unsupported region schema version: "
                        + document.schemaVersion() + " (current: " + CURRENT_SCHEMA_VERSION + ")");
            }
            return document.regions() == null ? List.of() : new ArrayList<>(document.regions());
        } catch (IOException | RuntimeException exception) {
            throw storageFailure("Could not parse " + source.getFileName() + ".", exception);
        }
    }

    private IllegalStateException storageFailure(String message, Throwable cause) {
        errorLogger.accept(message + " " + cause.getMessage());
        return new IllegalStateException(message, cause);
    }

    private Path backupPath() {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    private record RegionDocument(int schemaVersion, List<Region> regions) {
    }
}
