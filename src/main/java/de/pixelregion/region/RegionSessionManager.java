package de.pixelregion.region;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RegionSessionManager implements Listener {

    private final ConcurrentMap<UUID, RegionSession> sessions = new ConcurrentHashMap<>();

    public void start(final RegionSession session) {
        sessions.put(session.playerId(), session);
    }

    public RegionSession get(final UUID playerId) {
        return sessions.get(playerId);
    }

    public RegionSession remove(final UUID playerId) {
        return sessions.remove(playerId);
    }

    public boolean contains(final UUID playerId) {
        return sessions.containsKey(playerId);
    }

    // Removes an unfinished polygon editor session when its player leaves the server.
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }
}
