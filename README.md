# Pixel-Region

Pixel-Region is a standalone polygon-based region protection plugin for Paper 26.2.

## Platform

- Paper 26.2 / Dev Bundle 26.2.build.121-stable
- Java 25
- Mojang mappings
- `paper-plugin.yml`
- Adventure Components and MiniMessage for configured player messages
- Gradle 9.2.0 + Shadow 9.6.1

Pixel-Region has no runtime dependency on PixelRPG or another plugin.

## Region model

A region contains:

- UUID
- case-insensitively unique name
- world UUID
- Min-Y / Max-Y
- validated X/Z polygon
- priority
- owner
- members
- explicit flags

The polygon rejects fewer than three points, adjacent duplicate points, zero-area polygons, non-finite coordinates and self-intersections.

Queries use a world + chunk spatial index first, followed by Y range, bounding-box and polygon checks. Regions crossing chunk boundaries are indexed in every affected chunk.

## Policy

Multiple regions may overlap.

1. World UUID, Y range, bounds and polygon must match.
2. Matching regions are ordered by descending priority.
3. The first explicit flag wins.
4. An unset flag falls through to the next matching region.
5. If no matching region has an explicit decision, the configured default is used.
6. Owners and members receive the explicitly defined access of the region they control.
7. `pixelregion.bypass` is a separate global bypass.

## Protection flags

- `BUILD`
- `USE`
- `CONTAINER_USE`
- `PVP`
- `MOB_DAMAGE`
- `ENTITY_DAMAGE`
- `EXPLOSION`
- `FIRE_SPREAD`
- `FLUID_FLOW`
- `MOB_SPAWN`
- `ENTRY`
- `EXIT`

Protection is implemented through the central policy service. Container opening, creature spawning, fire ignition/spread, fluids, explosions, combat and block interactions are covered by dedicated listeners.

## Entry / Exit

Movement compares complete region sets before and after the move. This correctly handles:

- outside -> inside
- inside -> outside
- region A -> region B
- overlapping regions
- Y boundary changes
- world changes
- teleports, through the current Paper movement event chain

Only regions actually entered or exited generate transition messages.

## Commands

All commands require `pixelregion.command` at the root and then their specific permission.

```text
/pixelregion create <name>
/pixelregion point [x] [z]
/pixelregion finish
/pixelregion cancel
/pixelregion list
/pixelregion info [name]

/pixelregion edit <name> priority <value>
/pixelregion edit <name> miny <value>
/pixelregion edit <name> maxy <value>
/pixelregion edit <name> point add <x> <z>
/pixelregion edit <name> point set <index> <x> <z>
/pixelregion edit <name> point remove <index>

/pixelregion flag <name> <flag> <allow|deny>
/pixelregion member <add|remove> <name> <player>
/pixelregion delete <name>

/pixelregion debug [flag]
/pixelregion reload
/pixelregion save
```

Edits are validated before replacement, so an invalid polygon never replaces the existing region.

## Permissions

- `pixelregion.command`
- `pixelregion.command.create`
- `pixelregion.command.list`
- `pixelregion.command.info`
- `pixelregion.command.edit`
- `pixelregion.command.delete`
- `pixelregion.command.flag`
- `pixelregion.command.member`
- `pixelregion.command.reload`
- `pixelregion.command.save`
- `pixelregion.command.debug`
- `pixelregion.admin`
- `pixelregion.bypass`

The root command permission is operator-default and inherits the command sub-permissions. Ownership is still checked for region mutations; `pixelregion.admin` controls all regions.

## Storage

Region data is stored in:

`plugins/Pixel-Region/regions.json`

The JSON document contains a schema version. Legacy top-level JSON arrays remain readable.

Writes use:

1. serialization to a temporary file
2. backup of the previous file
3. atomic replace when supported
4. normal replace fallback

If the primary file is corrupt, the backup is attempted. Unsupported future schema versions are rejected instead of being silently overwritten.

The manager tracks a dirty state and saves after important command mutations and during shutdown.

## Configuration

The default configuration controls every supported policy flag:

```yaml
protection:
  default-build: allow
  default-use: allow
  default-container-use: allow
  default-pvp: allow
  default-mob-damage: allow
  default-entity-damage: allow
  default-explosion: allow
  default-fire-spread: allow
  default-fluid-flow: allow
  default-mob-spawn: allow
  default-entry: allow
  default-exit: allow
```

Player-facing entry, exit and denial messages are configured with MiniMessage. The `{region}` placeholder is inserted as unparsed user data.

## Diagnostics

`/pixelregion debug [flag]` shows the current location, matching regions, priority, explicit flag state, owner/member context and the final policy result.

## Tests

Automated JUnit tests cover:

- triangle, rectangle and concave polygons
- inside, outside, edge and corner behavior
- duplicate points
- zero-area polygons
- self-intersection
- world and Y filtering
- priority and flag fallthrough
- owner/member access
- defaults and bypass
- spatial index and chunk-boundary behavior
- many-region query coverage
- transition set calculation
- JSON save/load, legacy format, backup and future-schema rejection

## Build

```text
gradle clean check build --no-daemon --stacktrace
```

The build verifies:

- Java 25
- Paper 26.2
- forbidden legacy APIs
- no static live-server references
- tests
- ShadowJar
- Gson relocation
- final plugin artifact

CI runs the same clean verification flow on `main`.
