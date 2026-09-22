package de.pixelregion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionMessages {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final String denied;
    private final String enter;
    private final String exit;

    public RegionMessages(JavaPlugin plugin) {
        this.denied = plugin.getConfig().getString("messages.denied", "<red>You are not allowed to do that here.");
        this.enter = plugin.getConfig().getString("messages.enter", "<gray>You entered <gold>{region}</gold>.");
        this.exit = plugin.getConfig().getString("messages.exit", "<gray>You left <gold>{region}</gold>.");
    }

    public void send(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message));
    }

    public void denied(Player player) {
        player.sendMessage(miniMessage.deserialize(denied));
    }

    public void entered(Player player, String region) {
        player.sendMessage(miniMessage.deserialize(
                enter,
                Placeholder.unparsed("region", region)
        ));
    }

    public void exited(Player player, String region) {
        player.sendMessage(miniMessage.deserialize(
                exit,
                Placeholder.unparsed("region", region)
        ));
    }
}
