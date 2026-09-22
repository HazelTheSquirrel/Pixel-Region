package de.pixelregion;

import de.pixelregion.command.PixelRegionCommand;
import de.pixelregion.listener.RegionBlockListener;
import de.pixelregion.listener.RegionCombatListener;
import de.pixelregion.listener.RegionEnvironmentListener;
import de.pixelregion.listener.RegionMovementListener;
import de.pixelregion.region.RegionManager;
import de.pixelregion.region.RegionRepository;
import de.pixelregion.region.RegionSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PixelRegionPlugin extends JavaPlugin {

    private RegionManager regionManager;
    private RegionSessionManager sessionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final RegionRepository repository = new RegionRepository(this);
        this.regionManager = new RegionManager(repository);
        this.regionManager.load();

        this.sessionManager = new RegionSessionManager();

        getServer().getPluginManager().registerEvents(
                new RegionBlockListener(regionManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new RegionCombatListener(regionManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new RegionEnvironmentListener(regionManager),
                this
        );
        getServer().getPluginManager().registerEvents(
                new RegionMovementListener(regionManager),
                this
        );

        getServer().getPluginManager().registerEvents(sessionManager, this);

        final PixelRegionCommand command = new PixelRegionCommand(this, regionManager, sessionManager);
        registerCommand("pixelregion", command);

        getLogger().info("Pixel-Region enabled with " + regionManager.size() + " regions.");
    }

    @Override
    public void onDisable() {
        if (regionManager != null) {
            regionManager.save();
        }
    }

    public RegionManager regionManager() {
        return regionManager;
    }
}
