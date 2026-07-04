# CobaltRecorder — Project Instructions

## What This Is
A Fabric mod (Minecraft 1.21.4) built on the Cobalt client framework. It records and replays player actions for Hypixel SkyBlock mining — specifically powder mining and pure ore mining routes. Uses etherwarp for fast navigation between positions.

## Stack
- **Language:** Kotlin
- **Mod loader:** Fabric 1.21.4
- **Framework:** Cobalt addon system (JitPack: `com.github.CobaltScripts:Cobalt:1.0.0`)
- **Build:** Gradle 8.5 with Kotlin DSL
- **Serialization:** `kotlinx-serialization-json`

## Key Directories
- `src/main/kotlin/dev/recorder/` — all source code
  - `recording/` — data models (RecordedFrame, Recording) + Recorder + RecordingManager
  - `playback/` — PlaybackExecutor (tick-driven, handles etherwarp)
  - `command/` — `/rec` command and all subcommands
- `src/main/resources/` — `fabric.mod.json`, `cobalt-addon.json`
- Config saves to: `config/cobalt/recordings/<name>.json`

## Known Issues to Watch
- `wasLeftClicking` / `wasRightClicking` in Recorder.kt are tracked but never used — dead code
- `asin` is imported but unused in PlaybackExecutor.kt
- Etherwarp shift-release relies on the next frame having `sneaking = false` — could misfire
- `fabric.mod.json` has empty entrypoints block (Cobalt uses its own `cobalt-addon.json` loader)

## Build Command
```
./gradlew build
```
Output jar goes to `build/libs/`.

## Hypixel SkyBlock Context
- **Powder Mining** — Crystal Hollows / Glacite Tunnels; lots of movement + etherwarp jumps between gemstone/powder nodes
- **Pure Ore Mining** — hitting specific ore veins repeatedly; less movement, more precision clicks
- Etherwarp = SkyBlock ability that teleports you to where you're looking (shift + right-click with a warp item equipped)
