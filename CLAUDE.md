# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "Ruler App" that displays 7 adjustable horizontal ruler lines as an overlay on top of other apps. It's built with Kotlin and uses Android's overlay system.

## Build and Development Commands

```bash
# Build the debug APK
./gradlew assembleDebug

# Build the release APK
./gradlew assembleRelease

# Clean the project
./gradlew clean

# Install on connected device
./gradlew installDebug
```

## Architecture

The app follows a simple service-based architecture:

1. **MainActivity** (`app/src/main/java/com/example/ruler/MainActivity.kt`): Entry point that manages permissions and service lifecycle
2. **RulerService** (`app/src/main/java/com/example/ruler/RulerService.kt`): Foreground service that creates and manages the overlay with two views:
   - `RulerLinesView`: Non-interactive view that draws the lines
   - `RulerControlsView`: Interactive view with drag handles and controls

The service uses SharedPreferences to persist line positions and app state between sessions.

## Key Technical Details

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 33 (Android 13)
- **Kotlin Version**: 1.8.0
- **Critical Permission**: `SYSTEM_ALERT_WINDOW` for overlay functionality
- **State Management**: SharedPreferences with keys:
  - `service_running_status`
  - `line_position_0` through `line_position_6`
  - `first_launch`

## Development Considerations

1. **Overlay System**: When modifying overlay behavior, test on different Android versions as overlay APIs have changed
2. **Service Lifecycle**: The service uses `START_STICKY` to restart if killed - consider this when making changes
3. **Touch Handling**: The dual-view approach separates interactive elements from visual elements to manage touch events properly
4. **Screen Bounds**: Line positions are constrained with 40px padding from screen edges

## Testing

Currently no automated tests are implemented. When adding tests:
- Unit tests would go in `app/src/test/`
- Instrumentation tests would go in `app/src/androidTest/`
- Test overlay permission flows carefully on different Android versions