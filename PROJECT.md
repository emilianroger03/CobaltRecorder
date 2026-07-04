# Cobalt Recorder

## What is it?
A Cobalt addon (Fabric mod, Minecraft 1.21.4) that records and replays player actions for Hypixel SkyBlock mining — specifically **powder mining** (Crystal Hollows/Glacite Tunnels) and **pure ore mining** routes, with etherwarp navigation.

## Why am I building this?
To automate repetitive mining routes — record once, replay forever. Powder mining and ore mining are grind-heavy and repetitive, so a macro that replays exact routes saves tons of time.

## Steps
- [x] Step 1: Set up Cobalt addon project structure
- [x] Step 2: Create recording data structures (frames, actions, positions)
- [x] Step 3: Implement Recorder - captures player state every tick
- [x] Step 4: Implement PlaybackExecutor - replays with etherwarp navigation
- [x] Step 5: Create command system (/rec start, stop, play, loop, etc.)
- [ ] Step 6: Build and test in-game (get it to compile and load)
- [ ] Step 7: Test a powder mining route — record and replay in Crystal Hollows or Glacite Tunnels
- [ ] Step 8: Test a pure ore mining route — record and replay hitting veins
- [ ] Step 9: Add GUI toggle in Cobalt menu
- [ ] Step 10: Add keybind support for quick record/stop
- [ ] Step 11: Optimize playback smoothness + fix etherwarp release timing

## Mining Focus
- **Powder Mining** — targets Crystal Hollows / Glacite Tunnels; lots of movement and etherwarp jumps between nodes
- **Pure Ore Mining** — hitting specific ore veins repeatedly; less movement, more precision clicks

## Today's Goal
Get the addon compiling and testable in-game.

## Progress Log
- 2026-07-03: Created full addon structure with recording, playback, and commands
- 2026-07-04: Clarified project focus to powder mining + pure ore mining; set up CLAUDE.md and memory

## Commands
- `/rec start <name>` - Start recording
- `/rec stop` - Stop recording or playback
- `/rec save` - Save current recording
- `/rec play <name>` - Play a recording once
- `/rec loop <name> <count>` - Loop playback
- `/rec list` - List all recordings
- `/rec delete <name>` - Delete a recording
- `/rec status` - Show current status

## Features
- Records every tick: position, rotation, clicks, sneaking, sprinting
- Records inventory: hotbar switches, armor changes
- Etherwarp navigation during playback (shift+right-click)
- Loop support for repeated runs
- Saves to JSON files in config/cobalt/recordings/
