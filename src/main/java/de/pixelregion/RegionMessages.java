package de.pixelregion;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public final class RegionMessages {

    private RegionMessages() {
    }

    public static void send(final CommandSender sender, final String message) {
        sender.sendMessage(Component.text(message));
    }
}
