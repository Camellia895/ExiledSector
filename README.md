# Exiled Sector

A [Starsector](https://fractalsoftworks.com/) mod that gives every ship in your fleet its own skill tree.

Open any ship's refit screen and press **Skill Tree** to spend points on a large, branching passive tree. Each ship keeps its own allocations, so two ships of the same hull can be built in completely different directions.

## Features

- **A tree per ship.** 432 nodes built from 185 node types, spread across small nodes, notables and keystones.
- **Tech-level starting points.** Low Tech, Midline and High Tech roots. The root a ship starts from is chosen by its manufacturer.
- **Wormholes.** Paired nodes that link distant regions of the tree.
- **Choice nodes.** Nodes that let you pick one of several options, such as flux capacity, flux dissipation or hull.
- **Paid for in ordnance points.** Every allocated node costs OP: 1 for frigates, 2 for destroyers, 3 for cruisers and 4 for capitals (all configurable).
- **Ships level up.** Ships earn XP in combat, scaled by the deployment points of enemy ships they destroy or disable. Each level turns the ship's most recently OP-bought node into a free one, so veteran ships get their ordnance points back.
- **Hull-mod nodes.** Some nodes grant or install hull mods, and conflicts with hull mods already installed are handled automatically.
- **Hidden nodes.** Some nodes appear as unidentified sensor ghosts until you learn the matching blueprint.
- **New combat mechanics.** Beams that split across nearby enemies, energy shots that chain between targets, shield damage shared across nearby allies, escort bonuses near larger friendly ships, and more.

## Requirements

- Starsector 0.98a-RC8
- LazyLib
- MagicLib
- LunaLib

Optional:

- **Console Commands** adds `ExiledSectorGrantFleetXp [amount]`, which grants XP to every ship in your fleet.
- **Second-in-Command** is supported. Some nodes count as the matching hull mods for its skills.

## Installation

There is no prebuilt release yet, so build the mod from source first (see below). Then copy `mod_info.json`, `jars/`, `data/` and `graphics/` into `Starsector/mods/ExiledSector/` and enable the mod in the launcher.

> [!WARNING]
> Exiled Sector adds permanent hull mods to the ships in your fleet. Don't remove it from a save that has used it.

## Configuration

All settings are in the LunaLib mod settings menu:

- OP cost per node, by hull size
- Leveling curve: max level, XP per level, XP growth, and XP gained from a lost battle
- Maximum number of allocated nodes per ship
- Whether hidden nodes are revealed, and which unlock conditions are enforced

### Compatibility blocklists

Some weapons from other mods misbehave when their beams are split or their shots are chained. These are listed in:

- `data/config/exiledSector/split_beam_effect_blocklist.csv`
- `data/config/exiledSector/energy_chain_blocklist.csv`

Both files are merged across mods, so another mod can opt its own weapons out by shipping a file at the same path.

## Building from source

You need JDK 17, Maven, and a Starsector install with LazyLib, MagicLib, LunaLib and Console Commands in its `mods` folder.

1. Edit the paths at the top of `setup-libs.ps1` and `deploy.ps1` to point at your Starsector install.
2. Copy the compile-time jars into `libs/`:

   ```powershell
   .\setup-libs.ps1
   ```

3. Build, run the tests and install into your mods folder:

   ```powershell
   .\deploy.ps1
   ```

   To only build, run `mvn package`. The jar is written to `jars/ExiledSector.jar`.

## Editing the skill tree

The tree lives in `data/skilltrees/ship_skill_tree.json` (node layout) and `data/skilltrees/skill_types.json` (what each node does). There's a visual editor for both:

```powershell
.\tools\skill_tree_server.ps1
```

Then open <http://localhost:8791/>. Saving in the editor writes straight to the two JSON files.
