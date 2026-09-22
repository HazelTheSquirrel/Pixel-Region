package de.pixelregion.command;

import de.pixelregion.RegionMessages;
import de.pixelregion.region.FlagState;
import de.pixelregion.region.Region;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import de.pixelregion.region.RegionPoint;
import de.pixelregion.region.RegionSession;
import de.pixelregion.region.RegionSessionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.UUID;

public final class PixelRegionCommand implements io.papermc.paper.command.brigadier.BasicCommand {

    private final JavaPlugin plugin;
    private final RegionManager manager;
    private final RegionSessionManager sessions;

    public PixelRegionCommand(
            final JavaPlugin plugin,
            final RegionManager manager,
            final RegionSessionManager sessions
    ) {
        this.plugin = plugin;
        this.manager = manager;
        this.sessions = sessions;
    }

    @Override
    public void execute(final CommandSourceStack source, final String[] args) {
        final CommandSender sender = source.getSender();

        if (args.length == 0) {
            help(sender);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> list(sender);
            case "info" -> info(sender, args);
            case "create" -> create(sender, args);
            case "point" -> point(sender, args);
            case "finish" -> finish(sender);
            case "cancel" -> cancel(sender);
            case "delete" -> delete(sender, args);
            case "flag" -> flag(sender, args);
            case "member" -> member(sender, args);
            case "reload" -> reload(sender);
            case "save" -> save(sender);
            default -> help(sender);
        }
    }

    @Override
    public boolean canUse(final CommandSender sender) {
        return sender.hasPermission("pixelregion.command");
    }

    @Override
    public String permission() {
        return "pixelregion.command";
    }

    private void help(final CommandSender sender) {
        RegionMessages.send(sender, "/pixelregion commands:");
        RegionMessages.send(sender, "create <name> Start a polygon.");
        RegionMessages.send(sender, "point [x] [z] Add a polygon point.");
        RegionMessages.send(sender, "finish Create the region.");
        RegionMessages.send(sender, "cancel Cancel the current polygon.");
        RegionMessages.send(sender, "list List regions.");
        RegionMessages.send(sender, "info [name] Show region information.");
        RegionMessages.send(sender, "flag <name> <flag> <allow|deny>");
        RegionMessages.send(sender, "member <add|remove> <name> <player>");
        RegionMessages.send(sender, "delete <name>");
    }

    private void list(final CommandSender sender) {
        if (manager.all().isEmpty()) {
            RegionMessages.send(sender, "No regions exist.");
            return;
        }
        RegionMessages.send(sender, "Regions:");
        for (final Region region : manager.all()) {
            RegionMessages.send(sender, "- " + region.name()
                    + " (priority " + region.priority() + ")");
        }
    }

    private void info(final CommandSender sender, final String[] args) {
        Region region = null;
        if (args.length >= 2) {
            region = manager.byName(args[1]).orElse(null);
        } else if (sender instanceof Player player) {
            region = manager.find(player.getLocation()).orElse(null);
        }

        if (region == null) {
            RegionMessages.send(sender, "No region found.");
            return;
        }

        RegionMessages.send(sender, "" + region.name() + "");
        RegionMessages.send(sender, "World: " + region.worldId());
        RegionMessages.send(sender, "Points: " + region.points().size());
        RegionMessages.send(sender, "Y: " + region.minY() + " - " + region.maxY());
        RegionMessages.send(sender, "Priority: " + region.priority());
        RegionMessages.send(sender, "Owner: " + (region.owner() == null ? "none" : region.owner()));
    }

    private void create(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            RegionMessages.send(sender, "This command requires a player.");
            return;
        }
        if (args.length < 2 || args[1].isBlank()) {
            RegionMessages.send(sender, "Usage: /pixelregion create <name>");
            return;
        }
        if (sessions.contains(player.getUniqueId())) {
            RegionMessages.send(sender, "You already have an active polygon.");
            return;
        }
        if (manager.byName(args[1]).isPresent()) {
            RegionMessages.send(sender, "A region with that name already exists.");
            return;
        }

        final World world = player.getWorld();
        sessions.start(new RegionSession(
                player.getUniqueId(),
                args[1],
                world.getUID(),
                world.getMinHeight(),
                world.getMaxHeight()
        ));

        RegionMessages.send(sender, "Polygon started. Use /pixelregion point to add points.");
    }

    private void point(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            RegionMessages.send(sender, "This command requires a player.");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            RegionMessages.send(sender, "No active polygon. Use /pixelregion create <name>.");
            return;
        }

        final double x;
        final double z;
        try {
            x = args.length >= 3 ? Double.parseDouble(args[1]) : player.getLocation().getX();
            z = args.length >= 3 ? Double.parseDouble(args[2]) : player.getLocation().getZ();
        } catch (NumberFormatException exception) {
            RegionMessages.send(sender, "Coordinates must be numbers.");
            return;
        }

        session.addPoint(new RegionPoint(x, z));
        RegionMessages.send(sender, "Point added. Total: " + session.points().size());
    }

    private void finish(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            RegionMessages.send(sender, "This command requires a player.");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            RegionMessages.send(sender, "No active polygon.");
            return;
        }
        if (session.points().size() < 3) {
            RegionMessages.send(sender, "A polygon requires at least three points.");
            return;
        }

        try {
            final Region region = new Region(
                    UUID.randomUUID(),
                    session.name(),
                    session.worldId(),
                    session.minY(),
                    session.maxY(),
                    session.points(),
                    0,
                    player.getUniqueId(),
                    java.util.Set.of(),
                    java.util.Map.of()
            );
            manager.add(region);
            manager.save();
            sessions.remove(player.getUniqueId());
            RegionMessages.send(sender, "Region created: " + region.name() + "");
        } catch (IllegalArgumentException exception) {
            RegionMessages.send(sender, "Region rejected: " + exception.getMessage());
        }
    }

    private void cancel(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            RegionMessages.send(sender, "This command requires a player.");
            return;
        }
        if (sessions.remove(player.getUniqueId()) != null) {
            RegionMessages.send(sender, "Polygon cancelled.");
        } else {
            RegionMessages.send(sender, "No active polygon.");
        }
    }

    private void delete(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            RegionMessages.send(sender, "Usage: /pixelregion delete <name>");
            return;
        }
        if (!manager.remove(args[1])) {
            RegionMessages.send(sender, "Region not found.");
            return;
        }
        manager.save();
        RegionMessages.send(sender, "Region deleted.");
    }

    private void flag(final CommandSender sender, final String[] args) {
        if (args.length < 4) {
            RegionMessages.send(sender, "Usage: /pixelregion flag <name> <flag> <allow|deny>");
            return;
        }

        final Region region = manager.byName(args[1]).orElse(null);
        if (region == null) {
            RegionMessages.send(sender, "Region not found.");
            return;
        }

        final RegionFlag flag;
        final FlagState state;
        try {
            flag = RegionFlag.valueOf(args[2].toUpperCase(Locale.ROOT));
            state = FlagState.valueOf(args[3].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            RegionMessages.send(sender, "Unknown flag or state.");
            return;
        }

        region.setFlag(flag, state);
        manager.save();
        RegionMessages.send(sender, "Flag updated.");
    }

    private void member(final CommandSender sender, final String[] args) {
        if (args.length < 4) {
            RegionMessages.send(sender, "Usage: /pixelregion member <add|remove> <name> <player>");
            return;
        }

        final Region region = manager.byName(args[2]).orElse(null);
        if (region == null) {
            RegionMessages.send(sender, "Region not found.");
            return;
        }

        final Player target = plugin.getServer().getPlayerExact(args[3]);
        if (target == null) {
            RegionMessages.send(sender, "Player must be online.");
            return;
        }

        if (!(sender instanceof Player player) || (!region.hasAccess(player.getUniqueId())
                && !sender.hasPermission("pixelregion.admin"))) {
            RegionMessages.send(sender, "You do not control this region.");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            region.addMember(target.getUniqueId());
            RegionMessages.send(sender, "Member added.");
        } else if (args[1].equalsIgnoreCase("remove")) {
            region.removeMember(target.getUniqueId());
            RegionMessages.send(sender, "Member removed.");
        } else {
            RegionMessages.send(sender, "Use add or remove.");
            return;
        }
        manager.save();
    }

    private void reload(final CommandSender sender) {
        manager.load();
        RegionMessages.send(sender, "Regions reloaded.");
    }

    private void save(final CommandSender sender) {
        manager.save();
        RegionMessages.send(sender, "Regions saved.");
    }
}
