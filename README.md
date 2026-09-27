# Exiled Sector

Ever wish you had _even_ more customization options? Still sane exile?

Designed to be an overhaul of the simple hullmod system based on the infamous Path of Exile skill tree, ExiledSector is a [Starsector](https://fractalsoftworks.com/) mod that gives every ship in your fleet its own skill tree.

Navigate to the outfit screen and click the skill tree button on any of your ships to explore an entirely new sector:

<img src="\graphics\description\teaser.gif" alt="The skill tree">

## Features

- **A tree per ship.** 
  - 400+ nodes built from 180+ node types, spread across small nodes, notables and keystones.
- **Tech-level starting points.** 
  - Low Tech, Midline and High Tech roots. The root a ship starts from is chosen by its manufacturer.
- **Multi-choice travel nodes.** 

<img src="\graphics\description\teaser_choice.gif" alt="Choice Node">

- **Synergistic allocation** 
  - Every allocated node costs OP: 1 for frigates, 2 for destroyers, 3 for cruisers and 4 for capitals (configurable).
  - Ships also earn XP in combat. Each level turns the ship's most recently OP-bought node into a free one, so veteran ships get their ordnance points back (to be spent on more nodes!).
- **Hidden nodes.** Some nodes appear as unidentified until you learn the matching blueprint.

<img src="\graphics\description\Unidentified_node.png" alt="Hidden Node">
  
- **Unique passive effects**

<img src="\graphics\description\lions_gaze.png" alt="Lion's Gaze">

<img src="\graphics\description\ludds_light.png" alt="Ludd's Light">

<img src="\graphics\description\at_any_cost.png" alt="At Any Cost">

## Requirements

- Starsector 0.98a-RC8
- LazyLib
- MagicLib
- LunaLib

Optional:

- **Console Commands** adds `ExiledSectorGrantFleetXp [amount]`, which grants XP to every ship in your fleet.

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

Play your own way!

## Disclaimer

This mod is in (very) early development. It is my first mod, and first foray into UI design/game development.

I am seeking any feedback, bug reports and node suggestions. Please

### Compatibility

I have done what I can for some mods, but there is a long way to go for complete mod compatibility.

**Second-in-Command** is (theoretically) supported.

Some weapons from other mods misbehave when their beams are split or their shots are chained. These are listed in:

- `data/config/exiledSector/split_beam_effect_blocklist.csv`
- `data/config/exiledSector/energy_chain_blocklist.csv`

Both files are merged across mods, so other mods can opt their own weapons out.
