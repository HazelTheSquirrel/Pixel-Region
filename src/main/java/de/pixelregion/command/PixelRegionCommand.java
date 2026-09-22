package de.pixelregion.command;

import de.pixelregion.region.FlagState;
import de.pixelregion.region.Region;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import de.pixelregion.region.RegionPoint;
import de.pixelregion.region.RegionSession;
import de.pixelregion.region.RegionSessionManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.UUID;

public final class PixelRegionCommand implements org.bukkit.command.BasicCommand {

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
        sender.sendRichMessage("<gold>/pixelregion</gold> <gray>commands:</gray>");
        sender.sendRichMessage("<yellow>create <name></yellow> <gray>Start a polygon.</gray>");
        sender.sendRichMessage("<yellow>point [x] [z]</yellow> <gray>Add a polygon point.</gray>");
        sender.sendRichMessage("<yellow>finish</yellow> <gray>Create the region.</gray>");
        sender.sendRichMessage("<yellow>cancel</yellow> <gray>Cancel the current polygon.</gray>");
        sender.sendRichMessage("<yellow>list</yellow> <gray>List regions.</gray>");
        sender.sendRichMessage("<yellow>info [name]</yellow> <gray>Show region information.</gray>");
        sender.sendRichMessage("<yellow>flag <name> <flag> <allow|deny></yellow>");
        sender.sendRichMessage("<yellow>member <add|remove> <name> <player></yellow>");
        sender.sendRichMessage("<yellow>delete <name></yellow>");
    }

    private void list(final CommandSender sender) {
        if (manager.all().isEmpty()) {
            sender.sendRichMessage("<gray>No regions exist.</gray>");
            return;
        }
        sender.sendRichMessage("<gold>Regions:</gold>");
        for (final Region region : manager.all()) {
            sender.sendRichMessage("<gray>- <white>" + region.name()
                    + "</white> <dark_gray>(priority " + region.priority() + ")</dark_gray>");
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
            sender.sendRichMessage("<red>No region found.</red>");
            return;
        }

        sender.sendRichMessage("<gold>" + region.name() + "</gold>");
        sender.sendRichMessage("<gray>World:</gray> " + region.worldId());
        sender.sendRichMessage("<gray>Points:</gray> " + region.points().size());
        sender.sendRichMessage("<gray>Y:</gray> " + region.minY() + " - " + region.maxY());
        sender.sendRichMessage("<gray>Priority:</gray> " + region.priority());
        sender.sendRichMessage("<gray>Owner:</gray> " + (region.owner() == null ? "none" : region.owner()));
    }

    private void create(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>This command requires a player.</red>");
            return;
        }
        if (args.length < 2 || args[1].isBlank()) {
            sender.sendRichMessage("<red>Usage: /pixelregion create <name></red>");
            return;
        }
        if (sessions.contains(player.getUniqueId())) {
            sender.sendRichMessage("<red>You already have an active polygon.</red>");
            return;
        }
        if (manager.byName(args[1]).isPresent()) {
            sender.sendRichMessage("<red>A region with that name already exists.</red>");
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

        sender.sendRichMessage("<green>Polygon started.</green> Use <yellow>/pixelregion point</yellow> to add points.");
    }

    private void point(final CommandSender sender, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>This command requires a player.</red>");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            sender.sendRichMessage("<red>No active polygon. Use /pixelregion create <name>.</red>");
            return;
        }

        final double x;
        final double z;
        try {
            x = args.length >= 3 ? Double.parseDouble(args[1]) : player.getLocation().getX();
            z = args.length >= 3 ? Double.parseDouble(args[2]) : player.getLocation().getZ();
        } catch (NumberFormatException exception) {
            sender.sendRichMessage("<red>Coordinates must be numbers.</red>");
            return;
        }

        session.addPoint(new RegionPoint(x, z));
        sender.sendRichMessage("<green>Point added.</green> <gray>Total:</gray> " + session.points().size());
    }

    private void finish(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>This command requires a player.</red>");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            sender.sendRichMessage("<red>No active polygon.</red>");
            return;
        }
        if (session.points().size() < 3) {
            sender.sendRichMessage("<red>A polygon requires at least three points.</red>");
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
            sender.sendRichMessage("<green>Region created:</green> <gold>" + region.name() + "</gold>");
        } catch (IllegalArgumentException exception) {
            sender.sendRichMessage("<red>Region rejected:</red> " + exception.getMessage());
        }
    }

    private void cancel(final CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendRichMessage("<red>This command requires a player.</red>");
            return;
        }
        if (sessions.remove(player.getUniqueId()) != null) {
            sender.sendRichMessage("<gray>Polygon cancelled.</gray>");
        } else {
            sender.sendRichMessage("<red>No active polygon.</red>");
        }
    }

    private void delete(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendRichMessage("<red>Usage: /pixelregion delete <name></red>");
            return;
        }
        if (!manager.remove(args[1])) {
            sender.sendRichMessage("<red>Region not found.</red>");
            return;
        }
        manager.save();
        sender.sendRichMessage("<green>Region deleted.</green>");
    }

    private void flag(final CommandSender sender, final String[] args) {
        if (args.length < 4) {
            sender.sendRichMessage("<red>Usage: /pixelregion flag <name> <flag> <allow|deny></red>");
            return;
        }

        final Region region = manager.byName(args[1]).orElse(null);
        if (region == null) {
            sender.sendRichMessage("<red>Region not found.</red>");
            return;
        }

        final RegionFlag flag;
        final FlagState state;
        try {
            flag = RegionFlag.valueOf(args[2].toUpperCase(Locale.ROOT));
            state = FlagState.valueOf(args[3].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            sender.sendRichMessage("<red>Unknown flag or state.</red>");
            return;
        }

        region.setFlag(flag, state);
        manager.save();
        sender.sendRichMessage("<green>Flag updated.</green>");
    }

    private void member(final CommandSender sender, final String[] args) {
        if (args.length < 4) {
            sender.sendRichMessage("<red>Usage: /pixelregion member <add|remove> <name> <player></red>");
            return;
        }

        final Region region = manager.byName(args[2]).orElse(null);
        if (region == null) {
            sender.sendRichMessage("<red>Region not found.</red>");
            return;
        }

        final Player target = plugin.getServer().getPlayerExact(args[3]);
        if (target == null) {
            sender.sendRichMessage("<red>Player must be online.</red>");
            return;
        }

        if (!(sender instanceof Player player) || (!region.hasAccess(player.getUniqueId())
                && !sender.hasPermission("pixelregion.admin"))) {
            sender.sendRichMessage("<red>You do not control this region.</red>");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            region.addMember(target.getUniqueId());
            sender.sendRichMessage("<green>Member added.</green>");
        } else if (args[1].equalsIgnoreCase("remove")) {
            region.removeMember(target.getUniqueId());
            sender.sendRichMessage("<green>Member removed.</green>");
        } else {
            sender.sendRichMessage("<red>Use add or remove.</red>");
            return;
        }
        manager.save();
    }

    private void reload(final CommandSender sender) {
        manager.load();
        sender.sendRichMessage("<green>Regions reloaded.</green>");
    }

    private void save(final CommandSender sender) {
        manager.save();
        sender.sendRichMessage("<green>Regions saved.</green>");
    }
}
