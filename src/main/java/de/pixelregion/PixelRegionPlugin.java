package de.pixelregion;

import de.pixelregion.command.PixelRegionCommand;
import de.pixelregion.listener.RegionBlockListener;
import de.pixelregion.listener.RegionCombatListener;
import de.pixelregion.listener.RegionContainerListener;
import de.pixelregion.listener.RegionEnvironmentListener;
import de.pixelregion.listener.RegionMovementListener;
import de.pixelregion.region.FlagState;
import de.pixelregion.region.JsonRegionStorage;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import de.pixelregion.region.RegionPolicyService;
import de.pixelregion.region.RegionSessionManager;
import de.pixelregion.region.RegionTransitionService;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.EnumMap;

public final class PixelRegionPlugin extends JavaPlugin {
    private RegionManager regionManager;
    private RegionTransitionService regionTransitions;
    private RegionSessionManager sessionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final Path storagePath = getDataFolder().toPath().resolve(
                getConfig().getString("storage.file", "regions.json")
        );
        final JsonRegionStorage storage = new JsonRegionStorage(
                storagePath,
                message -> getLogger().warning(message),
                message -> getLogger().severe(message)
        );

        regionManager = new RegionManager(storage, defaultFlags());
        try {
            regionManager.load();
        } catch (IllegalStateException exception) {
            getLogger().severe("Pixel-Region cannot start because region storage is unavailable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final RegionPolicyService regionPolicy = new RegionPolicyService(regionManager);
        final RegionMessages messages = new RegionMessages(this);
        regionTransitions = new RegionTransitionService(regionManager, regionPolicy, messages);
        sessionManager = new RegionSessionManager();

        getServer().getPluginManager().registerEvents(new RegionBlockListener(regionPolicy, messages), this);
        getServer().getPluginManager().registerEvents(new RegionCombatListener(regionPolicy), this);
        getServer().getPluginManager().registerEvents(new RegionContainerListener(regionPolicy, messages), this);
        getServer().getPluginManager().registerEvents(new RegionEnvironmentListener(regionPolicy), this);
        getServer().getPluginManager().registerEvents(new RegionMovementListener(regionTransitions), this);
        getServer().getPluginManager().registerEvents(sessionManager, this);

        registerCommand(
                "pixelregion",
                new PixelRegionCommand(this, regionManager, sessionManager, regionPolicy, messages)
        );
        getLogger().info("Pixel-Region enabled with " + regionManager.size() + " regions.");
    }

    @Override
    public void onDisable() {
        if (regionManager != null && regionManager.isDirty()) {
            regionManager.save();
        }
        if (regionTransitions != null) {
            regionTransitions.clearAll();
        }
    }

    public RegionManager regionManager() {
        return regionManager;
    }

    private EnumMap<RegionFlag, FlagState> defaultFlags() {
        final EnumMap<RegionFlag, FlagState> flags = new EnumMap<>(RegionFlag.class);
        for (RegionFlag flag : RegionFlag.values()) {
            flags.put(flag, configState("default-" + flag.name().toLowerCase().replace('_', '-')));
        }
        return flags;
    }

    private FlagState configState(String path) {
        return "deny".equalsIgnoreCase(getConfig().getString("protection." + path, "allow"))
                ? FlagState.DENY
                : FlagState.ALLOW;
    }
}
