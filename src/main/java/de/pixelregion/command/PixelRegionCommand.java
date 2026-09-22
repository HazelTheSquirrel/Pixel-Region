package de.pixelregion.command;

import de.pixelregion.RegionMessages;
import de.pixelregion.region.FlagState;
import de.pixelregion.region.Region;
import de.pixelregion.region.RegionFlag;
import de.pixelregion.region.RegionManager;
import de.pixelregion.region.RegionPolicyService;
import de.pixelregion.region.RegionPoint;
import de.pixelregion.region.RegionSession;
import de.pixelregion.region.RegionSessionManager;
import de.pixelregion.region.RegionValidator;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PixelRegionCommand implements io.papermc.paper.command.brigadier.BasicCommand {
    private final JavaPlugin plugin;
    private final RegionManager manager;
    private final RegionSessionManager sessions;
    private final RegionPolicyService policy;
    private final RegionMessages messages;

    public PixelRegionCommand(
            JavaPlugin plugin,
            RegionManager manager,
            RegionSessionManager sessions,
            RegionPolicyService policy,
            RegionMessages messages
    ) {
        this.plugin = plugin;
        this.manager = manager;
        this.sessions = sessions;
        this.policy = policy;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
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
            case "edit" -> edit(sender, args);
            case "delete" -> delete(sender, args);
            case "flag" -> flag(sender, args);
            case "member" -> member(sender, args);
            case "debug" -> debug(sender, args);
            case "reload" -> reload(sender);
            case "save" -> save(sender);
            default -> help(sender);
        }
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("pixelregion.command");
    }

    @Override
    public String permission() {
        return "pixelregion.command";
    }

    private void help(CommandSender sender) {
        messages.send(sender, "/pixelregion commands:");
        messages.send(sender, "create <name> Start a polygon.");
        messages.send(sender, "point [x] [z] Add a polygon point.");
        messages.send(sender, "finish Create the region.");
        messages.send(sender, "cancel Cancel the current polygon.");
        messages.send(sender, "list List regions.");
        messages.send(sender, "info [name] Show region information.");
        messages.send(sender, "edit <name> <priority|miny|maxy|point>");
        messages.send(sender, "flag <name> <flag> <allow|deny>");
        messages.send(sender, "member <add|remove> <name> <player>");
        messages.send(sender, "delete <name>");
        messages.send(sender, "debug [flag]");
        messages.send(sender, "reload");
        messages.send(sender, "save");
    }

    private void list(CommandSender sender) {
        if (!hasPermission(sender, "pixelregion.command.list")) {
            return;
        }
        if (manager.all().isEmpty()) {
            messages.send(sender, "No regions exist.");
            return;
        }
        messages.send(sender, "Regions:");
        for (Region region : manager.all()) {
            messages.send(sender, "- " + region.name() + " (priority " + region.priority() + ")");
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.info")) {
            return;
        }

        Region region = null;
        if (args.length >= 2) {
            region = manager.byName(args[1]).orElse(null);
        } else if (sender instanceof Player player) {
            region = manager.find(player.getLocation()).orElse(null);
        }

        if (region == null) {
            messages.send(sender, "No region found.");
            return;
        }

        messages.send(sender, region.name());
        messages.send(sender, "World: " + region.worldId());
        messages.send(sender, "Points: " + region.points().size());
        messages.send(sender, "Y: " + region.minY() + " - " + region.maxY());
        messages.send(sender, "Priority: " + region.priority());
        messages.send(sender, "Owner: " + (region.owner() == null ? "none" : region.owner()));
        messages.send(sender, "Members: " + region.members().size());
        messages.send(sender, "Flags: " + region.flags());
    }

    private void create(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.create")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "This command requires a player.");
            return;
        }
        if (args.length < 2 || args[1].isBlank()) {
            messages.send(sender, "Usage: /pixelregion create <name>");
            return;
        }

        try {
            RegionValidator.validateName(args[1]);
        } catch (IllegalArgumentException exception) {
            messages.send(sender, exception.getMessage());
            return;
        }

        if (sessions.contains(player.getUniqueId())) {
            messages.send(sender, "You already have an active polygon.");
            return;
        }
        if (manager.byName(args[1]).isPresent()) {
            messages.send(sender, "A region with that name already exists.");
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
        messages.send(sender, "Polygon started. Use /pixelregion point to add points.");
    }

    private void point(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.create")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "This command requires a player.");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            messages.send(sender, "No active polygon. Use /pixelregion create <name>.");
            return;
        }

        final double x;
        final double z;
        try {
            x = args.length >= 3 ? Double.parseDouble(args[1]) : player.getLocation().getX();
            z = args.length >= 3 ? Double.parseDouble(args[2]) : player.getLocation().getZ();
        } catch (NumberFormatException exception) {
            messages.send(sender, "Coordinates must be numbers.");
            return;
        }

        session.addPoint(new RegionPoint(x, z));
        messages.send(sender, "Point added. Total: " + session.points().size());
    }

    private void finish(CommandSender sender) {
        if (!hasPermission(sender, "pixelregion.command.create")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "This command requires a player.");
            return;
        }

        final RegionSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            messages.send(sender, "No active polygon.");
            return;
        }
        if (session.points().size() < 3) {
            messages.send(sender, "A polygon requires at least three points.");
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
                    Set.of(),
                    Map.of()
            );
            manager.add(region);
            manager.save();
            sessions.remove(player.getUniqueId());
            messages.send(sender, "Region created: " + region.name());
        } catch (IllegalArgumentException exception) {
            messages.send(sender, "Region rejected: " + exception.getMessage());
        }
    }

    private void cancel(CommandSender sender) {
        if (!hasPermission(sender, "pixelregion.command.create")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "This command requires a player.");
            return;
        }
        if (sessions.remove(player.getUniqueId()) != null) {
            messages.send(sender, "Polygon cancelled.");
        } else {
            messages.send(sender, "No active polygon.");
        }
    }

    private void edit(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.edit")) {
            return;
        }
        if (args.length < 3) {
            messages.send(sender, "Usage: /pixelregion edit <name> <priority|miny|maxy|point ...>");
            return;
        }

        final Region region = manager.byName(args[1]).orElse(null);
        if (region == null) {
            messages.send(sender, "Region not found.");
            return;
        }
        if (!controls(sender, region)) {
            messages.send(sender, "You do not control this region.");
            return;
        }

        try {
            final String operation = args[2].toLowerCase(Locale.ROOT);
            switch (operation) {
                case "priority" -> editPriority(sender, region, args);
                case "miny" -> editY(sender, region, args, true);
                case "maxy" -> editY(sender, region, args, false);
                case "point" -> editPoint(sender, region, args);
                default -> messages.send(sender, "Unknown edit operation.");
            }
        } catch (NumberFormatException exception) {
            messages.send(sender, "Numeric values are required.");
        } catch (IllegalArgumentException exception) {
            messages.send(sender, "Edit rejected: " + exception.getMessage());
        }
    }

    private void editPriority(CommandSender sender, Region region, String[] args) {
        if (args.length < 4) {
            messages.send(sender, "Usage: /pixelregion edit <name> priority <value>");
            return;
        }
        final Region updated = rebuild(region, region.points(), region.minY(), region.maxY(), Integer.parseInt(args[3]));
        manager.replace(updated);
        manager.save();
        messages.send(sender, "Priority updated.");
    }

    private void editY(CommandSender sender, Region region, String[] args, boolean min) {
        if (args.length < 4) {
            messages.send(sender, "Usage: /pixelregion edit <name> " + (min ? "miny" : "maxy") + " <value>");
            return;
        }
        final int value = Integer.parseInt(args[3]);
        final int minY = min ? value : region.minY();
        final int maxY = min ? region.maxY() : value;
        final Region updated = rebuild(region, region.points(), minY, maxY, region.priority());
        manager.replace(updated);
        manager.save();
        messages.send(sender, (min ? "Min-Y" : "Max-Y") + " updated.");
    }

    private void editPoint(CommandSender sender, Region region, String[] args) {
        if (args.length < 4) {
            messages.send(sender, "Usage: /pixelregion edit <name> point <add|set|remove> ...");
            return;
        }

        final List<RegionPoint> points = new ArrayList<>(region.points());
        final String operation = args[3].toLowerCase(Locale.ROOT);

        switch (operation) {
            case "add" -> {
                if (args.length < 6) {
                    messages.send(sender, "Usage: ... point add <x> <z>");
                    return;
                }
                points.add(new RegionPoint(Double.parseDouble(args[4]), Double.parseDouble(args[5])));
            }
            case "set" -> {
                if (args.length < 7) {
                    messages.send(sender, "Usage: ... point set <index> <x> <z>");
                    return;
                }
                final int index = parsePointIndex(args[4], points.size());
                points.set(index, new RegionPoint(Double.parseDouble(args[5]), Double.parseDouble(args[6])));
            }
            case "remove" -> {
                if (args.length < 5) {
                    messages.send(sender, "Usage: ... point remove <index>");
                    return;
                }
                if (points.size() <= 3) {
                    messages.send(sender, "A polygon must keep at least three points.");
                    return;
                }
                points.remove(parsePointIndex(args[4], points.size()));
            }
            default -> {
                messages.send(sender, "Use add, set or remove.");
                return;
            }
        }

        final Region updated = rebuild(region, points, region.minY(), region.maxY(), region.priority());
        manager.replace(updated);
        manager.save();
        messages.send(sender, "Polygon points updated.");
    }

    private void delete(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.delete")) {
            return;
        }
        if (args.length < 2) {
            messages.send(sender, "Usage: /pixelregion delete <name>");
            return;
        }
        final Region region = manager.byName(args[1]).orElse(null);
        if (region == null) {
            messages.send(sender, "Region not found.");
            return;
        }
        if (!controls(sender, region)) {
            messages.send(sender, "You do not control this region.");
            return;
        }
        if (!manager.remove(region.name())) {
            messages.send(sender, "Region could not be deleted.");
            return;
        }
        manager.save();
        messages.send(sender, "Region deleted.");
    }

    private void flag(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.flag")) {
            return;
        }
        if (args.length < 4) {
            messages.send(sender, "Usage: /pixelregion flag <name> <flag> <allow|deny>");
            return;
        }

        final Region region = manager.byName(args[1]).orElse(null);
        if (region == null) {
            messages.send(sender, "Region not found.");
            return;
        }
        if (!controls(sender, region)) {
            messages.send(sender, "You do not control this region.");
            return;
        }

        final RegionFlag flag;
        final FlagState state;
        try {
            flag = RegionFlag.valueOf(args[2].toUpperCase(Locale.ROOT));
            state = FlagState.valueOf(args[3].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            messages.send(sender, "Unknown flag or state.");
            return;
        }

        region.setFlag(flag, state);
        manager.markDirty();
        manager.save();
        messages.send(sender, "Flag updated.");
    }

    private void member(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.member")) {
            return;
        }
        if (args.length < 4) {
            messages.send(sender, "Usage: /pixelregion member <add|remove> <name> <player>");
            return;
        }

        final Region region = manager.byName(args[2]).orElse(null);
        if (region == null) {
            messages.send(sender, "Region not found.");
            return;
        }
        if (!controls(sender, region)) {
            messages.send(sender, "You do not control this region.");
            return;
        }

        final Player target = plugin.getServer().getPlayerExact(args[3]);
        if (target == null) {
            messages.send(sender, "Player must be online.");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            region.addMember(target.getUniqueId());
        } else if (args[1].equalsIgnoreCase("remove")) {
            region.removeMember(target.getUniqueId());
        } else {
            messages.send(sender, "Use add or remove.");
            return;
        }

        manager.markDirty();
        manager.save();
        messages.send(sender, "Member updated.");
    }

    private void debug(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "pixelregion.command.debug")) {
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "This command requires a player.");
            return;
        }

        final RegionFlag flag;
        try {
            flag = args.length >= 2
                    ? RegionFlag.valueOf(args[1].toUpperCase(Locale.ROOT))
                    : RegionFlag.USE;
        } catch (IllegalArgumentException exception) {
            messages.send(sender, "Unknown flag.");
            return;
        }

        final List<Region> applicable = manager.applicableRegions(player.getLocation());
        messages.send(sender, "Location: " + player.getLocation().getBlockX() + ", "
                + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
        messages.send(sender, "Flag: " + flag);
        messages.send(sender, "Applicable regions: " + applicable.size());

        for (Region region : applicable) {
            messages.send(sender, "- " + region.name()
                    + " priority=" + region.priority()
                    + " explicit=" + region.flag(flag)
                    + " owner=" + region.owner()
                    + " member=" + region.hasAccess(player.getUniqueId()));
        }

        final boolean allowed = policy.allows(player.getLocation(), flag, player.getUniqueId(),
                player.hasPermission("pixelregion.bypass"));
        messages.send(sender, "Final policy: " + (allowed ? "ALLOW" : "DENY"));
    }

    private void reload(CommandSender sender) {
        if (!hasPermission(sender, "pixelregion.command.reload")) {
            return;
        }
        manager.load();
        messages.send(sender, "Regions reloaded.");
    }

    private void save(CommandSender sender) {
        if (!hasPermission(sender, "pixelregion.command.save")) {
            return;
        }
        manager.save();
        messages.send(sender, "Regions saved.");
    }

    private boolean controls(CommandSender sender, Region region) {
        return sender.hasPermission("pixelregion.admin")
                || sender instanceof Player player && region.hasAccess(player.getUniqueId());
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            messages.send(sender, "You do not have permission to use this command.");
            return false;
        }
        return true;
    }

    private static int parsePointIndex(String value, int size) {
        final int index = Integer.parseInt(value);
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Point index out of range.");
        }
        return index;
    }

    private static Region rebuild(
            Region source,
            List<RegionPoint> points,
            int minY,
            int maxY,
            int priority
    ) {
        return new Region(
                source.id(),
                source.name(),
                source.worldId(),
                minY,
                maxY,
                points,
                priority,
                source.owner(),
                source.members(),
                new EnumMap<>(source.flags())
        );
    }
}
