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
import java.util.ArrayList;
import java.util.List;

public final class RegionRepository {

    private static final Type LIST_TYPE = new TypeToken<List<Region>>() {}.getType();

    private final JavaPlugin plugin;
    private final Gson gson;
    private final Path file;

    public RegionRepository(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.file = plugin.getDataFolder().toPath().resolve(
                plugin.getConfig().getString("storage.file", "regions.json")
        );
    }

    public List<Region> load() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            final List<Region> regions = gson.fromJson(reader, LIST_TYPE);
            return regions == null ? new ArrayList<>() : new ArrayList<>(regions);
        } catch (Exception exception) {
            plugin.getLogger().severe("Could not load regions: " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    public void save(final List<Region> regions) {
        try {
            Files.createDirectories(file.getParent());
            final Path temp = file.resolveSibling(file.getFileName() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(temp)) {
                gson.toJson(regions, LIST_TYPE, writer);
            }

            try {
                Files.move(
                        temp,
                        file,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE
                );
            } catch (IOException atomicFailure) {
                Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not save regions: " + exception.getMessage());
        }
    }
}
