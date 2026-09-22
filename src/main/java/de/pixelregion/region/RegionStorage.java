package de.pixelregion.region;

import java.util.List;

public interface RegionStorage {
    List<Region> load();
    void save(List<Region> regions);
    void logInvalid(Region region, RuntimeException exception);
}
