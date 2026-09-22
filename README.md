# Pixel-Region

Pixel-Region is a standalone polygon-based region protection plugin for Paper 26.2.

## Core model

- Regions are independent objects identified by UUID.
- Each region belongs to one world UUID.
- Geometry is a validated X/Z polygon.
- Regions have an explicit vertical range.
- Multiple regions may overlap.
- Priority determines the effective region policy.
- Flags are resolved from highest priority to lowest priority; an unset flag falls through.
- Owners and members can bypass the explicit protection flag of the region they control.
- Region data is stored in `plugins/Pixel-Region/regions.json`.
- No PixelRPG classes or runtime dependency are required.

## Commands

`/pixelregion`

- `create <name>`
- `point [x] [z]`
- `finish`
- `cancel`
- `list`
- `info [name]`
- `flag <name> <flag> <allow|deny>`
- `member <add|remove> <name> <player>`
- `delete <name>`
- `reload`
- `save`

## Initial flags

- BUILD
- USE
- PVP
- MOB_DAMAGE
- ENTITY_DAMAGE
- EXPLOSION
- FIRE_SPREAD
- FLUID_FLOW
- MOB_SPAWN
- ENTRY
- EXIT

The project is intentionally structured so that further WorldGuard-inspired systems can be added without coupling the region core to PixelRPG.
