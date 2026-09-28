# Exiled Sector

Because your Hound deserves a character arc.

Designed to be an overhaul of the simple hullmod system based on the infamous Path of Exile skill tree, ExiledSector is a [Starsector](https://fractalsoftworks.com/) mod that gives every ship in your fleet its own skill tree. Still sane exile?

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
  - Ships also earn XP in combat. Each level turns the ship's most recently OP-bought node into a free one, so veteran ships get their ordnance points back. The crew gets nothing, as is tradition.
- **Hidden, unlockable nodes.**

<img src="\graphics\description\Unidentified_node.png" alt="Hidden Node">
  
- **Unique passive effects**
- **Beam splitting** 
  - Your Tachyon Lance can now disappoint multiple enemies at once.
  
<img src="\graphics\description\lions_gaze.png" alt="Lion's Gaze">
<img src="\graphics\description\lions_gaze2.png" alt="Lion's Gaze2">

- **Faction themed**
  - Your ship's main goal can be to blow up, and act like you don't know nobody

<img src="\graphics\description\ludds_light.png" alt="Ludds Light">
<img src="\graphics\description\at_any_cost.png" alt="At Any Cost">

## Requirements

- Starsector 0.98a-RC8
- [LazyLib](https://fractalsoftworks.com/forum/index.php?topic=5444.225)
- [MagicLib](https://fractalsoftworks.com/forum/index.php?topic=25868.0)
- [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658.0)


Optional:

- Console Commands adds `ExiledSectorGrantFleetXp [amount]`.
- a healthy disregard for vanilla balance.

## Configuration

Play your own way – all settings are in the LunaLib mod settings menu:

- OP cost per node, by hull size
- Leveling curve: max level, XP per level, XP growth, and XP gained from a lost battle
- Maximum number of allocated nodes per ship
- Whether hidden nodes are revealed, and which unlock conditions are enforced

## Disclaimer

This mod is in (very) early development. 

It is my first mod, and first foray into OpenGL/game development, and is by no means feature complete or bug free.

I have used starsector-core graphics and royalty-free art assets for all art in the mod. I will not use LLM-generated art.

I am seeking any feedback, bug reports, node suggestions, or faction designs. 

You can find me on the [Unofficial Starsector Discord](https://fractalsoftworks.com/forum/index.php?topic=11488.0) or by direct message @portals_ 

### Compatibility

> [!WARNING]
> Exiled Sector adds permanent hull mods to the ships in your fleet. Don't remove it from a save that has used it.

I have done what I can for some mods, but there is a long way to go for complete mod compatibility.

**[Second-in-Command](https://fractalsoftworks.com/forum/index.php?topic=30407.0)** is (theoretically) supported.

Some weapons from other mods misbehave when their beams are split or their shots are chained. These are listed in:

- `data/config/exiledSector/split_beam_effect_blocklist.csv`
- `data/config/exiledSector/energy_chain_blocklist.csv`

Both files are merged across mods, so other mods can opt their own weapons out.
