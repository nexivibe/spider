# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a libGDX game project called "spider" generated with gdx-liftoff. It's a multi-platform game targeting desktop (LWJGL3), Android, and iOS.

## Build Commands

```bash
# Run the desktop application
./gradlew lwjgl3:run

# Build runnable JAR (outputs to lwjgl3/build/libs/)
./gradlew lwjgl3:jar

# Platform-specific JARs (smaller file sizes)
./gradlew lwjgl3:jarWin    # Windows only
./gradlew lwjgl3:jarMac    # macOS only
./gradlew lwjgl3:jarLinux  # Linux only

# Build all projects
./gradlew build

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

## Architecture

- **core/**: Shared game logic (all platforms). Main entry point is `ape.spider.Main` which extends `ApplicationAdapter`.
- **lwjgl3/**: Desktop launcher using LWJGL3. Entry point is `Lwjgl3Launcher`.
- **android/**: Android platform module.
- **ios/**: iOS platform using RoboVM.
- **assets/**: Shared game assets. An `assets.txt` manifest is auto-generated during build.

## Key Dependencies

- libGDX 1.13.1 (core game framework)
- Box2D (physics)
- Box2DLights (lighting)
- FreeType (font rendering)
- gdx-controllers (gamepad support)

## Configuration

- `gradle.properties`: Version numbers and build settings
- Java 8 source compatibility
- GraalVM native image support available via `enableGraalNative=true`
